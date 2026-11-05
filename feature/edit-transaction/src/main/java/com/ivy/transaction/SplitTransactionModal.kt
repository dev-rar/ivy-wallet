package com.ivy.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ivy.data.model.Category
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionSplit
import com.ivy.data.model.TransactionSplitId
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton
import com.ivy.wallet.ui.theme.modal.IvyModal
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID
import kotlin.math.abs

@Composable
fun BoxWithConstraintsScope.SplitTransactionModal(
    visible: Boolean,
    parentAmount: Double,
    currency: String,
    initialSplits: List<TransactionSplit>,
    categories: ImmutableList<Category>,
    onApplySplits: (List<TransactionSplit>) -> Unit,
    dismiss: () -> Unit
) {
    var splitsState by remember(visible, initialSplits) {
        val initial = if (initialSplits.isNotEmpty()) {
            initialSplits
        } else {
            val half = (parentAmount / 2.0)
            listOf(
                TransactionSplit(
                    id = TransactionSplitId(UUID.randomUUID()),
                    transactionId = TransactionId(UUID.randomUUID()),
                    amount = half,
                    categoryId = categories.firstOrNull()?.id,
                    orderNum = 0.0
                ),
                TransactionSplit(
                    id = TransactionSplitId(UUID.randomUUID()),
                    transactionId = TransactionId(UUID.randomUUID()),
                    amount = parentAmount - half,
                    categoryId = categories.getOrNull(1)?.id ?: categories.firstOrNull()?.id,
                    orderNum = 1.0
                )
            )
        }
        mutableStateOf(initial)
    }

    val totalSplit = splitsState.sumOf { it.amount }
    val remaining = parentAmount - totalSplit
    val isBalanced = abs(remaining) < 0.01 && splitsState.isNotEmpty()

    IvyModal(
        id = UUID.randomUUID(),
        visible = visible,
        dismiss = dismiss,
        SecondaryActions = {
            if (initialSplits.isNotEmpty() || splitsState.isNotEmpty()) {
                IvyOutlinedButton(
                    text = "Clear Splits",
                    iconStart = null,
                    onClick = {
                        onApplySplits(emptyList())
                        dismiss()
                    }
                )
            }
        },
        PrimaryAction = {
            IvyButton(
                text = "Apply Splits",
                enabled = isBalanced,
                onClick = {
                    if (isBalanced) {
                        onApplySplits(splitsState)
                        dismiss()
                    }
                }
            )
        }
    ) {
        Text(
            text = "Split Transaction",
            style = UI.typo.h2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Balance Summary Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(UI.shapes.r3)
                .background(UI.colors.medium)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transaction Total",
                    style = UI.typo.c.style(color = UI.colors.pureInverse)
                )
                Text(
                    text = "$currency ${String.format("%.2f", parentAmount)}",
                    style = UI.typo.b1.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isBalanced) "Status" else "Remaining",
                    style = UI.typo.c.style(color = UI.colors.pureInverse)
                )
                Text(
                    text = if (isBalanced) "Balanced ✓" else "$currency ${String.format("%.2f", remaining)}",
                    style = UI.typo.b1.style(
                        color = if (isBalanced) UI.colors.primary else UI.colors.red,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Split items
        splitsState.forEachIndexed { index, split ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(UI.shapes.r3)
                    .background(UI.colors.medium)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Split #${index + 1}",
                        style = UI.typo.b2.style(
                            color = UI.colors.pureInverse,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    if (splitsState.size > 1) {
                        Text(
                            text = "Remove",
                            style = UI.typo.c.style(color = UI.colors.red),
                            modifier = Modifier.clickable {
                                splitsState = splitsState.filterIndexed { i, _ -> i != index }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Amount input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(UI.shapes.r2)
                        .background(UI.colors.pure)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currency ",
                        style = UI.typo.b1.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold)
                    )
                    BasicTextField(
                        value = if (split.amount == 0.0) "" else String.format("%.2f", split.amount),
                        onValueChange = { newText ->
                            val parsed = newText.toDoubleOrNull() ?: 0.0
                            splitsState = splitsState.mapIndexed { i, s ->
                                if (i == index) s.copy(amount = parsed) else s
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = UI.typo.b1.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold),
                        cursorBrush = SolidColor(UI.colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Category selector chips (horizontal scroll)
                val catScroll = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(catScroll),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = split.categoryId == category.id
                        Box(
                            modifier = Modifier
                                .clip(UI.shapes.r2)
                                .background(if (isSelected) UI.colors.primary else UI.colors.pure)
                                .clickable {
                                    splitsState = splitsState.mapIndexed { i, s ->
                                        if (i == index) s.copy(categoryId = category.id) else s
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = category.name.value,
                                style = UI.typo.c.style(
                                    color = if (isSelected) UI.colors.pure else UI.colors.pureInverse,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Note input
                BasicTextField(
                    value = split.note.orEmpty(),
                    onValueChange = { newNote ->
                        splitsState = splitsState.mapIndexed { i, s ->
                            if (i == index) s.copy(note = newNote.ifBlank { null }) else s
                        }
                    },
                    textStyle = UI.typo.c.style(color = UI.colors.pureInverse),
                    cursorBrush = SolidColor(UI.colors.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(UI.shapes.r2)
                                .background(UI.colors.pure)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (split.note.isNullOrBlank()) {
                                Text(
                                    text = "Add note (optional)",
                                    style = UI.typo.c.style(color = UI.colors.mediumInverse)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Add Split button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(UI.shapes.r3)
                .border(1.dp, UI.colors.primary, UI.shapes.r3)
                .clickable {
                    val newSplitAmount = if (remaining > 0) remaining else 0.0
                    splitsState = splitsState + TransactionSplit(
                        id = TransactionSplitId(UUID.randomUUID()),
                        transactionId = TransactionId(UUID.randomUUID()),
                        amount = newSplitAmount,
                        categoryId = categories.firstOrNull()?.id,
                        orderNum = splitsState.size.toDouble()
                    )
                }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add Another Split",
                style = UI.typo.b2.style(color = UI.colors.primary, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

