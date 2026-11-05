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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Zero-based status banner
        val leftToAssign = plan.leftToAssign
        val isZero = abs(leftToAssign) < 0.01
        val isOverAssigned = leftToAssign < -0.01

        val bannerBg = when {
            isZero -> Color(0xFF1B5E20).copy(alpha = 0.15f) // Muted Forest Green
            isOverAssigned -> Color(0xFFB71C1C).copy(alpha = 0.15f) // Soft Coral Red
            else -> Color(0xFFF57F17).copy(alpha = 0.15f) // Warm Amber
        }

        val bannerTextColor = when {
            isZero -> Color(0xFF4CAF50)
            isOverAssigned -> Color(0xFFEF5350)
            else -> Color(0xFFFFB300)
        }

        val bannerTitle = when {
            isZero -> "It's an EveryDollar budget!"
            isOverAssigned -> "${abs(leftToAssign).format(baseCurrency)} $baseCurrency over-assigned"
            else -> "${leftToAssign.format(baseCurrency)} $baseCurrency left to assign"
        }

        val bannerSubtitle = when {
            isZero -> "Every Rupee has a job."
            isOverAssigned -> "Reduce allocations to balance your budget."
            else -> "Assign remaining money to savings, investments, or expenses."
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bannerBg)
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bannerTitle,
                        style = UI.typo.b1.style(
                            color = bannerTextColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = bannerSubtitle,
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Income vs Planned Allocations row (tappable to edit planned income)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(UI.colors.medium.copy(alpha = 0.08f))
                .clickable { onEditIncome() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Planned Income",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${plan.totalIncome.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (plan.carryoverAmount > 0.0) {
                    Text(
                        text = "incl. ${plan.carryoverAmount.format(baseCurrency)} $baseCurrency carryover",
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
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${plan.totalAllocated.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "tap to edit income",
                    style = UI.typo.nC.style(
                        color = UI.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

