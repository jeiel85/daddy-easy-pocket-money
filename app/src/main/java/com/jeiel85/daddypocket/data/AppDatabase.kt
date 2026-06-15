package com.jeiel85.daddypocket.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Budget::class, Expense::class, CardInfo::class, FixedExpense::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daddy_pocket_db"
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate initial cards for 40-50s convenience
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.appDao()
                    dao.insertCard(CardInfo(name = "국민카드", paymentDay = 14, isActive = true))
                    dao.insertCard(CardInfo(name = "신한카드", paymentDay = 25, isActive = true))
                    dao.insertCard(CardInfo(name = "삼성카드", paymentDay = 10, isActive = true))
                    dao.insertCard(CardInfo(name = "현대카드", paymentDay = 12, isActive = true))
                    
                    // Prepopulate some default fixed expenses (example info)
                    dao.insertFixedExpense(FixedExpense(title = "휴대폰 요금", amount = 65000, category = "통신비", paymentDay = 25, isActive = true))
                    dao.insertFixedExpense(FixedExpense(title = "넷플릭스", amount = 17000, category = "구독료", paymentDay = 15, isActive = true))
                    dao.insertFixedExpense(FixedExpense(title = "자동차 보험료", amount = 58000, category = "보험", paymentDay = 5, isActive = true))
                }
            }
        }
    }
}
