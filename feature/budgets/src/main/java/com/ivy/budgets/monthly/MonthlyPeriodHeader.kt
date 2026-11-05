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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon

@Composable
fun MonthlyPeriodHeader(
    period: BudgetPeriod,
    onChangePeriod: (BudgetPeriod) -> Unit,
    onCopyPreviousMonth: () -> Unit,
    hasItems: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(UI.colors.medium.copy(alpha = 0.15f))
                    .clickable { onChangePeriod(period.previous()) },
                contentAlignment = Alignment.Center
            ) {
                IvyIcon(
                    icon = R.drawable.ic_back,
                    tint = UI.colors.pureInverse,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = period.toDisplayMonthYear(),
                style = UI.typo.h2.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.Bold
                )
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(UI.colors.medium.copy(alpha = 0.15f))
                    .clickable { onChangePeriod(period.next()) },
                contentAlignment = Alignment.Center
            ) {
                IvyIcon(
                    icon = R.drawable.ic_arrow_right,
                    tint = UI.colors.pureInverse,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (!hasItems) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(UI.colors.primary.copy(alpha = 0.12f))
                    .clickable { onCopyPreviousMonth() }
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IvyIcon(
                        icon = R.drawable.ic_sync,
                        tint = UI.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Copy Plan from ${period.previous().toDisplayMonthYear()}",
                        style = UI.typo.b2.style(
                            color = UI.colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}
