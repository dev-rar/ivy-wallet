package com.ivy.domain.model.statement

enum class BankStatementProfile(
    val displayName: String,
    val dateColumnNames: List<String>,
    val descriptionColumnNames: List<String>,
    val amountColumnNames: List<String>,
    val debitColumnNames: List<String>,
    val creditColumnNames: List<String>,
    val dateFormat: String,
) {
    MEEZAN_BANK(
        displayName = "Meezan Bank",
        dateColumnNames = listOf("post date", "trans date", "date"),
        descriptionColumnNames = listOf("description", "particulars", "narration"),
        amountColumnNames = emptyList(),
        debitColumnNames = listOf("debit", "withdrawal"),
        creditColumnNames = listOf("credit", "deposit"),
        dateFormat = "dd/MM/yyyy"
    ),
    HBL(
        displayName = "Habib Bank Limited (HBL)",
        dateColumnNames = listOf("transaction date", "trans date", "date"),
        descriptionColumnNames = listOf("narration", "description", "transaction remarks"),
        amountColumnNames = emptyList(),
        debitColumnNames = listOf("withdrawal", "debit"),
        creditColumnNames = listOf("deposit", "credit"),
        dateFormat = "dd-MM-yyyy"
    ),
    SADAPAY(
        displayName = "SadaPay",
        dateColumnNames = listOf("date", "timestamp"),
        descriptionColumnNames = listOf("description", "title", "recipient"),
        amountColumnNames = listOf("amount (pkr)", "amount"),
        debitColumnNames = emptyList(),
        creditColumnNames = emptyList(),
        dateFormat = "yyyy-MM-dd"
    ),
    NAYAPAY(
        displayName = "NayaPay",
        dateColumnNames = listOf("transaction date", "date"),
        descriptionColumnNames = listOf("details", "title", "description"),
        amountColumnNames = listOf("amount", "amount (pkr)"),
        debitColumnNames = emptyList(),
        creditColumnNames = emptyList(),
        dateFormat = "yyyy-MM-dd"
    ),
    GENERIC(
        displayName = "Standard Banking CSV",
        dateColumnNames = listOf("date", "trans date", "transaction date"),
        descriptionColumnNames = listOf("description", "narration", "title", "memo", "details"),
        amountColumnNames = listOf("amount", "total"),
        debitColumnNames = listOf("debit", "withdrawal", "outflow"),
        creditColumnNames = listOf("credit", "deposit", "inflow"),
        dateFormat = "yyyy-MM-dd"
    );

    companion object {
        fun detectProfile(headerRow: List<String>): BankStatementProfile {
            val lowerHeaders = headerRow.map { it.trim().lowercase() }
            for (profile in entries) {
                if (profile == GENERIC) continue
                val hasDate = profile.dateColumnNames.any { it in lowerHeaders }
                val hasDebitCredit = profile.debitColumnNames.isNotEmpty() &&
                        profile.debitColumnNames.any { it in lowerHeaders } &&
                        profile.creditColumnNames.any { it in lowerHeaders }
                val hasAmount = profile.amountColumnNames.isNotEmpty() &&
                        profile.amountColumnNames.any { it in lowerHeaders }

                if (hasDate && (hasDebitCredit || hasAmount)) {
                    return profile
                }
            }
            return GENERIC
        }
    }
}

