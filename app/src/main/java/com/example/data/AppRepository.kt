package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class AppRepository(private val appDao: AppDao) {

    // --- Budgets ---
    fun getAllBudgetsFlow(): Flow<List<Budget>> = appDao.getAllBudgetsFlow()
    
    suspend fun getBudgetForMonth(month: String): Budget? {
        return appDao.getBudgetForMonth(month)
    }

    suspend fun saveBudget(month: String, amount: Long) {
        appDao.insertBudget(Budget(month, amount))
    }

    // --- Expenses ---
    fun getAllExpensesFlow(): Flow<List<Expense>> = appDao.getAllExpensesFlow()

    fun getExpensesForMonthFlow(month: String): Flow<List<Expense>> {
        return appDao.getExpensesForMonthFlow(month)
    }

    suspend fun getExpenseById(id: Int): Expense? = appDao.getExpenseById(id)

    suspend fun insertExpense(expense: Expense) = appDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = appDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = appDao.deleteExpense(expense)

    // --- Cards ---
    fun getAllCardsFlow(): Flow<List<CardInfo>> = appDao.getAllCardsFlow()
    
    fun getActiveCardsFlow(): Flow<List<CardInfo>> = appDao.getActiveCardsFlow()

    suspend fun getCardById(id: Int): CardInfo? = appDao.getCardById(id)

    suspend fun saveCard(card: CardInfo) {
        if (card.id == 0) {
            appDao.insertCard(card)
        } else {
            appDao.updateCard(card)
        }
    }

    suspend fun deleteCard(card: CardInfo) = appDao.deleteCard(card)

    // --- Fixed Expenses ---
    fun getAllFixedExpensesFlow(): Flow<List<FixedExpense>> = appDao.getAllFixedExpensesFlow()

    suspend fun saveFixedExpense(fixedExpense: FixedExpense) {
        if (fixedExpense.id == 0) {
            appDao.insertFixedExpense(fixedExpense)
        } else {
            appDao.updateFixedExpense(fixedExpense)
        }
    }

    suspend fun deleteFixedExpense(fixedExpense: FixedExpense) = appDao.deleteFixedExpense(fixedExpense)

    /**
     * Automatically applies active fixed expenses to a specific month's history.
     * Prevents duplication by checking if an expense with the prefix "[고정비] Title" already exists.
     */
    suspend fun applyFixedExpensesForMonth(month: String) {
        val fixedExpenses = appDao.getAllFixedExpensesFlow().firstOrNull() ?: return
        val existingExpenses = appDao.getExpensesForMonthFlow(month).firstOrNull() ?: emptyList()

        for (fixed in fixedExpenses) {
            if (!fixed.isActive || !fixed.autoApply) continue

            // Unique signature: starts with "[고정비]" followed by title
            val memoSignature = "[고정비] ${fixed.title}"
            val alreadyApplied = existingExpenses.any { it.memo == memoSignature }

            if (!alreadyApplied) {
                // Determine day format
                val paddedDay = fixed.paymentDay.toString().padStart(2, '0')
                val dateStr = "$month-$paddedDay"

                val expense = Expense(
                    amount = fixed.amount,
                    category = fixed.category,
                    paymentType = "TRANSFER", // Fixed expenditures are typically transfer/auto-debits
                    cardId = null,
                    memo = memoSignature,
                    date = dateStr,
                    createdAt = System.currentTimeMillis()
                )
                appDao.insertExpense(expense)
            }
        }
    }
}
