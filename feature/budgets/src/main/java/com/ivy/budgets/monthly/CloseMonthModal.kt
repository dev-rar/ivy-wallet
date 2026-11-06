package com.ivy.budgets.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.budget.MonthCloseSummary
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalCheck
import com.ivy.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.CloseMonthModal(
    visible: Boolean,
    summary: MonthCloseSummary?,
    baseCurrency: String,
    onCloseMonth: (rolloverSurplus: Boolean) -> Unit,
    onReopenMonth: () -> Unit,
    dismiss: () -> Unit
) {
    if (!visible || summary == null) return

    val modalId = remember(visible) { UUID.randomUUID() }
    var rollover by remember(visible) { mutableStateOf(true) }

    IvyModal(
        id = modalId,
        visible = true,
        dismiss = dismiss,
        PrimaryAction = {
            if (!summary.isClosed) {
                ModalCheck(
                    label = "Close Month"
                ) {
                    onCloseMonth(rollover)
                    dismiss()
                }
            } else {
                ModalCheck(
                    label = "Done"
                ) {
                    dismiss()
                }
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModalTitle(text = "Month Review")

            val statusBg = if (summary.isClosed) Color(0xFF2E7D32).copy(alpha = 0.15f) else UI.colors.primary.copy(alpha = 0.15f)
            val statusColor = if (summary.isClosed) Color(0xFF4CAF50) else UI.colors.primary

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (summary.isClosed) "Closed ✓" else "Open",
                    style = UI.typo.c.style(
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Review income, spending, and surplus for ${summary.period.toDisplayMonthYear()}.",
            style = UI.typo.b2.style(
                color = UI.colors.pureInverse.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Net Surplus Card
        val isSurplus = summary.netSurplus >= 0.0
        val surplusBg = if (isSurplus) Color(0xFF1B5E20).copy(alpha = 0.15f) else Color(0xFFB71C1C).copy(alpha = 0.15f)
        val surplusTextColor = if (isSurplus) Color(0xFF4CAF50) else Color(0xFFEF5350)
        val surplusSign = if (isSurplus) "+" else ""

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(surplusBg)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = if (isSurplus) "Net Surplus / Savings" else "Budget Deficit",
                    style = UI.typo.c.style(
                        color = surplusTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$surplusSign${summary.netSurplus.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.h2.style(
                        color = surplusTextColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isSurplus) {
                        "Congratulations! You lived within your means this month."
                    } else {
                        "Spending exceeded income. Adjust allocations or cover from reserves."
                    },
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Income vs Spent Breakdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.medium.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Total Income",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalIncome.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.b1.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.medium.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${summary.totalSpent.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.b1.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Unspent & Overspent categories stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.medium.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Under Budget",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${summary.unspentCategoriesCount} items",
                        style = UI.typo.b2.style(
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.medium.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Over Budget",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${summary.overspentCategoriesCount} items",
                        style = UI.typo.b2.style(
                            color = if (summary.overspentCategoriesCount > 0) Color(0xFFEF5350) else UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Rollover option if not closed and surplus > 0
        if (!summary.isClosed && summary.netSurplus > 0.0) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.medium.copy(alpha = 0.08f))
                    .clickable { rollover = !rollover }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (rollover) UI.colors.primary else UI.colors.medium.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rollover) {
                            IvyIcon(
                                icon = R.drawable.ic_check,
                                tint = UI.colors.pure,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Roll surplus into next month",
                            style = UI.typo.b2.style(
                                color = UI.colors.pureInverse,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Transfers +${summary.netSurplus.format(baseCurrency)} $baseCurrency to ${summary.period.next().toDisplayMonthYear()}'s carryover.",
                            style = UI.typo.c.style(
                                color = UI.colors.pureInverse.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }

        // Reopen button if closed
        if (summary.isClosed) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFB71C1C).copy(alpha = 0.12f))
                    .clickable {
                        onReopenMonth()
                        dismiss()
                    }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reopen Month for Adjustments",
                    style = UI.typo.b2.style(
                        color = Color(0xFFEF5350),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

