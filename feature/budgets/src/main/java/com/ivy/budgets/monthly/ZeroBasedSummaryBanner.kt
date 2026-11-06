package com.ivy.budgets.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.ivy.domain.model.budget.MonthlyPlanSummary
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon
import kotlin.math.abs

@Composable
fun ZeroBasedSummaryBanner(
    plan: MonthlyPlanSummary,
    baseCurrency: String,
    onEditIncome: () -> Unit,
    onReviewMonth: () -> Unit = {},
    onViewReport: () -> Unit = {},
    onImportStatement: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        val leftToAssign = plan.leftToAssign
        val isZero = abs(leftToAssign) < 0.01
        val isOverAssigned = leftToAssign < -0.01

        val bannerBg = when {
            isZero -> Color(0xFF1B5E20).copy(alpha = 0.12f)
            isOverAssigned -> Color(0xFFB71C1C).copy(alpha = 0.12f)
            else -> Color(0xFFF57F17).copy(alpha = 0.12f)
        }

        val bannerTextColor = when {
            isZero -> Color(0xFF4CAF50)
            isOverAssigned -> Color(0xFFEF5350)
            else -> Color(0xFFFFB300)
        }

        val statusBadgeText = when {
            isZero -> "✓ Balanced"
            isOverAssigned -> "⚠️ Over Budget"
            else -> "To Allocate"
        }

        val headlineAmount = when {
            isZero -> "Zero-Based Balanced"
            isOverAssigned -> "${abs(leftToAssign).format(baseCurrency)} $baseCurrency over"
            else -> "${leftToAssign.format(baseCurrency)} $baseCurrency remaining"
        }

        val subtitle = when {
            isZero -> "All planned income has been allocated."
            isOverAssigned -> "Planned allocations exceed monthly income."
            else -> "Assign remaining funds across your categories."
        }

        // Unified Status & Income Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bannerBg)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = headlineAmount,
                    style = UI.typo.b1.style(
                        color = bannerTextColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bannerTextColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusBadgeText,
                        style = UI.typo.c.style(
                            color = bannerTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = UI.typo.c.style(
                    color = UI.colors.pureInverse.copy(alpha = 0.65f),
                    fontWeight = FontWeight.Normal
                )
            )

            Spacer(Modifier.height(12.dp))

            // Income & Planned strip (clickable to edit income)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(UI.colors.pure.copy(alpha = 0.5f))
                    .clickable { onEditIncome() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Planned Income",
                        style = UI.typo.nC.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "${plan.totalIncome.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.b2.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (plan.carryoverAmount > 0.0) {
                        Text(
                            text = "+${plan.carryoverAmount.format(baseCurrency)} $baseCurrency carryover",
                            style = UI.typo.nC.style(
                                color = UI.colors.pureInverse.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Total Planned",
                        style = UI.typo.nC.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "${plan.totalAllocated.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.b2.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "tap to edit ✎",
                        style = UI.typo.nC.style(
                            color = UI.colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Compact Action Pills (single row: Variance, Import CSV, Close Month)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Variance Report
            CompactActionButton(
                icon = R.drawable.ic_vue_chart_chart,
                label = "Variance",
                tint = UI.colors.pureInverse,
                bg = UI.colors.medium.copy(alpha = 0.08f),
                onClick = onViewReport,
                modifier = Modifier.weight(1f)
            )

            // Import Bank Statement CSV
            CompactActionButton(
                icon = R.drawable.ic_custom_account_s,
                label = "Import",
                tint = UI.colors.primary,
                bg = UI.colors.primary.copy(alpha = 0.1f),
                onClick = onImportStatement,
                modifier = Modifier.weight(1f)
            )

            // Close Month / Closed
            CompactActionButton(
                icon = if (plan.isClosed) R.drawable.ic_check else R.drawable.ic_calendar,
                label = if (plan.isClosed) "Closed ✓" else "Close",
                tint = if (plan.isClosed) Color(0xFF4CAF50) else UI.colors.primary,
                bg = if (plan.isClosed) Color(0xFF2E7D32).copy(alpha = 0.12f) else UI.colors.medium.copy(alpha = 0.08f),
                onClick = onReviewMonth,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: Int,
    label: String,
    tint: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IvyIcon(
            icon = icon,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = UI.typo.c.style(
                color = tint,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1
        )
    }
}
