package com.ivy.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.data.model.TransactionSplit
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.AddPrimaryAttributeButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SplitTransactionButton(
    splits: ImmutableList<TransactionSplit>,
    categories: ImmutableList<Category>,
    currency: String,
    onClick: () -> Unit
) {
    if (splits.isEmpty()) {
        AddPrimaryAttributeButton(
            icon = R.drawable.ic_categories,
            text = "Split Transaction",
            onClick = onClick
        )
    } else {
        val catMap = categories.associateBy { it.id }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(UI.shapes.r4)
                .background(UI.colors.medium, UI.shapes.r4)
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IvyIcon(icon = R.drawable.ic_categories)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Split Transaction (${splits.size} items)",
                        style = UI.typo.b2.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "Edit",
                    style = UI.typo.c.style(
                        color = UI.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            splits.forEachIndexed { index, split ->
                val cat = split.categoryId?.let { catMap[it] }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = cat?.name?.value ?: split.note ?: "Split #${index + 1}",
                        style = UI.typo.c.style(color = UI.colors.pureInverse)
                    )
                    Text(
                        text = "$currency ${String.format("%.2f", split.amount)}",
                        style = UI.typo.c.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

