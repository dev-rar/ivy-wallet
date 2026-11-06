package com.ivy.budgets.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.budgets.model.EditBudgetItemModalData
import com.ivy.data.model.Category
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.legacy.ui.theme.components.ListItem
import com.ivy.legacy.legacy.ui.theme.modal.ModalNameInput
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.legacy.utils.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.modal.DeleteModal
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalAddSave
import com.ivy.wallet.ui.theme.modal.ModalAmountSection
import com.ivy.wallet.ui.theme.modal.ModalDelete
import com.ivy.wallet.ui.theme.modal.ModalTitle
import com.ivy.wallet.ui.theme.modal.edit.AmountModal
import com.ivy.wallet.ui.theme.toComposeColor
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.EditBudgetItemModal(
    modal: EditBudgetItemModalData?,
    categories: List<Category>,
    baseCurrency: String,
    onSave: (
        itemId: UUID?,
        groupId: UUID,
        period: BudgetPeriod,
        name: String,
        amount: Double,
        itemType: BudgetItemType,
        categoryId: UUID?,
        targetAmount: Double,
        openingBalance: Double,
        notes: String?
    ) -> Unit,
    onDelete: (itemId: UUID) -> Unit,
    dismiss: () -> Unit
) {
    if (modal == null) return

    val modalId = remember(modal) { UUID.randomUUID() }
    val amountModalId = remember(modal) { UUID.randomUUID() }

    var nameTextFieldValue by remember(modal) {
        mutableStateOf(selectEndTextFieldValue(modal.initialName))
    }
    var amount by remember(modal) {
        mutableDoubleStateOf(modal.initialPlannedAmount)
    }
    var selectedCategoryId by remember(modal) {
        mutableStateOf(modal.initialCategoryId)
    }
    var isSinkingFund by remember(modal) {
        mutableStateOf(modal.initialItemType == BudgetItemType.SINKING_FUND || modal.initialTargetAmount > 0.0)
    }
    var targetAmount by remember(modal) {
        mutableDoubleStateOf(modal.initialTargetAmount)
    }
    var openingBalance by remember(modal) {
        mutableDoubleStateOf(modal.initialOpeningBalance)
    }

    var amountModalVisible by remember(modal) { mutableStateOf(false) }
    var targetAmountModalVisible by remember(modal) { mutableStateOf(false) }
    var openingBalanceModalVisible by remember(modal) { mutableStateOf(false) }
    var deleteModalVisible by remember(modal) { mutableStateOf(false) }

    IvyModal(
        id = modalId,
        visible = true,
        dismiss = dismiss,
        PrimaryAction = {
            ModalAddSave(
                item = modal.itemId,
                enabled = nameTextFieldValue.text.isNotNullOrBlank() && amount > 0.0
            ) {
                onSave(
                    modal.itemId,
                    modal.groupId,
                    modal.period,
                    nameTextFieldValue.text.trim(),
                    amount,
                    if (isSinkingFund) BudgetItemType.SINKING_FUND else BudgetItemType.STANDARD,
                    selectedCategoryId,
                    if (isSinkingFund) targetAmount else 0.0,
                    if (isSinkingFund) openingBalance else 0.0,
                    modal.initialNotes
                )
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ModalTitle(
                text = if (modal.itemId != null) "Edit Budget Item" else "New Budget Item"
            )

            Spacer(Modifier.weight(1f))

            if (modal.itemId != null) {
                ModalDelete {
                    deleteModalVisible = true
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        ModalNameInput(
            hint = "e.g., Groceries, Rent, PSX Shares",
            autoFocusKeyboard = modal.itemId == null,
            textFieldValue = nameTextFieldValue,
            setTextFieldValue = { nameTextFieldValue = it }
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(UI.shapes.r2)
                    .background(if (!isSinkingFund) UI.colors.primary else UI.colors.medium)
                    .clickable { isSinkingFund = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Standard Expense",
                    style = UI.typo.b2.style(
                        color = if (!isSinkingFund) UI.colors.pure else UI.colors.pureInverse,
                        fontWeight = if (!isSinkingFund) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(UI.shapes.r2)
                    .background(if (isSinkingFund) UI.colors.primary else UI.colors.medium)
                    .clickable { isSinkingFund = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎯 Sinking Fund",
                    style = UI.typo.b2.style(
                        color = if (isSinkingFund) UI.colors.pure else UI.colors.pureInverse,
                        fontWeight = if (isSinkingFund) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        ModalAmountSection(
            label = if (isSinkingFund) "Monthly Planned Contribution" else "Planned Amount",
            amount = amount,
            currency = baseCurrency
        ) {
            amountModalVisible = true
        }

        if (isSinkingFund) {
            Spacer(Modifier.height(12.dp))

            ModalAmountSection(
                label = "Total Goal Target",
                amount = targetAmount,
                currency = baseCurrency
            ) {
                targetAmountModalVisible = true
            }

            Spacer(Modifier.height(12.dp))

            ModalAmountSection(
                label = "Opening / Saved Balance",
                amount = openingBalance,
                currency = baseCurrency
            ) {
                openingBalanceModalVisible = true
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Link to Category (for automatic tracking)",
            style = UI.typo.c.style(
                color = UI.colors.pureInverse.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Spacer(Modifier.width(24.dp))
            }
            items(categories) { category ->
                ListItem(
                    icon = category.icon?.id,
                    defaultIcon = R.drawable.ic_custom_category_s,
                    text = category.name.value,
                    selectedColor = category.color.value.toComposeColor().takeIf {
                        selectedCategoryId == category.id.value
                    }
                ) { selected ->
                    selectedCategoryId = if (selected) null else category.id.value
                    if (selectedCategoryId != null && nameTextFieldValue.text.isBlank()) {
                        nameTextFieldValue = selectEndTextFieldValue(category.name.value)
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
            item {
                Spacer(Modifier.width(16.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    AmountModal(
        id = amountModalId,
        visible = amountModalVisible,
        currency = baseCurrency,
        initialAmount = amount,
        dismiss = { amountModalVisible = false }
    ) {
        amount = it
    }

    AmountModal(
        id = remember(modal) { UUID.randomUUID() },
        visible = targetAmountModalVisible,
        currency = baseCurrency,
        initialAmount = targetAmount,
        dismiss = { targetAmountModalVisible = false }
    ) {
        targetAmount = it
    }

    AmountModal(
        id = remember(modal) { UUID.randomUUID() },
        visible = openingBalanceModalVisible,
        currency = baseCurrency,
        initialAmount = openingBalance,
        dismiss = { openingBalanceModalVisible = false }
    ) {
        openingBalance = it
    }

    DeleteModal(
        visible = deleteModalVisible,
        title = "Delete ${nameTextFieldValue.text}?",
        description = "This will remove this budget item from ${modal.period.toDisplayMonthYear()}.",
        dismiss = { deleteModalVisible = false }
    ) {
        deleteModalVisible = false
        modal.itemId?.let { onDelete(it) }
        dismiss()
    }
}
