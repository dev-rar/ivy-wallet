package com.ivy.budgets.monthly

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalAddSave
import com.ivy.wallet.ui.theme.modal.ModalAmountSection
import com.ivy.wallet.ui.theme.modal.ModalTitle
import com.ivy.wallet.ui.theme.modal.edit.AmountModal
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.EditIncomeModal(
    visible: Boolean,
    initialIncome: Double,
    initialCarryover: Double,
    baseCurrency: String,
    onSave: (income: Double, carryover: Double) -> Unit,
    dismiss: () -> Unit
) {
    if (!visible) return

    val modalId = remember(visible) { UUID.randomUUID() }
    val incomeAmountModalId = remember(visible) { UUID.randomUUID() }
    val carryoverAmountModalId = remember(visible) { UUID.randomUUID() }

    var income by remember(visible, initialIncome) { mutableDoubleStateOf(initialIncome) }
    var carryover by remember(visible, initialCarryover) { mutableDoubleStateOf(initialCarryover) }

    var incomeAmountModalVisible by remember { mutableStateOf(false) }
    var carryoverAmountModalVisible by remember { mutableStateOf(false) }

    IvyModal(
        id = modalId,
        visible = true,
        dismiss = dismiss,
        PrimaryAction = {
            ModalAddSave(
                item = true,
                enabled = true
            ) {
                onSave(income, carryover)
                dismiss()
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ModalTitle(text = "Monthly Income & Carryover")
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Total planned income to be assigned across your budget lines this month.",
            style = UI.typo.c.style(
                color = UI.colors.pureInverse.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(16.dp))

        ModalAmountSection(
            label = "Planned Income",
            amount = income,
            currency = baseCurrency
        ) {
            incomeAmountModalVisible = true
        }

        Spacer(Modifier.height(16.dp))

        ModalAmountSection(
            label = "Carryover from Previous Month (Optional)",
            amount = carryover,
            currency = baseCurrency
        ) {
            carryoverAmountModalVisible = true
        }

        Spacer(Modifier.height(32.dp))
    }

    AmountModal(
        id = incomeAmountModalId,
        visible = incomeAmountModalVisible,
        currency = baseCurrency,
        initialAmount = income,
        dismiss = { incomeAmountModalVisible = false }
    ) {
        income = it
    }

    AmountModal(
        id = carryoverAmountModalId,
        visible = carryoverAmountModalVisible,
        currency = baseCurrency,
        initialAmount = carryover,
        dismiss = { carryoverAmountModalVisible = false }
    ) {
        carryover = it
    }
}
