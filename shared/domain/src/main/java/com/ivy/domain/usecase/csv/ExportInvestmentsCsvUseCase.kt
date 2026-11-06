package com.ivy.domain.usecase.csv

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.model.AccountId
import com.ivy.data.model.AccountType
import com.ivy.data.repository.InvestmentRepository
import kotlinx.coroutines.withContext
import org.apache.commons.text.StringEscapeUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class ExportInvestmentsCsvUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val investmentRepository: InvestmentRepository,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun exportCsv(): String = withContext(dispatchers.io) {
        val accounts = accountDao.findAll().filter {
            !it.isDeleted && it.accountType == AccountType.INVESTMENT.name
        }

        buildString {
            append(COLUMNS.joinToString(separator = CSV_SEPARATOR))
            append(NEWLINE)

            for (acc in accounts) {
                val valuations = investmentRepository.getValuations(AccountId(acc.id))
                for (v in valuations) {
                    val dateStr = v.valuationDate
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)

                    val row = listOf(
                        acc.name.escapeCsv(),
                        dateStr,
                        v.valuationAmount.csvFormat(),
                        (v.notes ?: "").escapeCsv()
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
            "Account",
            "Valuation Date",
            "Total Valuation",
            "Notes"
        )
    }
}

