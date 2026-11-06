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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.report.PlannedVsActualReport
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalCheck
import com.ivy.wallet.ui.theme.modal.ModalTitle
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.PlannedVsActualModal(
    visible: Boolean,
    report: PlannedVsActualReport?,
    baseCurrency: String,
    onExportCsv: () -> Unit,
    dismiss: () -> Unit
) {
    if (!visible || report == null) return

    val modalId = remember(visible) { UUID.randomUUID() }

    IvyModal(
        id = modalId,
        visible = true,
        dismiss = dismiss,
        PrimaryAction = {
            ModalCheck(
                label = "Done"
            ) {
                dismiss()
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
            ModalTitle(text = "Planned vs Actual")

            // Export CSV button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(UI.colors.primary.copy(alpha = 0.12f))
                    .clickable { onExportCsv() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IvyIcon(
                    icon = R.drawable.ic_export_csv,
                    tint = UI.colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Export CSV",
                    style = UI.typo.c.style(
                        color = UI.colors.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Variance analysis for ${report.period.toDisplayMonthYear()}.",
            style = UI.typo.b2.style(
                color = UI.colors.pureInverse.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Net Summary Card
        val isNetPositive = report.netActual >= 0.0
        val netBg = if (isNetPositive) Color(0xFF1B5E20).copy(alpha = 0.15f) else Color(0xFFB71C1C).copy(alpha = 0.15f)
        val netColor = if (isNetPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)
        val sign = if (isNetPositive) "+" else ""

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(netBg)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Net Cash Surplus / (Deficit)",
                    style = UI.typo.c.style(
                        color = netColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$sign${report.netActual.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.h2.style(
                        color = netColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Planned net: ${report.netPlanned.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Income vs Expenses Comparison Row
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
                        text = "Income",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${report.totalActualIncome.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.b1.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Plan: ${report.totalPlannedIncome.format(baseCurrency)}",
                        style = UI.typo.nC.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal
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
                        text = "Expenses",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${report.totalActualExpenses.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.b1.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Plan: ${report.totalPlannedExpenses.format(baseCurrency)}",
                        style = UI.typo.nC.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Group-by-Group breakdown
        for (group in report.groups) {
            if (group.items.isEmpty()) continue

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.medium.copy(alpha = 0.05f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.name,
                        style = UI.typo.b1.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    val groupVarText = if (group.variance >= 0) {
                        "+${group.variance.format(baseCurrency)}"
                    } else {
                        group.variance.format(baseCurrency)
                    }
                    val groupVarColor = if (group.variance >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)

                    Text(
                        text = "$groupVarText $baseCurrency",
                        style = UI.typo.c.style(
                            color = groupVarColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                for (item in group.items) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = UI.typo.b2.style(
                                    color = UI.colors.pureInverse,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Planned: ${item.plannedAmount.format(baseCurrency)} | Actual: ${item.actualAmount.format(baseCurrency)}",
                                style = UI.typo.nC.style(
                                    color = UI.colors.pureInverse.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }

                        val itemColor = if (item.isOverspent) Color(0xFFEF5350) else Color(0xFF4CAF50)
                        val itemLabel = if (item.isOverspent) {
                            "-${(-item.variance).format(baseCurrency)}"
                        } else {
                            "+${item.variance.format(baseCurrency)}"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(itemColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = itemLabel,
                                style = UI.typo.c.style(
                                    color = itemColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(32.dp))
    }
}

