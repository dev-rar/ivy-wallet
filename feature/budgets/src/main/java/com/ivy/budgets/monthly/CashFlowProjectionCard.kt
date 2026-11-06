package com.ivy.budgets.monthly

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.cashflow.CashFlowProjection
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon

@Composable
fun CashFlowProjectionCard(
    projection: CashFlowProjection,
    baseCurrency: String,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(UI.colors.medium.copy(alpha = 0.08f))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        // Top header row: Title, status badge, expand chevron
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IvyIcon(
                icon = R.drawable.ic_wallet,
                tint = UI.colors.pureInverse,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = "Cash Forecast",
                style = UI.typo.b2.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.weight(1f))

            val statusColor = if (projection.hasShortfall) Color(0xFFEF5350) else Color(0xFF4CAF50)
            val statusText = if (projection.hasShortfall) "Shortfall Risk" else "Adequate"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusText,
                    style = UI.typo.c.style(
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.width(6.dp))

            IvyIcon(
                icon = R.drawable.ic_expandarrow,
                tint = UI.colors.pureInverse.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Compact 2-column metrics (never overflows)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Lowest Buffer",
                    style = UI.typo.nC.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Medium
                    )
                )
                val minColor = if (projection.minimumBalance < 0) Color(0xFFEF5350) else UI.colors.pureInverse
                Text(
                    text = "${projection.minimumBalance.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = minColor, fontWeight = FontWeight.Bold)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Ending Projected",
                    style = UI.typo.nC.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "${projection.endingCash.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Shortfall warning banner (always prominent if shortfall exists)
        if (projection.hasShortfall && projection.shortfallDate != null) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEF5350).copy(alpha = 0.12f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "⚠️ Shortage expected around ${projection.shortfallDate}. Incoming buffer needed.",
                    style = UI.typo.c.style(
                        color = Color(0xFFEF5350),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Expandable timeline & starting balance
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Starting Cash",
                        style = UI.typo.c.style(color = UI.colors.pureInverse.copy(alpha = 0.55f))
                    )
                    Text(
                        text = "${projection.startingCash.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.c.style(color = UI.colors.pureInverse, fontWeight = FontWeight.SemiBold)
                    )
                }

                if (projection.events.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Upcoming Timeline",
                        style = UI.typo.nC.style(
                            color = UI.colors.pureInverse.copy(alpha = 0.5f),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(4.dp))

                    for (event in projection.events.take(3)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${event.date.dayOfMonth}th",
                                style = UI.typo.c.style(
                                    color = UI.colors.pureInverse.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = event.title,
                                style = UI.typo.c.style(color = UI.colors.pureInverse),
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            val amountPrefix = if (event.isIncome) "+" else "-"
                            val amountColor = if (event.isIncome) Color(0xFF4CAF50) else Color(0xFFEF5350)
                            Text(
                                text = "$amountPrefix${event.amount.format(baseCurrency)} $baseCurrency",
                                style = UI.typo.c.style(color = amountColor, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
