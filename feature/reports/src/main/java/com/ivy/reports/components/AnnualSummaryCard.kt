package com.ivy.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.IvyIcon
import java.time.LocalDate

@Composable
fun AnnualSummaryCard(
    year: Int,
    baseCurrency: String,
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    savingsRate: Double,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCalendarYear = LocalDate.now().year
    val canGoForward = year < currentCalendarYear
    val isNetPositive = netSavings >= 0
    val prefix = if (isNetPositive) "+" else ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1B1D24),
                        Color(0xFF2A2E39)
                    )
                )
            )
            .border(1.dp, White.copy(alpha = 0.12f), UI.shapes.r4)
    ) {
        // Top Row: Year Navigator on Left | Savings Rate Pill on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(White.copy(alpha = 0.1f))
                        .clickable { onPreviousYear() },
                    contentAlignment = Alignment.Center
                ) {
                    IvyIcon(
                        icon = R.drawable.ic_back,
                        tint = White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = year.toString(),
                    style = UI.typo.b1.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = White
                    )
                )

                Spacer(Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (canGoForward) White.copy(alpha = 0.1f) else White.copy(alpha = 0.04f))
                        .clickable(enabled = canGoForward) { onNextYear() },
                    contentAlignment = Alignment.Center
                ) {
                    IvyIcon(
                        icon = R.drawable.ic_arrow_right,
                        tint = if (canGoForward) White else Gray.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (totalIncome > 0) {
                Box(
                    modifier = Modifier
                        .clip(UI.shapes.rFull)
                        .background(
                            if (isNetPositive) Green.copy(alpha = 0.18f) else Red.copy(alpha = 0.18f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", savingsRate)}% Saved",
                        style = UI.typo.nC.style(
                            color = if (isNetPositive) Green else Red,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Center: Net Cash Flow
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "NET CASH FLOW",
                style = UI.typo.c.style(
                    color = White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$prefix${netSavings.format(baseCurrency)} $baseCurrency",
                style = UI.typo.b1.style(
                    color = if (isNetPositive) Green else Red,
                    fontWeight = FontWeight.ExtraBold
                ).copy(fontSize = 24.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(20.dp))

        // Bottom Strip: Unified Income & Expenses Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White.copy(alpha = 0.06f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Green.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IvyIcon(
                        icon = R.drawable.ic_income_white,
                        tint = Green,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = stringResource(R.string.income),
                        style = UI.typo.c.style(
                            color = White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${totalIncome.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.nB2.style(
                            color = Green,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Vertical Divider
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(White.copy(alpha = 0.14f))
            )

            Spacer(Modifier.width(10.dp))

            // Expense Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Red.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IvyIcon(
                        icon = R.drawable.ic_expense,
                        tint = Red,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Text(
                        text = stringResource(R.string.expenses),
                        style = UI.typo.c.style(
                            color = White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${totalExpenses.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.nB2.style(
                            color = White,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
