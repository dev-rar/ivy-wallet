package com.ivy.reports.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.ivy.legacy.utils.format
import com.ivy.reports.model.YearlyReportState
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfStatementExporter {

    fun exportToOutputStream(
        state: YearlyReportState,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 portrait
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val currency = state.baseCurrency

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1B1D24")
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#5C6475")
            textSize = 11f
            isAntiAlias = true
        }
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#14CC9E")
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#222632")
            textSize = 11f
            isAntiAlias = true
        }
        val boldBodyPaint = Paint().apply {
            color = Color.parseColor("#1B1D24")
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E4EB")
            strokeWidth = 1f
        }

        var y = 48f
        val left = 40f
        val right = 555f

        canvas.drawText("CashPulse — Financial & Accounting Statement (${state.year})", left, y, titlePaint)
        y += 18f
        val generated = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated: $generated • Base Currency: $currency", left, y, subtitlePaint)
        y += 16f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 24f

        // 1. Annual Profit & Loss / Cash Flow
        canvas.drawText("1. ANNUAL CASH FLOW & P&L SUMMARY", left, y, sectionPaint)
        y += 18f
        canvas.drawText("Total Income: ${state.totalAnnualIncome.format(currency)} $currency", left, y, bodyPaint)
        y += 16f
        canvas.drawText("Total Expenses: ${state.totalAnnualExpenses.format(currency)} $currency", left, y, bodyPaint)
        y += 16f
        val netPrefix = if (state.netAnnualSavings >= 0) "+" else ""
        canvas.drawText(
            "Net Cash Flow / Savings: $netPrefix${state.netAnnualSavings.format(currency)} $currency (${String.format("%.1f", state.annualSavingsRate)}%)",
            left,
            y,
            boldBodyPaint
        )
        y += 24f

        // 2. Balance Sheet
        val bs = state.balanceSheet
        canvas.drawText("2. BALANCE SHEET (ASSETS vs. LIABILITIES)", left, y, sectionPaint)
        y += 18f
        canvas.drawText("Cash & Bank Assets: ${bs.cashAndBankAssets.format(currency)} $currency", left, y, bodyPaint)
        canvas.drawText("Credit & Overdrafts: ${bs.creditAndOverdraftLiabilities.format(currency)} $currency", 310f, y, bodyPaint)
        y += 16f
        canvas.drawText("Accounts Receivable (Lent): ${bs.accountsReceivable.format(currency)} $currency", left, y, bodyPaint)
        canvas.drawText("Accounts Payable (Borrowed): ${bs.accountsPayable.format(currency)} $currency", 310f, y, bodyPaint)
        y += 16f
        canvas.drawText("Total Assets: ${bs.totalAssets.format(currency)} $currency", left, y, boldBodyPaint)
        canvas.drawText("Total Liabilities: ${bs.totalLiabilities.format(currency)} $currency", 310f, y, boldBodyPaint)
        y += 18f
        val worthPrefix = if (bs.netWorth >= 0) "+" else ""
        canvas.drawText(
            "Net Worth (Equity): $worthPrefix${bs.netWorth.format(currency)} $currency  •  Debt-to-Asset Ratio: ${String.format("%.1f", bs.debtToAssetRatio)}%",
            left,
            y,
            boldBodyPaint
        )
        y += 24f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 24f

        // 3. Selected Month Category Breakdown
        val selectedMonth = state.monthlyData.getOrNull(state.selectedMonthIndex)
        if (selectedMonth != null) {
            canvas.drawText("3. ${selectedMonth.monthName.uppercase(Locale.getDefault())} CATEGORY BREAKDOWN", left, y, sectionPaint)
            y += 18f
            canvas.drawText(
                "Month Income: +${selectedMonth.income.format(currency)} $currency  |  Expenses: -${selectedMonth.expenses.format(currency)} $currency  |  Budget: ${selectedMonth.totalBudgeted.format(currency)} $currency",
                left,
                y,
                boldBodyPaint
            )
            y += 20f

            canvas.drawText("Category", left, y, boldBodyPaint)
            canvas.drawText("Spent / Income", 270f, y, boldBodyPaint)
            canvas.drawText("Budget", 440f, y, boldBodyPaint)
            y += 12f
            canvas.drawLine(left, y, right, y, linePaint)
            y += 16f

            for (cat in selectedMonth.categoryBudgets) {
                if (y > 790f) break
                val amountLabel = when {
                    cat.incomeAmount > cat.spentAmount && cat.incomeAmount > 0 ->
                        "+${cat.incomeAmount.format(currency)} $currency"
                    else -> "-${cat.spentAmount.format(currency)} $currency"
                }
                val budgetLabel = if (cat.hasBudget) "${cat.budgetAmount.format(currency)} $currency" else "—"
                canvas.drawText(cat.categoryName.take(28), left, y, bodyPaint)
                canvas.drawText(amountLabel, 270f, y, bodyPaint)
                canvas.drawText(budgetLabel, 440f, y, bodyPaint)
                y += 16f
            }
        }

        document.finishPage(page)
        outputStream.use { stream ->
            document.writeTo(stream)
        }
        document.close()
    }
}

