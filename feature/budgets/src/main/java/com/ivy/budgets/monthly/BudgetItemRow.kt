package com.ivy.budgets.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.budget.BillStatus
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.legacy.utils.format
import com.ivy.wallet.ui.theme.components.ProgressBar

@Composable
fun BudgetItemRow(
    item: MonthlyItemSummary,
    baseCurrency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(UI.colors.medium.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category color indicator dot
            val dotColor = item.categoryColor?.let { Color(it) } ?: UI.colors.primary
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = UI.typo.b1.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                if (item.categoryName != null) {
                    Text(
                        text = item.categoryName ?: "",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.plannedAmount.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Bold
                    )
                )

                val remaining = item.remainingAmount
                val remainingText = if (remaining >= 0) {
                    "${remaining.format(baseCurrency)} $baseCurrency left"
                } else {
                    "${(-remaining).format(baseCurrency)} $baseCurrency over"
                }
                val remainingColor = if (remaining >= 0) {
                    UI.colors.pureInverse.copy(alpha = 0.6f)
                } else {
                    Color(0xFFEF5350)
                }

                Text(
                    text = "${item.actualAmount.format(baseCurrency)} spent • $remainingText",
                    style = UI.typo.c.style(
                        color = remainingColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        if (item.bills.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val overdueCount = item.bills.count { it.status == BillStatus.OVERDUE }
                val dueSoonCount = item.bills.count { it.status == BillStatus.DUE_SOON }
                val paidCount = item.bills.count { it.status == BillStatus.PAID }

                val (badgeText, badgeColor) = when {
                    overdueCount > 0 -> "⚠️ $overdueCount Bill Overdue" to Color(0xFFEF5350)
                    dueSoonCount > 0 -> "⚡ $dueSoonCount Bill Due Soon" to Color(0xFFFFA000)
                    paidCount == item.bills.size -> "✓ ${item.bills.size} Bill(s) Paid" to Color(0xFF4CAF50)
                    else -> "📅 ${item.bills.size} Scheduled Bill(s)" to UI.colors.pureInverse.copy(alpha = 0.6f)
                }

                Text(
                    text = badgeText,
                    style = UI.typo.c.style(
                        color = badgeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        val fundProg = item.sinkingFundProgress
        if (fundProg != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val percentInt = (fundProg.progressPercent * 100).toInt()
                val targetText = "${fundProg.currentSavedAmount.format(baseCurrency)} / ${fundProg.targetAmount.format(baseCurrency)} $baseCurrency ($percentInt%)"
                Text(
                    text = "🎯 $targetText",
                    style = UI.typo.c.style(
                        color = UI.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(Modifier.weight(1f))

                if (fundProg.isFullyFunded) {
                    Text(
                        text = "✓ Funded",
                        style = UI.typo.c.style(
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else if (!fundProg.isSolvent) {
                    Text(
                        text = "⚠️ Cash Shortage",
                        style = UI.typo.c.style(
                            color = Color(0xFFFFA000),
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    val monthlyNeeded = fundProg.monthlyContributionNeeded
                    val monthsRemaining = fundProg.monthsRemaining
                    if (monthlyNeeded != null && monthsRemaining != null) {
                        Text(
                            text = "~${monthlyNeeded.format(baseCurrency)}/mo (${monthsRemaining}m)",
                            style = UI.typo.c.style(
                                color = UI.colors.pureInverse.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            ProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                percent = fundProg.progressPercent.toDouble(),
                positiveProgress = true
            )
        } else {
            Spacer(Modifier.height(8.dp))

            ProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                percent = item.progress.toDouble(),
                positiveProgress = false
            )
        }
    }
}
