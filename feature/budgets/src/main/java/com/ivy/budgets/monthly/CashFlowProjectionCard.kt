package com.ivy.budgets.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.ivy.domain.model.cashflow.CashFlowProjection
import com.ivy.legacy.utils.format

@Composable
fun CashFlowProjectionCard(
    projection: CashFlowProjection,
    baseCurrency: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(UI.colors.medium.copy(alpha = 0.08f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Cash-Flow Forecast",
                style = UI.typo.b1.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.weight(1f))

            val statusColor = if (projection.hasShortfall) Color(0xFFEF5350) else Color(0xFF4CAF50)
            val statusText = if (projection.hasShortfall) "Shortfall Warning" else "Cash Adequate"

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    style = UI.typo.c.style(
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Balance trajectory overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Starting Cash",
                    style = UI.typo.c.style(color = UI.colors.pureInverse.copy(alpha = 0.6f))
                )
                Text(
                    text = "${projection.startingCash.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.SemiBold)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lowest Point (Buffer)",
                    style = UI.typo.c.style(color = UI.colors.pureInverse.copy(alpha = 0.6f))
                )
                val minColor = if (projection.minimumBalance < 0) Color(0xFFEF5350) else UI.colors.pureInverse
                Text(
                    text = "${projection.minimumBalance.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = minColor, fontWeight = FontWeight.Bold)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Ending Projected",
                    style = UI.typo.c.style(color = UI.colors.pureInverse.copy(alpha = 0.6f))
                )
                Text(
                    text = "${projection.endingCash.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Shortfall warning banner if applicable
        if (projection.hasShortfall && projection.shortfallDate != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF5350).copy(alpha = 0.12f))
                    .padding(10.dp)
            ) {
                Text(
                    text = "⚠️ Cash shortage expected around ${projection.shortfallDate}. Incoming paycheck or liquid buffer required before this date.",
                    style = UI.typo.c.style(
                        color = Color(0xFFEF5350),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Upcoming scheduled events preview
        if (projection.events.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Upcoming Timeline",
                style = UI.typo.c.style(
                    color = UI.colors.pureInverse.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(6.dp))

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
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = event.title,
                        style = UI.typo.c.style(color = UI.colors.pureInverse),
                        modifier = Modifier.weight(1f)
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

