package com.ivy.data.bank

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.model.TransactionType
import com.ivy.data.cloud.CashPulseCloudAccountManager
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.write.WriteTransactionDao
import com.ivy.data.db.entity.TransactionEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

/**
 * On-device bank/wallet SMS & push notification auto-tracker for CashPulse Pro users.
 * Zero transaction data leaves the phone unless the user enables Firebase Cloud Sync.
 */
@AndroidEntryPoint
class CashPulseBankNotificationService : NotificationListenerService() {

    @Inject
    lateinit var sharedPrefs: SharedPrefs

    @Inject
    lateinit var accountDao: AccountDao

    @Inject
    lateinit var writeTransactionDao: WriteTransactionDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val enabled = sharedPrefs.getBoolean(CashPulseCloudAccountManager.KEY_BANK_AUTO_TRACK, false)
        val isPro = sharedPrefs.getBoolean(CashPulseCloudAccountManager.KEY_IS_PRO, false)
        if (!enabled || !isPro) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val combined = "$title $text".trim()
        if (combined.isBlank()) return

        val parsed = parseBankMessage(combined) ?: return

        serviceScope.launch {
            val primaryAccount = accountDao.findAll(deleted = false).firstOrNull() ?: return@launch
            val transaction = TransactionEntity(
                accountId = primaryAccount.id,
                type = parsed.type,
                amount = parsed.amount,
                toAmount = parsed.amount,
                title = parsed.merchantTitle,
                description = "Auto-captured from notification: $combined",
                dateTime = Instant.now()
            )
            writeTransactionDao.save(transaction)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    data class ParsedBankTransaction(
        val type: TransactionType,
        val amount: Double,
        val merchantTitle: String
    )

    companion object {
        private val AMOUNT_REGEX =
            Regex("""(?:PKR|Rs\.?|USD|\$|EUR|€|GBP|£|AED|SAR|INR|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)

        private val EXPENSE_KEYWORDS = listOf(
            "debited", "spent", "paid", "purchase", "withdrawn", "sent", "charged", "pos", "atm"
        )
        private val INCOME_KEYWORDS = listOf(
            "credited", "received", "deposited", "refund", "salary", "cashback"
        )

        fun parseBankMessage(message: String): ParsedBankTransaction? {
            val lower = message.lowercase()
            val isExpense = EXPENSE_KEYWORDS.any { lower.contains(it) }
            val isIncome = INCOME_KEYWORDS.any { lower.contains(it) }
            if (!isExpense && !isIncome) return null

            val match = AMOUNT_REGEX.find(message) ?: return null
            val rawNum = match.groupValues.getOrNull(1)?.replace(",", "") ?: return null
            val amount = rawNum.toDoubleOrNull() ?: return null
            if (amount <= 0.0) return null

            val type = if (isIncome && !isExpense) TransactionType.INCOME else TransactionType.EXPENSE
            val merchant = extractMerchant(message) ?: if (type == TransactionType.INCOME) {
                "Bank Deposit"
            } else {
                "Bank Purchase"
            }
            return ParsedBankTransaction(type = type, amount = amount, merchantTitle = merchant)
        }

        private fun extractMerchant(message: String): String? {
            val atMatch = Regex("""\b(?:at|to|from)\s+([A-Za-z0-9 &._-]{2,28})""", RegexOption.IGNORE_CASE)
                .find(message)
            return atMatch?.groupValues?.getOrNull(1)?.trim()
        }
    }
}

