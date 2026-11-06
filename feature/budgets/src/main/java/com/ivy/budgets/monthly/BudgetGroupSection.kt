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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.budget.MonthlyGroupSummary
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon

@Composable
fun BudgetGroupSection(
    group: MonthlyGroupSummary,
    baseCurrency: String,
    onAddItem: (groupId: java.util.UUID) -> Unit,
    onEditItem: (item: MonthlyItemSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Group Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = UI.typo.b1.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${group.actualTotal.format(baseCurrency)} / ${group.plannedTotal.format(baseCurrency)} $baseCurrency spent",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Normal
                    )
                )
            }

            // + Add Item Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(UI.colors.primary.copy(alpha = 0.12f))
                    .clickable { onAddItem(group.id) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IvyIcon(
                        icon = R.drawable.ic_plus,
                        tint = UI.colors.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        style = UI.typo.c.style(
                            color = UI.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Group Items
        if (group.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(UI.colors.medium.copy(alpha = 0.03f))
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "No budget items yet. Tap Add above to plan an expense line.",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        } else {
            for (item in group.items) {
                BudgetItemRow(
                    item = item,
                    baseCurrency = baseCurrency,
                    onClick = { onEditItem(item) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
