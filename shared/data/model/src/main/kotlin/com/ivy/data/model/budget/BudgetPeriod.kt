package com.ivy.data.model.budget

import java.time.YearMonth
import java.time.format.DateTimeFormatter

@JvmInline
value class BudgetPeriod(val value: String) : Comparable<BudgetPeriod> {
    init {
        require(REGEX.matches(value)) { "BudgetPeriod must match YYYY-MM format, got: $value" }
    }

    val year: Int
        get() = value.substring(0, 4).toInt()

    val month: Int
        get() = value.substring(5, 7).toInt()

    fun toYearMonth(): YearMonth = YearMonth.of(year, month)

    fun next(): BudgetPeriod = from(toYearMonth().plusMonths(1))

    fun previous(): BudgetPeriod = from(toYearMonth().minusMonths(1))

    override fun compareTo(other: BudgetPeriod): Int {
        return value.compareTo(other.value)
    }

    override fun toString(): String = value

    companion object {
        private val REGEX = Regex("""^\d{4}-(0[1-9]|1[0-2])$""")

        fun of(year: Int, month: Int): BudgetPeriod {
            return from(YearMonth.of(year, month))
        }

        fun parseOrNull(value: String): BudgetPeriod? {
            return if (REGEX.matches(value)) BudgetPeriod(value) else null
        }

        fun from(yearMonth: YearMonth): BudgetPeriod {
            return BudgetPeriod(yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
        }

        fun current(): BudgetPeriod {
            return from(YearMonth.now())
        }
    }
}
