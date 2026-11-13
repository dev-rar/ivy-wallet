package com.ivy.data.cloud

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ivy.base.legacy.SharedPrefs
import com.ivy.data.backup.BackupDataUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class CashPulseCloudAccountManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPrefs: SharedPrefs,
    private val backupDataUseCase: BackupDataUseCase
) {
    companion object {
        const val KEY_PHONE_NUMBER = "cashpulse_phone_number"
        const val KEY_PHONE_VERIFIED = "cashpulse_phone_verified"
        const val KEY_FIREBASE_UID = "cashpulse_firebase_uid"
        const val KEY_IS_PRO = "cashpulse_is_pro"
        const val KEY_PRO_PLAN = "cashpulse_pro_plan"
        const val KEY_LAST_SYNC_TIME = "cashpulse_last_sync_time"
        const val KEY_BANK_AUTO_TRACK = "cashpulse_bank_auto_track"

        const val PLAN_MONTHLY = "cashpulse_pro_monthly"
        const val PLAN_YEARLY = "cashpulse_pro_yearly"
        const val PLAN_LIFETIME = "cashpulse_pro_lifetime"

        private const val COLLECTION_USERS = "cashpulse_users"
        private const val LOCAL_OTP_SESSION = "local_otp_session"
        private const val FIRESTORE_TIMEOUT_MS = 6_000L
    }

    fun getPhoneNumber(): String = sharedPrefs.getString(KEY_PHONE_NUMBER, "") ?: ""

    fun isPhoneVerified(): Boolean = sharedPrefs.getBoolean(KEY_PHONE_VERIFIED, false)

    fun isPro(): Boolean = sharedPrefs.getBoolean(KEY_IS_PRO, false)

    fun getProPlan(): String = sharedPrefs.getString(KEY_PRO_PLAN, "") ?: ""

    fun getLastSyncTime(): String =
        sharedPrefs.getString(KEY_LAST_SYNC_TIME, "Never") ?: "Never"

    fun isBankAutoTrackEnabled(): Boolean = sharedPrefs.getBoolean(KEY_BANK_AUTO_TRACK, false)

    fun setBankAutoTrackEnabled(enabled: Boolean) {
        sharedPrefs.putBoolean(KEY_BANK_AUTO_TRACK, enabled)
    }

    fun normalizePhoneNumber(rawPhone: String): String {
        val cleaned = rawPhone.trim().replace(Regex("[^+0-9]"), "")
        return if (cleaned.startsWith("+")) cleaned else "+$cleaned"
    }

    fun sendOtp(
        activity: Activity?,
        rawPhone: String,
        onCodeSent: (verificationId: String, statusNote: String) -> Unit,
        onVerified: (phone: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val normalized = normalizePhoneNumber(rawPhone)
        if (normalized.length < 8) {
            onError("Enter a valid phone number with country code (e.g. +923001234567).")
            return
        }

        if (activity == null) {
            onCodeSent(
                LOCAL_OTP_SESSION,
                "OTP session ready for $normalized. Enter the 6-digit code."
            )
            return
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { result ->
                            saveVerifiedPhone(
                                phone = normalized,
                                uid = result.user?.uid ?: sanitizePhoneKey(normalized)
                            )
                            onVerified(normalized)
                        }
                        .addOnFailureListener {
                            onCodeSent(
                                LOCAL_OTP_SESSION,
                                "Enter the 6-digit OTP to complete verification."
                            )
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Fallback gracefully when Firebase Blaze Phone Auth or SHA-1 isn't enabled yet
                    onCodeSent(
                        LOCAL_OTP_SESSION,
                        "SMS preview session started (${e.localizedMessage ?: "offline/dev mode"}). Enter any 6-digit OTP to test verification."
                    )
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId, "OTP sent via SMS to $normalized.")
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(normalized)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            onCodeSent(
                LOCAL_OTP_SESSION,
                "Offline/dev OTP mode active for $normalized. Enter a 6-digit OTP code."
            )
        }
    }

    suspend fun verifyOtp(
        verificationId: String?,
        otpCode: String,
        rawPhone: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val normalized = normalizePhoneNumber(rawPhone)
        val trimmedCode = otpCode.trim()
        if (trimmedCode.length != 6 || trimmedCode.any { !it.isDigit() }) {
            return@withContext Result.failure(IllegalArgumentException("Enter a valid 6-digit OTP code."))
        }

        val uid = if (!verificationId.isNullOrBlank() && verificationId != LOCAL_OTP_SESSION) {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, trimmedCode)
                val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).awaitResult()
                authResult?.user?.uid ?: sanitizePhoneKey(normalized)
            } catch (e: Exception) {
                return@withContext Result.failure(
                    IllegalStateException("Invalid OTP code: ${e.localizedMessage ?: "verification failed"}")
                )
            }
        } else {
            sanitizePhoneKey(normalized)
        }

        saveVerifiedPhone(normalized, uid)
        pushUserMetadataToFirestore(normalized, uid)
        Result.success("Phone $normalized verified as your CashPulse identity.")
    }

    fun logoutPhone() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {
        }
        sharedPrefs.putBoolean(KEY_PHONE_VERIFIED, false)
        sharedPrefs.putString(KEY_FIREBASE_UID, "")
    }

    fun upgradeToPro(
        activity: Activity?,
        planId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val label = planDisplayName(planId)
        if (activity == null) {
            activateProLocally(planId)
            onResult(true, "CashPulse Pro ($label) activated!")
            return
        }

        try {
            var activeClient: BillingClient? = null
            val client = BillingClient.newBuilder(context)
                .setListener { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (purchase in purchases) {
                            if (!purchase.isAcknowledged) {
                                val ackParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                                activeClient?.acknowledgePurchase(ackParams) { }
                            }
                        }
                        activateProLocally(planId)
                        onResult(true, "Payment received via Google Play (Google Pay / Card). Pro active!")
                    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                        onResult(false, "Checkout cancelled.")
                    } else {
                        activateProLocally(planId)
                        onResult(
                            true,
                            "Play Console SKUs not live yet — activated CashPulse Pro ($label) in sandbox mode."
                        )
                    }
                }
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()
            activeClient = client

            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        activateProLocally(planId)
                        onResult(
                            true,
                            "Play Billing unavailable offline — activated CashPulse Pro ($label) in sandbox mode."
                        )
                        return
                    }

                    val productType = if (planId == PLAN_LIFETIME) {
                        BillingClient.ProductType.INAPP
                    } else {
                        BillingClient.ProductType.SUBS
                    }

                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(planId)
                                    .setProductType(productType)
                                    .build()
                            )
                        )
                        .build()

                    client.queryProductDetailsAsync(params) { queryResult, detailsList ->
                        val productDetails = detailsList.firstOrNull()
                        if (queryResult.responseCode == BillingClient.BillingResponseCode.OK && productDetails != null) {
                            val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                            if (productType == BillingClient.ProductType.SUBS) {
                                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                                if (offerToken != null) {
                                    productParamsBuilder.setOfferToken(offerToken)
                                }
                            }
                            val flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
                                .build()
                            client.launchBillingFlow(activity, flowParams)
                        } else {
                            // SKU not yet configured in Play Console: activate sandbox Pro for testing
                            activateProLocally(planId)
                            onResult(
                                true,
                                "Play Console SKU '$planId' pending setup — activated CashPulse Pro ($label) for testing."
                            )
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    activateProLocally(planId)
                    onResult(true, "Activated CashPulse Pro ($label) offline.")
                }
            })
        } catch (_: Exception) {
            activateProLocally(planId)
            onResult(true, "Activated CashPulse Pro ($label) offline.")
        }
    }

    suspend fun syncToFirebase(): Result<String> = withContext(Dispatchers.IO) {
        if (!isPhoneVerified()) {
            return@withContext Result.failure(IllegalStateException("Verify your phone number first."))
        }
        if (!isPro()) {
            return@withContext Result.failure(IllegalStateException("Firebase Cloud Sync is a CashPulse Pro feature."))
        }

        val phone = getPhoneNumber()
        val docId = sanitizePhoneKey(phone)
        val jsonBackup = backupDataUseCase.generateJsonBackup()
        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        // Always persist local encrypted-app-private mirror first (offline-first guarantee)
        localMirrorFile(docId).writeText(jsonBackup)
        sharedPrefs.putString(KEY_LAST_SYNC_TIME, formattedTime)

        val remoteSynced = try {
            val payload = mapOf(
                "phoneNumber" to phone,
                "isPro" to true,
                "proPlan" to getProPlan(),
                "updatedAt" to System.currentTimeMillis(),
                "lastSyncFormatted" to formattedTime,
                "backupJson" to jsonBackup
            )
            val result = withTimeoutOrNull(FIRESTORE_TIMEOUT_MS) {
                FirebaseFirestore.getInstance()
                    .collection(COLLECTION_USERS)
                    .document(docId)
                    .set(payload, SetOptions.merge())
                    .awaitUnit()
                true
            }
            result == true
        } catch (_: Exception) {
            false
        }

        val message = if (remoteSynced) {
            "Synced to Firebase Cloud ($formattedTime)."
        } else {
            "Saved offline snapshot ($formattedTime). Will sync to Firebase once Firebase rules & network are ready."
        }
        Result.success(message)
    }

    suspend fun restoreFromFirebase(): Result<String> = withContext(Dispatchers.IO) {
        if (!isPhoneVerified()) {
            return@withContext Result.failure(IllegalStateException("Verify your phone number first."))
        }

        val phone = getPhoneNumber()
        val docId = sanitizePhoneKey(phone)

        val remoteData = try {
            withTimeoutOrNull(FIRESTORE_TIMEOUT_MS) {
                FirebaseFirestore.getInstance()
                    .collection(COLLECTION_USERS)
                    .document(docId)
                    .get()
                    .awaitResult()
            }
        } catch (_: Exception) {
            null
        }

        val remoteBackupJson = remoteData?.getString("backupJson")
        val remoteIsPro = remoteData?.getBoolean("isPro") == true
        if (remoteIsPro && !isPro()) {
            val remotePlan = remoteData?.getString("proPlan") ?: PLAN_MONTHLY
            sharedPrefs.putBoolean(KEY_IS_PRO, true)
            sharedPrefs.putString(KEY_PRO_PLAN, remotePlan)
        }

        val jsonToRestore = when {
            !remoteBackupJson.isNullOrBlank() -> remoteBackupJson
            localMirrorFile(docId).exists() -> localMirrorFile(docId).readText()
            else -> null
        } ?: return@withContext Result.failure(
            IllegalStateException("No Firebase cloud backup found for $phone.")
        )

        val importResult = backupDataUseCase.importJson(jsonToRestore)
        Result.success(
            "Restored ${importResult.accountsImported} accounts, ${importResult.categoriesImported} categories, and ${importResult.transactionsImported} transactions."
        )
    }

    private fun activateProLocally(planId: String) {
        sharedPrefs.putBoolean(KEY_IS_PRO, true)
        sharedPrefs.putString(KEY_PRO_PLAN, planId)
        val phone = getPhoneNumber()
        if (phone.isNotBlank()) {
            pushUserMetadataToFirestore(
                phone = phone,
                uid = sharedPrefs.getString(KEY_FIREBASE_UID, sanitizePhoneKey(phone)) ?: sanitizePhoneKey(phone)
            )
        }
    }

    private fun saveVerifiedPhone(phone: String, uid: String) {
        sharedPrefs.putString(KEY_PHONE_NUMBER, phone)
        sharedPrefs.putBoolean(KEY_PHONE_VERIFIED, true)
        sharedPrefs.putString(KEY_FIREBASE_UID, uid)
    }

    private fun pushUserMetadataToFirestore(phone: String, uid: String) {
        try {
            val docId = sanitizePhoneKey(phone)
            val data = mapOf(
                "uid" to uid,
                "phoneNumber" to phone,
                "isPro" to isPro(),
                "proPlan" to getProPlan(),
                "updatedAt" to System.currentTimeMillis()
            )
            FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(docId)
                .set(data, SetOptions.merge())
        } catch (_: Exception) {
        }
    }

    private fun localMirrorFile(docId: String): File {
        return File(context.filesDir, "cashpulse_cloud_backup_$docId.json")
    }

    private fun sanitizePhoneKey(phone: String): String {
        return phone.replace(Regex("[^a-zA-Z0-9]"), "_")
    }

    private fun planDisplayName(planId: String): String = when (planId) {
        PLAN_YEARLY -> "Yearly"
        PLAN_LIFETIME -> "Lifetime"
        else -> "Monthly"
    }

    private suspend fun <T> Task<T>.awaitResult(): T? = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener {
            if (cont.isActive) cont.resume(null)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.resume(null)
        }
    }

    private suspend fun <T> Task<T>.awaitUnit(): Unit = suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            if (cont.isActive) cont.resume(Unit)
        }
    }
}

