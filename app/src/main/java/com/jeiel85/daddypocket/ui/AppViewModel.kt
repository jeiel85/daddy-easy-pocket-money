package com.jeiel85.daddypocket.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jeiel85.daddypocket.MainActivity
import com.jeiel85.daddypocket.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(db.appDao())

    // --- Navigation/Common State ---
    private val _currentMonth = MutableStateFlow("")
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()

    // --- DB Flow States ---
    val allExpenses = repository.getAllExpensesFlow()
    val allCards = repository.getAllCardsFlow()
    val activeCards = repository.getActiveCardsFlow()
    val allFixedExpenses = repository.getAllFixedExpensesFlow()

    // --- Budget State ---
    private val _monthlyBudget = MutableStateFlow<Budget?>(null)
    val monthlyBudget: StateFlow<Budget?> = _monthlyBudget.asStateFlow()

    // --- Monthly Filter States ---
    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedCardFilter = MutableStateFlow<Int?>(null)
    val selectedCardFilter: StateFlow<Int?> = _selectedCardFilter.asStateFlow()

    // --- Notification/Reminder Banner State ---
    private val _paymentDayAlarms = MutableStateFlow<List<String>>(emptyList())
    val paymentDayAlarms: StateFlow<List<String>> = _paymentDayAlarms.asStateFlow()

    init {
        // Set initial month to current "yyyy-MM" (e.g. "2026-06")
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val initialMonth = sdf.format(Date())
        _currentMonth.value = initialMonth

        // Listen for month changes to load budget and auto-apply fixed expenses
        viewModelScope.launch {
            _currentMonth.collect { month ->
                if (month.isNotEmpty()) {
                    loadBudget(month)
                    // Auto-apply fixed expenses for the month on startup/month-change
                    repository.applyFixedExpensesForMonth(month)
                }
            }
        }

        // Monitor expenses and cards to check for coming card payment days
        viewModelScope.launch {
            combine(allCards, _currentMonth) { cards, month ->
                Pair(cards, month)
            }.collect { (cards, month) ->
                checkCardPaymentDays(cards)
            }
        }

        createNotificationChannel()
    }

    // --- Month Controls ---
    fun selectMonth(month: String) {
        _currentMonth.value = month
    }

    fun nextMonth() {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val date = sdf.parse(_currentMonth.value) ?: return
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, 1)
            _currentMonth.value = sdf.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun prevMonth() {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val date = sdf.parse(_currentMonth.value) ?: return
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.MONTH, -1)
            _currentMonth.value = sdf.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Budget Logic ---
    private fun loadBudget(month: String) {
        viewModelScope.launch {
            val budget = repository.getBudgetForMonth(month)
            if (budget == null) {
                // Set default budget to 500k KRW if not exists for a brand new user
                repository.saveBudget(month, 500000L)
                _monthlyBudget.value = Budget(month, 500000L)
            } else {
                _monthlyBudget.value = budget
            }
        }
    }

    fun updateBudget(month: String, amount: Long) {
        viewModelScope.launch {
            repository.saveBudget(month, amount)
            _monthlyBudget.value = Budget(month, amount)
        }
    }

    // --- Filter Handlers ---
    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setCardFilter(cardId: Int?) {
        _selectedCardFilter.value = cardId
    }

    // --- Expense Operations ---
    suspend fun getExpenseById(id: Int): Expense? = repository.getExpenseById(id)

    fun saveExpense(
        id: Int,
        amount: Long,
        category: String,
        paymentType: String,
        cardId: Int?,
        memo: String,
        date: String
    ) {
        viewModelScope.launch {
            val expense = Expense(
                id = if (id == 0) 0 else id,
                amount = amount,
                category = category,
                paymentType = paymentType,
                cardId = cardId,
                memo = memo,
                date = date,
                createdAt = if (id == 0) System.currentTimeMillis() else (repository.getExpenseById(id)?.createdAt ?: System.currentTimeMillis())
            )
            if (id == 0) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // --- Card Operations ---
    fun saveCard(id: Int, name: String, paymentDay: Int, isActive: Boolean) {
        viewModelScope.launch {
            val card = CardInfo(id = id, name = name, paymentDay = paymentDay, isActive = isActive)
            repository.saveCard(card)
        }
    }

    fun deleteCard(card: CardInfo) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    // --- Fixed Expense Operations ---
    fun saveFixedExpense(id: Int, title: String, amount: Long, category: String, paymentDay: Int, isActive: Boolean, autoApply: Boolean) {
        viewModelScope.launch {
            val fixed = FixedExpense(id = id, title = title, amount = amount, category = category, paymentDay = paymentDay, isActive = isActive, autoApply = autoApply)
            repository.saveFixedExpense(fixed)
            // Trigger auto-apply instantly to let it sync
            repository.applyFixedExpensesForMonth(_currentMonth.value)
        }
    }

    fun deleteFixedExpense(fixedExpense: FixedExpense) {
        viewModelScope.launch {
            repository.deleteFixedExpense(fixedExpense)
        }
    }

    // --- Calculated Statistics for Home (Filtered by selectedMonth) ---
    val currentMonthExpenses: StateFlow<List<Expense>> = _currentMonth
        .flatMapLatest { month ->
            repository.getExpensesForMonthFlow(month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeStatsStream: StateFlow<HomeStats> = combine(
        _monthlyBudget,
        currentMonthExpenses
    ) { budget, expenses ->
        val budgetAmount = budget?.amount ?: 500000L
        val totalSpent = expenses.sumOf { it.amount }
        val remaining = budgetAmount - totalSpent

        // Today's Spent
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todaySpent = expenses.filter { it.date == todayStr }.sumOf { it.amount }

        // Top 3 Visual Categories
        val categoryGroups = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(3)

        HomeStats(
            budget = budgetAmount,
            totalSpent = totalSpent,
            remaining = remaining,
            todaySpent = todaySpent,
            topCategories = categoryGroups
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeStats())

    // --- Filtered Expenses for Monthly History ---
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        currentMonthExpenses,
        _selectedCategoryFilter,
        _selectedCardFilter
    ) { expenses, category, cardId ->
        expenses.filter { expense ->
            val matchCategory = category == null || expense.category == category
            val matchCard = cardId == null || expense.cardId == cardId
            matchCategory && matchCard
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Card Spend Information (Summary) ---
    val cardMonthlySpend: StateFlow<Map<Int, Long>> = currentMonthExpenses.map { expenses ->
        expenses.filter { it.cardId != null }
            .groupBy { it.cardId!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- Card Payment Day Notification Alarm Logic ---
    private fun checkCardPaymentDays(cards: List<CardInfo>) {
        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val alerts = mutableListOf<String>()

        for (card in cards) {
            if (!card.isActive) continue
            val diff = card.paymentDay - todayDay
            if (diff in 0..3) {
                val dayWord = when (diff) {
                    0 -> "오늘"
                    1 -> "내일"
                    else -> "${diff}일 뒤"
                }
                alerts.add("[${card.name}] 결제일이 ${dayWord}(${card.paymentDay}일)입니다!")
                
                // Show actual system notification on today or 1 day before
                if (diff == 1 || diff == 0) {
                    showSystemNotification(card.name, "카드 결제일 안내", "오늘은 ${card.name} 결제일(${card.paymentDay}일) 전후입니다. 지출을 미리 확인하세요!")
                }
            }
        }
        _paymentDayAlarms.value = alerts
    }

    // --- CSV Export Logic ---
    fun exportToCSV() {
        val app = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            val expensesList = currentMonthExpenses.value
            if (expensesList.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, "이번 달 지출 내역이 없어 CSV를 내보낼 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                val fileName = "daddy_pocket_expenses_${_currentMonth.value}.csv"
                val dir = File(app.cacheDir, "csv_exports")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fileName)

                val writer = FileWriter(file)
                // Write UTF-8 BOM representation for Excel Korean support
                writer.write('\ufeff'.toInt())
                writer.write("날짜,카테고리,결제수단,카드명,메모,금액\n")

                val cardMap = allCards.firstOrNull()?.associate { it.id to it.name } ?: emptyMap()

                for (exp in expensesList) {
                    val pType = when (exp.paymentType) {
                        "CASH" -> "현금"
                        "CHECK_CARD" -> "체크카드"
                        "CREDIT_CARD" -> "신용카드"
                        "TRANSFER" -> "계좌이체"
                        else -> exp.paymentType
                    }
                    val cardName = if (exp.cardId != null) cardMap[exp.cardId] ?: "미지정" else "현금/이체"
                    val sanitizedMemo = exp.memo.replace(",", " ")
                    writer.write("${exp.date},${exp.category},$pType,$cardName,$sanitizedMemo,${exp.amount}\n")
                }
                writer.flush()
                writer.close()

                withContext(Dispatchers.Main) {
                    shareCSVFile(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(app, "CSV 파일 생성 및 공유에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun shareCSVFile(file: File) {
        val app = getApplication<Application>()
        try {
            val fileUri = FileProvider.getUriForFile(
                app,
                "${app.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "아빠 용돈/카드 지출 정리 CSV 내보내기 (${_currentMonth.value})")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(intent, "CSV 지출 내역 내보내기").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(app, "CSV 공유를 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Android System Notification Helper ---
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val app = getApplication<Application>()
            val name = "카드 결제일 리마인더"
            val descriptionText = "카드 결제일 이벤트를 리마인드해주는 채널입니다."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CARD_PAYMENT_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun triggerTestNotification() {
        showSystemNotification(
            "테스트 카드", 
            "결제일 테스트 안내", 
            "여백과 가독성이 훌륭한 아빠 용돈 가계부의 로컬 알림이 성공적으로 전송되었습니다!"
        )
    }

    private fun showSystemNotification(cardName: String, title: String, content: String) {
        val app = getApplication<Application>()
        val intent = Intent(app, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            app, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(app, "CARD_PAYMENT_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use hash of cardName to prevent spamming duplicate notification categories
        notificationManager.notify(cardName.hashCode(), builder.build())
    }
}

// Stats Class
data class HomeStats(
    val budget: Long = 500000L,
    val totalSpent: Long = 0L,
    val remaining: Long = 500000L,
    val todaySpent: Long = 0L,
    val topCategories: List<Pair<String, Long>> = emptyList()
)
