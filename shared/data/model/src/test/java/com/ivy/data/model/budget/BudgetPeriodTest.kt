package com.ivy.data.model.budget

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test

class BudgetPeriodTest {
    @Test
    fun `parse valid period`() {
        val period = BudgetPeriod.parseOrNull("2026-11")
        period shouldNotBe null
        period!!.year shouldBe 2026
        period.month shouldBe 11
        period.toString() shouldBe "2026-11"
    }

    @Test
    fun `parse invalid period formats`() {
        BudgetPeriod.parseOrNull("2026") shouldBe null
        BudgetPeriod.parseOrNull("2026-1") shouldBe null
        BudgetPeriod.parseOrNull("2026-13") shouldBe null
        BudgetPeriod.parseOrNull("2026-00") shouldBe null
        BudgetPeriod.parseOrNull("invalid") shouldBe null
    }

    @Test
    fun `navigation previous and next`() {
        val nov = BudgetPeriod.of(2026, 11)
        nov.next() shouldBe BudgetPeriod.of(2026, 12)
        nov.previous() shouldBe BudgetPeriod.of(2026, 10)

        val dec = BudgetPeriod.of(2026, 12)
        dec.next() shouldBe BudgetPeriod.of(2027, 1)

        val jan = BudgetPeriod.of(2026, 1)
        jan.previous() shouldBe BudgetPeriod.of(2025, 12)
    }

    @Test
    fun `ordering and comparison`() {
        val p1 = BudgetPeriod.of(2025, 12)
        val p2 = BudgetPeriod.of(2026, 1)
        val p3 = BudgetPeriod.of(2026, 2)

        (p1 < p2) shouldBe true
        (p2 < p3) shouldBe true
        (p3 > p1) shouldBe true
    }
}
