package com.jeiel85.daddypocket.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel85.daddypocket.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputExpenseScreen(
    viewModel: AppViewModel,
    expenseId: Int = 0, // 0 if new, otherwise edit
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val cards by viewModel.activeCards.collectAsState(initial = emptyList())

    // Editing State loading
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("식비") }
    var selectedPaymentType by remember { mutableStateOf("CREDIT_CARD") } // CASH, CHECK_CARD, CREDIT_CARD, TRANSFER
    var selectedCardId by remember { mutableStateOf<Int?>(null) }
    var memo by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf("") }

    val categories = listOf("식비", "카페", "주유", "경조사", "용돈", "기타")
    val paymentTypes = listOf(
        "CREDIT_CARD" to "신용카드",
        "CHECK_CARD" to "체크카드",
        "CASH" to "현금",
        "TRANSFER" to "계좌이체"
    )

    // Set initial date to today
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    if (dateStr.isEmpty()) {
        dateStr = sdf.format(Date())
    }

    // Load expense for edit if exists
    LaunchedEffect(expenseId) {
        if (expenseId != 0) {
            val exp = viewModel.getExpenseById(expenseId)
            if (exp != null) {
                amountStr = exp.amount.toString()
                selectedCategory = exp.category
                selectedPaymentType = exp.paymentType
                selectedCardId = exp.cardId
                memo = exp.memo
                dateStr = exp.date
            }
        }
    }

    // Auto assign first physical card if none is selected and card payment type is chosen
    LaunchedEffect(selectedPaymentType, cards) {
        if ((selectedPaymentType == "CREDIT_CARD" || selectedPaymentType == "CHECK_CARD") && selectedCardId == null) {
            if (cards.isNotEmpty()) {
                selectedCardId = cards.first().id
            }
        } else if (selectedPaymentType == "CASH" || selectedPaymentType == "TRANSFER") {
            selectedCardId = null
        }
    }

    // Card Dropdown State
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == 0) "빠른 지출 입력" else "지출 내역 수정", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 금액 입력 영역 (Large scale standard text field)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "사용 금액",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = amountStr,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                amountStr = input
                            }
                        },
                        placeholder = { Text("0", fontSize = 28.sp, fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Right,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        suffix = { Text("원", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        )
                    )

                    // 아빠들을 위한 단축입력 보조버튼 (+1만 / +5만 / +10만 / 지우기)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "+1만원" to 10000L,
                            "+5만원" to 50000L,
                            "+10만원" to 100000L
                        ).forEach { (label, value) ->
                            Button(
                                onClick = {
                                    val currentVal = amountStr.toLongOrNull() ?: 0L
                                    amountStr = (currentVal + value).toString()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(44.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { amountStr = "" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(44.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("비우기", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. 카테고리 선택 영역 (식비, 카페, 주유, 경조사, 용돈, 기타)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("지출 항목 (카테고리)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    // Flows as a 3x2 grid of huge selectable buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (col in 0 until 3) {
                                    val idx = row * 3 + col
                                    if (idx < categories.size) {
                                        val cat = categories[idx]
                                        val isSelected = selectedCategory == cat
                                        OutlinedButton(
                                            onClick = { selectedCategory = cat },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            border = if (isSelected) null else ButtonDefaults.outlinedButtonBorder,
                                            modifier = Modifier.weight(1f).height(54.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(cat, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. 결제 수단 선택 영역 (신용, 체크, 현금, 계좌이체)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("결제 수단", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        paymentTypes.forEach { (typeVal, label) ->
                            val isSelected = selectedPaymentType == typeVal
                            Button(
                                onClick = { selectedPaymentType = typeVal },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(48.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Dynamically render Card Dropdown Selector if using Cards (CREDIT_CARD, CHECK_CARD)
                    if (selectedPaymentType == "CREDIT_CARD" || selectedPaymentType == "CHECK_CARD") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("카드 선택", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Box(modifier = Modifier.fillMaxWidth()) {
                            val cardMap = cards.associate { it.id to it.name }
                            val currentCardText = cardMap[selectedCardId] ?: "카드를 선택해주세요"

                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(currentCardText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "펼치기")
                                }
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                if (cards.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("등록된 사용 가능한 카드가 없습니다.", fontSize = 15.sp) },
                                        onClick = { dropdownExpanded = false }
                                    )
                                } else {
                                    cards.forEach { card ->
                                        DropdownMenuItem(
                                            text = { Text(card.name, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                selectedCardId = card.id
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. 날짜 및 메모 영역
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("결제 일자 및 메모", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    // Date Input Row
                    OutlinedButton(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            // Set to current custom date if parsing was successful
                            try {
                                val currentParsedDate = sdf.parse(dateStr)
                                currentParsedDate?.let { calendar.time = it }
                            } catch (e: Exception) {}

                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH)
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
                                val cal = Calendar.getInstance()
                                cal.set(selectedYear, selectedMonth, selectedDay)
                                dateStr = sdf.format(cal.time)
                            }, year, month, day).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dateStr, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.CalendarToday, contentDescription = "달력 열기")
                        }
                    }

                    // Memo input
                    OutlinedTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        placeholder = { Text("어디서 썼는지 메모해보세요 (예: 은혜네 부대찌개, 용산주유소)") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 16.sp),
                        maxLines = 2,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // 5. 저장하기 액션 버튼
            Button(
                onClick = {
                    val amountLong = amountStr.toLongOrNull() ?: 0L
                    if (amountLong <= 0) {
                        android.widget.Toast.makeText(context, "금액을 올바르게 입력해주세요 (예: 5000)", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Save
                    viewModel.saveExpense(
                        id = expenseId,
                        amount = amountLong,
                        category = selectedCategory,
                        paymentType = selectedPaymentType,
                        cardId = selectedCardId,
                        memo = memo.trim(),
                        date = dateStr
                    )

                    android.widget.Toast.makeText(context, if (expenseId == 0) "등록되었습니다" else "수정되었습니다", android.widget.Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (expenseId == 0) "지출 내역 저장하기" else "지출 내역 수정하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
