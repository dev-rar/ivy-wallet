package com.ivy.domain.usecase.csv

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.usecase.budget.GetMonthlyPlanSummaryUseCase
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

class ExportBudgetPlanCsvUseCase @Inject constructor(
    private val getMonthlyPlanSummaryUseCase: GetMonthlyPlanSummaryUseCase,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun exportCsv(period: BudgetPeriod): String = withContext(dispatchers.io) {
        val summary = getMonthlyPlanSummaryUseCase(period)

        buildString {
            // Header
            append(COLUMNS.joinToString(separator = CSV_SEPARATOR))
            append(NEWLINE)

            for (group in summary.groups) {
                for (item in group.items) {
                    val progressPct = (item.progress * 100).toDouble().csvFormat()
                    val row = listOf(
                        period.value.escapeCsv(),
                        group.name.escapeCsv(),
                        item.name.escapeCsv(),
                        item.itemType.name.escapeCsv(),
                        item.plannedAmount.csvFormat(),
                        item.actualAmount.csvFormat(),
                        item.remainingAmount.csvFormat(),
                        "$progressPct%"
                    )
                    append(row.joinToString(separator = CSV_SEPARATOR))
                    append(NEWLINE)
                }
            }
        }
    }

    private fun String.escapeCsv(): String = try {
        StringEscapeUtils.escapeCsv(this).replace("\\", "")
    } catch (e: Exception) {
        replace("\\", "")
    }

    private fun Double.csvFormat(): String = DecimalFormat(NUMBER_FORMAT).apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    }.format(this)

    companion object {
        private const val CSV_SEPARATOR = ","
        private const val NEWLINE = "\n"
        private const val NUMBER_FORMAT = "#,##0.00"

        val COLUMNS = listOf(
            "Period",
            "Group",
            "Item",
            "Type",
            "Planned Amount",
            "Actual Amount",
            "Remaining Amount",
            "Progress"
        )
    }
}

