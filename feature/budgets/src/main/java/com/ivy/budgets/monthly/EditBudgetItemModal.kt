package com.ivy.budgets.monthly

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
        categoryId: UUID?
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

    var amountModalVisible by remember(modal) { mutableStateOf(false) }
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
                    modal.initialItemType,
                    selectedCategoryId
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

        ModalAmountSection(
            label = "Planned Amount",
            amount = amount,
            currency = baseCurrency
        ) {
            amountModalVisible = true
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
