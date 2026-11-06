package com.ivy.budgets.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.base.model.TransactionType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.statement.BankStatementProfile
import com.ivy.domain.model.statement.StatementTransaction
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalCheck
import com.ivy.wallet.ui.theme.modal.ModalTitle
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun BoxWithConstraintsScope.ImportStatementModal(
    visible: Boolean,
    accounts: List<Account>,
    period: BudgetPeriod,
    baseCurrency: String,
    onParse: suspend (csv: String, profile: BankStatementProfile, accountId: UUID?) -> List<StatementTransaction>,
    onImport: (transactions: List<StatementTransaction>, accountId: UUID, skipDuplicates: Boolean) -> Unit,
    dismiss: () -> Unit
) {
    if (!visible) return

    val modalId = remember(visible) { UUID.randomUUID() }
    val coroutineScope = rememberCoroutineScope()
    var selectedProfile by remember(visible) { mutableStateOf(BankStatementProfile.GENERIC) }
    var selectedAccountId by remember(visible) { mutableStateOf(accounts.firstOrNull()?.id) }
    var csvText by remember(visible) { mutableStateOf("") }
    var parsedTransactions by remember(visible) { mutableStateOf<List<StatementTransaction>?>(null) }
    var skipDuplicates by remember(visible) { mutableStateOf(true) }

    IvyModal(
        id = modalId,
        visible = true,
        dismiss = dismiss,
        PrimaryAction = {
            if (parsedTransactions == null) {
                ModalCheck(
                    label = "Parse CSV",
                    enabled = csvText.isNotBlank()
                ) {
                    coroutineScope.launch {
                        parsedTransactions = onParse(csvText, selectedProfile, selectedAccountId)
                    }
                }
            } else {
                val nonDups = parsedTransactions!!.filter { !skipDuplicates || !it.isDuplicate }
                ModalCheck(
                    label = "Import (${nonDups.size})",
                    enabled = nonDups.isNotEmpty() && selectedAccountId != null
                ) {
                    onImport(parsedTransactions!!, selectedAccountId!!, skipDuplicates)
                    dismiss()
                }
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModalTitle(text = if (parsedTransactions == null) "Import Statement" else "Preview & Match")

            if (parsedTransactions != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(UI.colors.medium.copy(alpha = 0.1f))
                        .clickable { parsedTransactions = null }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Edit CSV",
                        style = UI.typo.c.style(
                            color = UI.colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (parsedTransactions == null) {
            // STEP 1: Format selection & CSV Input
            Text(
                text = "Select your bank format or use Auto-Detect for Meezan, HBL, SadaPay, NayaPay, or Generic CSV.",
                style = UI.typo.c.style(
                    color = UI.colors.pureInverse.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Profile chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(BankStatementProfile.entries.toTypedArray()) { profile ->
                    val isSelected = selectedProfile == profile
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) UI.colors.primary else UI.colors.medium.copy(alpha = 0.1f))
                            .clickable { selectedProfile = profile }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = profile.displayName,
                            style = UI.typo.c.style(
                                color = if (isSelected) UI.colors.pure else UI.colors.pureInverse,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Account selector
            if (accounts.isNotEmpty()) {
                Text(
                    text = "Deposit / Deduct From Account",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts) { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) UI.colors.primary else UI.colors.medium.copy(alpha = 0.1f))
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = acc.name,
                                style = UI.typo.c.style(
                                    color = if (isSelected) UI.colors.pure else UI.colors.pureInverse,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Quick sample fill button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(UI.colors.primary.copy(alpha = 0.08f))
                    .clickable {
                        selectedProfile = BankStatementProfile.MEEZAN_BANK
                        csvText = """
Post Date,Description,Instrument No,Debit,Credit,Balance
01/11/2026,K-Electric Bill Payment,,4500.00,,95500.00
03/11/2026,Imtiaz Supermarket Groceries,,12500.00,,83000.00
05/11/2026,Salary Transfer,,,150000.00,233000.00
08/11/2026,Fuel Shell Station,,3000.00,,230000.00
                        """.trimIndent()
                    }
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡ Load Sample Pakistani Bank Statement",
                    style = UI.typo.c.style(
                        color = UI.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // CSV input box
            TextField(
                value = csvText,
                onValueChange = { csvText = it },
                placeholder = {
                    Text(
                        text = "Paste statement CSV content here...\ne.g. Date, Description, Debit, Credit",
                        style = UI.typo.c.style(color = UI.colors.pureInverse.copy(alpha = 0.4f))
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = UI.colors.medium.copy(alpha = 0.08f),
                    unfocusedContainerColor = UI.colors.medium.copy(alpha = 0.05f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        } else {
            // STEP 2: Preview & Duplicate Handling
            val transactions = parsedTransactions!!
            val dupCount = transactions.count { it.isDuplicate }
            val newCount = transactions.size - dupCount

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1B5E20).copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "New Transactions", style = UI.typo.c.style(color = Color(0xFF4CAF50)))
                        Text(text = "$newCount items", style = UI.typo.b1.style(color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold))
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (dupCount > 0) Color(0xFFF57F17).copy(alpha = 0.15f) else UI.colors.medium.copy(alpha = 0.08f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Duplicates Found", style = UI.typo.c.style(color = if (dupCount > 0) Color(0xFFFFB300) else UI.colors.pureInverse))
                        Text(text = "$dupCount items", style = UI.typo.b1.style(color = if (dupCount > 0) Color(0xFFFFB300) else UI.colors.pureInverse, fontWeight = FontWeight.Bold))
                    }
                }
            }

            if (dupCount > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(UI.colors.medium.copy(alpha = 0.08f))
                        .clickable { skipDuplicates = !skipDuplicates }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (skipDuplicates) UI.colors.primary else UI.colors.medium.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (skipDuplicates) {
                            IvyIcon(icon = R.drawable.ic_check, tint = UI.colors.pure, modifier = Modifier.size(12.dp))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Skip detected duplicate transactions",
                        style = UI.typo.c.style(color = UI.colors.pureInverse, fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            for (trn in transactions) {
                val isExpense = trn.type == TransactionType.EXPENSE
                val trnColor = if (isExpense) Color(0xFFEF5350) else Color(0xFF4CAF50)
                val sign = if (isExpense) "-" else "+"

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(UI.colors.medium.copy(alpha = 0.05f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trn.description,
                            style = UI.typo.b2.style(color = UI.colors.pureInverse, fontWeight = FontWeight.Medium),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$sign${trn.amount.format(baseCurrency)} $baseCurrency",
                            style = UI.typo.b1.style(color = trnColor, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (trn.isDuplicate) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF57F17).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "⚠️ Duplicate",
                                    style = UI.typo.nC.style(color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        if (trn.suggestedBudgetItemId != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(UI.colors.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🎯 Matches Budget Item",
                                    style = UI.typo.nC.style(color = UI.colors.primary, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

