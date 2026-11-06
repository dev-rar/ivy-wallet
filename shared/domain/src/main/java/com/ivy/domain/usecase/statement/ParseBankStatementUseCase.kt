package com.ivy.domain.usecase.statement

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.statement.BankStatementProfile
import com.ivy.domain.model.statement.StatementTransaction
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

class ParseBankStatementUseCase @Inject constructor(
    private val detectDuplicateTransactionUseCase: DetectDuplicateTransactionUseCase,
    private val suggestBudgetItemUseCase: SuggestBudgetItemUseCase,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun parse(
        csvText: String,
        accountId: UUID? = null,
        period: BudgetPeriod = BudgetPeriod.current(),
        forcedProfile: BankStatementProfile? = null
    ): List<StatementTransaction> = withContext(dispatchers.io) {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.size < 2) return@withContext emptyList()

        val headerRow = parseCsvLine(lines.first())
        val profile = forcedProfile ?: BankStatementProfile.detectProfile(headerRow)

        val headerIndices = headerRow.mapIndexed { index, name ->
            name.trim().lowercase() to index
        }.toMap()

        val parsedTransactions = mutableListOf<StatementTransaction>()

        for (i in 1 until lines.size) {
            val rowValues = parseCsvLine(lines[i])
            if (rowValues.size < headerRow.size) continue

            val rawMap = headerRow.mapIndexed { index, col ->
                col to (rowValues.getOrNull(index) ?: "")
            }.toMap()

            // 1. Extract Date
            val dateStr = findValue(rowValues, headerIndices, profile.dateColumnNames)
            val instant = parseDate(dateStr, profile.dateFormat) ?: Instant.now()

            // 2. Extract Description
            val description = findValue(rowValues, headerIndices, profile.descriptionColumnNames)
                .ifBlank { "Bank Transaction" }

            // 3. Extract Amount & Type
            val (amount, type) = parseAmountAndType(rowValues, headerIndices, profile)
            if (amount <= 0.0) continue

            // 4. Check Duplicate
            val isDuplicate = detectDuplicateTransactionUseCase.isDuplicate(
                accountId = accountId,
                amount = amount,
                date = instant
            )

            // 5. Smart Suggestion
            val suggestion = suggestBudgetItemUseCase.suggest(description, period)

            parsedTransactions.add(
                StatementTransaction(
                    id = UUID.randomUUID(),
                    date = instant,
                    description = description,
                    amount = amount,
                    type = type,
                    isDuplicate = isDuplicate,
                    suggestedCategoryId = suggestion.categoryId,
                    suggestedBudgetItemId = suggestion.budgetItemId,
                    rawRow = rawMap
                )
            )
        }

        parsedTransactions
    }

    private fun parseAmountAndType(
        row: List<String>,
        indices: Map<String, Int>,
        profile: BankStatementProfile
    ): Pair<Double, TransactionType> {
        val debitStr = findValue(row, indices, profile.debitColumnNames)
        val creditStr = findValue(row, indices, profile.creditColumnNames)
        val amountStr = findValue(row, indices, profile.amountColumnNames)

        val debit = cleanNumber(debitStr)
        val credit = cleanNumber(creditStr)
        val amount = cleanNumber(amountStr)

        return when {
            debit != null && debit > 0.0 -> debit to TransactionType.EXPENSE
            credit != null && credit > 0.0 -> credit to TransactionType.INCOME
            amount != null -> {
                if (amount < 0.0) {
                    abs(amount) to TransactionType.EXPENSE
                } else {
                    amount to TransactionType.EXPENSE // Default single-amount column to expense
                }
            }
            else -> 0.0 to TransactionType.EXPENSE
        }
    }

    private fun cleanNumber(str: String): Double? {
        if (str.isBlank()) return null
        val cleaned = str.replace(",", "")
            .replace("PKR", "", ignoreCase = true)
            .replace("Rs.", "", ignoreCase = true)
            .replace("$", "")
            .trim()
        return cleaned.toDoubleOrNull()
    }

    private fun findValue(row: List<String>, indices: Map<String, Int>, candidateNames: List<String>): String {
        for (candidate in candidateNames) {
            val idx = indices[candidate.lowercase()]
            if (idx != null && idx < row.size) {
                val value = row[idx].trim()
                if (value.isNotBlank()) return value
            }
        }
        return ""
    }

    private fun parseDate(dateStr: String, defaultPattern: String): Instant? {
        if (dateStr.isBlank()) return null
        val patterns = listOf(
            defaultPattern,
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "MM/dd/yyyy"
        )
        for (p in patterns) {
            val parsed = runCatching {
                val formatter = DateTimeFormatter.ofPattern(p)
                val localDate = LocalDate.parse(dateStr, formatter)
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return runCatching { Instant.parse(dateStr) }.getOrNull()
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        val sb = StringBuilder()

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }
}

