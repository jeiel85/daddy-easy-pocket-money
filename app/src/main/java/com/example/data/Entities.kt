package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val month: String, // Format: "yyyy-MM"
    val amount: Long
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Long,
    val category: String,
    val paymentType: String, // "CASH", "CHECK_CARD", "CREDIT_CARD", "TRANSFER"
    val cardId: Int? = null,
    val memo: String,
    val date: String, // Format: "yyyy-MM-dd"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cards")
data class CardInfo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val paymentDay: Int, // e.g., 14 for 14th of the month
    val isActive: Boolean = true
)

@Entity(tableName = "fixed_expenses")
data class FixedExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Long,
    val category: String,
    val paymentDay: Int, // e.g., 25 for 25th of the month
    val isActive: Boolean = true,
    val autoApply: Boolean = true // Whether to automatically apply to history
)
