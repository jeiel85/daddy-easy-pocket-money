package com.jeiel85.daddypocket

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.jeiel85.daddypocket.data.AppDatabase
import com.jeiel85.daddypocket.data.AppRepository
import com.jeiel85.daddypocket.data.FixedExpense
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the real fixed-expense auto-apply logic of [AppRepository] against an
 * in-memory Room database: an active fixed expense is materialized into the month's
 * history exactly once and is not duplicated on repeated month syncs.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppRepositoryTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: AppRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = AppRepository(db.appDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun fixedExpense_appliedOnce_andNotDuplicatedOnRepeatedSync() = runTest {
    val month = "2026-06"
    repository.saveFixedExpense(
      FixedExpense(
        title = "휴대폰 요금",
        amount = 65_000L,
        category = "통신비",
        paymentDay = 25,
        isActive = true,
        autoApply = true
      )
    )

    // Two syncs of the same month must not create two ledger rows.
    repository.applyFixedExpensesForMonth(month)
    repository.applyFixedExpensesForMonth(month)

    val applied = repository.getExpensesForMonthFlow(month).first()
      .filter { it.memo == "[고정비] 휴대폰 요금" }

    assertEquals(1, applied.size)
    assertEquals(65_000L, applied.first().amount)
    assertEquals("통신비", applied.first().category)
  }

  @Test
  fun inactiveFixedExpense_isNotApplied() = runTest {
    val month = "2026-06"
    repository.saveFixedExpense(
      FixedExpense(
        title = "넷플릭스",
        amount = 17_000L,
        category = "구독료",
        paymentDay = 15,
        isActive = false,
        autoApply = true
      )
    )

    repository.applyFixedExpensesForMonth(month)

    val applied = repository.getExpensesForMonthFlow(month).first()
      .filter { it.memo == "[고정비] 넷플릭스" }

    assertEquals(0, applied.size)
  }
}
