package com.ivy.accounts

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
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
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.data.model.AccountData
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.modal.IvyModal
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.UpdateValuationModal(
    visible: Boolean,
    accounts: List<AccountData>,
    currency: String,
    onSaveValuation: (accountId: UUID, amount: Double, notes: String?) -> Unit,
    dismiss: () -> Unit
) {
    var selectedAccount by remember(accounts) {
        mutableStateOf(accounts.firstOrNull()?.account)
    }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val isValid = selectedAccount != null && parsedAmount > 0.0

    IvyModal(
        id = UUID.randomUUID(),
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            IvyButton(
                text = "Save Valuation",
                enabled = isValid,
                onClick = {
                    if (isValid) {
                        onSaveValuation(selectedAccount!!.id.value, parsedAmount, noteText.ifBlank { null })
                        dismiss()
                    }
                }
            )
        }
    ) {
        Text(
            text = "Record Investment Valuation",
            style = UI.typo.h2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Select account and update its latest portfolio market value.",
            style = UI.typo.c.style(color = UI.colors.mediumInverse),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        val chipScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipScroll)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            accounts.forEach { accountData ->
                val acc = accountData.account
                val isSelected = selectedAccount?.id == acc.id
                Box(
                    modifier = Modifier
                        .clip(UI.shapes.r2)
                        .background(if (isSelected) UI.colors.primary else UI.colors.medium)
                        .clickable { selectedAccount = acc }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = acc.name.value,
                        style = UI.typo.b2.style(
                            color = if (isSelected) UI.colors.pure else UI.colors.pureInverse,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Current Value / NAV",
                style = UI.typo.c.style(color = UI.colors.mediumInverse)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(UI.shapes.r2)
                    .background(UI.colors.medium)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$currency ",
                    style = UI.typo.b1.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold)
                )
                BasicTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = UI.typo.b1.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(UI.colors.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Notes (optional)",
                style = UI.typo.c.style(color = UI.colors.mediumInverse)
            )
            Spacer(Modifier.height(4.dp))
            BasicTextField(
                value = noteText,
                onValueChange = { noteText = it },
                textStyle = UI.typo.b2.style(color = UI.colors.pureInverse),
                cursorBrush = SolidColor(UI.colors.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(UI.shapes.r2)
                            .background(UI.colors.medium)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        if (noteText.isBlank()) {
                            Text(
                                text = "e.g. Q4 NAV statement, PSX close",
                                style = UI.typo.b2.style(color = UI.colors.mediumInverse)
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

