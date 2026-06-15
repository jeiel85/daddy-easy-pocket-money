package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Budget Queries ---
    @Query("SELECT * FROM budgets WHERE month = :month LIMIT 1")
    suspend fun getBudgetForMonth(month: String): Budget?

    @Query("SELECT * FROM budgets")
    fun getAllBudgetsFlow(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    // --- Expense Queries ---
    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, createdAt DESC")
    fun getExpensesForMonthFlow(monthPrefix: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseById(id: Int): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // --- Card Queries ---
    @Query("SELECT * FROM cards ORDER BY id ASC")
    fun getAllCardsFlow(): Flow<List<CardInfo>>

    @Query("SELECT * FROM cards WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveCardsFlow(): Flow<List<CardInfo>>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Int): CardInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardInfo)

    @Update
    suspend fun updateCard(card: CardInfo)

    @Delete
    suspend fun deleteCard(card: CardInfo)

    // --- Fixed Expense Queries ---
    @Query("SELECT * FROM fixed_expenses ORDER BY paymentDay ASC")
    fun getAllFixedExpensesFlow(): Flow<List<FixedExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedExpense(fixedExpense: FixedExpense)

    @Update
    suspend fun updateFixedExpense(fixedExpense: FixedExpense)

    @Delete
    suspend fun deleteFixedExpense(fixedExpense: FixedExpense)
}
