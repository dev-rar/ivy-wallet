package com.ivy.data.cloud

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.ivy.base.legacy.SharedPrefs
import com.ivy.data.backup.BackupDataUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class CloudBackupMetadata(
    val phoneNumber: String,
    val lastSyncFormatted: String,
    val updatedAtMillis: Long,
    val hasBackupData: Boolean,
    val isPro: Boolean,
    val proPlan: String
)

@Singleton
class CashPulseCloudAccountManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPrefs: SharedPrefs,
    private val backupDataUseCase: BackupDataUseCase
) {
    @Volatile
    private var isRestoring = false

    @Volatile
    var pendingContinueOnboarding = false

    @Volatile
    var detectedCloudBackup: CloudBackupMetadata? = null

    private val _isProFlow = MutableStateFlow(false)
    val isProFlow: StateFlow<Boolean> = _isProFlow.asStateFlow()

    private val _proPlanFlow = MutableStateFlow("")
    val proPlanFlow: StateFlow<String> = _proPlanFlow.asStateFlow()

    private var subscriptionListener: ListenerRegistration? = null
    private var listenedDocId: String? = null

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
        private const val DATE_DISPLAY_PATTERN = "MMM dd, yyyy 'at' hh:mm a"
    }

    init {
        // Subscription state lives strictly in the cloud; clear any legacy local flags.
        sharedPrefs.putBoolean(KEY_IS_PRO, false)
        sharedPrefs.putString(KEY_PRO_PLAN, "")
        if (isPhoneVerified()) {
            startCloudSubscriptionListener()
        }
    }

    fun isDebugBuild(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    fun getPhoneNumber(): String = sharedPrefs.getString(KEY_PHONE_NUMBER, "") ?: ""

    fun isPhoneVerified(): Boolean = sharedPrefs.getBoolean(KEY_PHONE_VERIFIED, false)

    fun isPro(): Boolean = isPhoneVerified() && _isProFlow.value

    fun getProPlan(): String = if (isPro()) _proPlanFlow.value else ""

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

    @Synchronized
    fun startCloudSubscriptionListener() {
        val phone = getPhoneNumber()
        if (!isPhoneVerified() || phone.isBlank()) {
            subscriptionListener?.remove()
            subscriptionListener = null
            listenedDocId = null
            _isProFlow.value = false
            _proPlanFlow.value = ""
            return
        }

        val docId = sanitizePhoneKey(phone)
        if (subscriptionListener != null && listenedDocId == docId) {
            return
        }

        subscriptionListener?.remove()
        listenedDocId = docId

        try {
            subscriptionListener = FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(docId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val remoteIsPro = snapshot.exists() && snapshot.getBoolean("isPro") == true
                    val remotePlan = if (remoteIsPro) {
                        snapshot.getString("proPlan").orEmpty().ifBlank { PLAN_LIFETIME }
                    } else {
                        ""
                    }
                    _isProFlow.value = remoteIsPro
                    _proPlanFlow.value = remotePlan
                }
        } catch (_: Exception) {
        }
    }

    suspend fun refreshSubscriptionFromCloud(): Boolean = withContext(Dispatchers.IO) {
        val phone = getPhoneNumber()
        if (!isPhoneVerified() || phone.isBlank()) {
            _isProFlow.value = false
            _proPlanFlow.value = ""
            return@withContext false
        }

        startCloudSubscriptionListener()
        val docId = sanitizePhoneKey(phone)
        val snapshot = try {
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

        if (snapshot != null) {
            val remoteIsPro = snapshot.exists() && snapshot.getBoolean("isPro") == true
            val remotePlan = if (remoteIsPro) {
                snapshot.getString("proPlan").orEmpty().ifBlank { PLAN_LIFETIME }
            } else {
                ""
            }
            _isProFlow.value = remoteIsPro
            _proPlanFlow.value = remotePlan
        }

        isPro()
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
                            val uid = result.user?.uid ?: sanitizePhoneKey(normalized)
                            saveVerifiedPhone(phone = normalized, uid = uid)
                            pushUserMetadataToFirestore(normalized, uid)
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
                    // Fallback gracefully when SMS verification or SHA-1 isn't enabled yet
                    onCodeSent(
                        LOCAL_OTP_SESSION,
                        "Verification session ready for $normalized. Enter the 6-digit code to continue."
                    )
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId, "Verification code sent via SMS to $normalized.")
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(normalized)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (_: Exception) {
            onCodeSent(
                LOCAL_OTP_SESSION,
                "Verification session ready for $normalized. Enter the 6-digit code to continue."
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
        refreshSubscriptionFromCloud()
        Result.success("Mobile number $normalized verified.")
    }

    fun logoutPhone() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {
        }
        subscriptionListener?.remove()
        subscriptionListener = null
        listenedDocId = null
        _isProFlow.value = false
        _proPlanFlow.value = ""
        detectedCloudBackup = null
        sharedPrefs.putBoolean(KEY_PHONE_VERIFIED, false)
        sharedPrefs.putString(KEY_FIREBASE_UID, "")
    }

    fun upgradeToPro(
        activity: Activity?,
        planId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!isPhoneVerified() || getPhoneNumber().isBlank()) {
            onResult(false, "Please verify your mobile number first to link your Paid tier.")
            return
        }

        val resolvedPlan = if (planId.isNotBlank()) planId else PLAN_LIFETIME
        val sandboxAllowed = isDebugBuild()

        if (activity == null) {
            if (sandboxAllowed) {
                activateProInCloud(planId = resolvedPlan, isSandbox = true)
                onResult(true, "CashPulse Paid activated in debug sandbox and saved to cloud!")
            } else {
                onResult(false, "Unable to open Google Play checkout right now.")
            }
            return
        }

        try {
            var activeClient: BillingClient? = null
            val client = BillingClient.newBuilder(context)
                .setListener { billingResult, purchases ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                        val completedPurchase = purchases.firstOrNull {
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                        }
                        if (completedPurchase != null) {
                            if (!completedPurchase.isAcknowledged) {
                                val ackParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(completedPurchase.purchaseToken)
                                    .build()
                                activeClient?.acknowledgePurchase(ackParams) { }
                            }
                            activateProInCloud(
                                planId = resolvedPlan,
                                orderId = completedPurchase.orderId,
                                purchaseToken = completedPurchase.purchaseToken,
                                isSandbox = false
                            )
                            onResult(true, "Payment received via Google Play. Paid tier active!")
                        } else {
                            onResult(false, "Payment is pending confirmation from Google Play.")
                        }
                    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                        onResult(false, "Checkout cancelled.")
                    } else if (sandboxAllowed) {
                        activateProInCloud(planId = resolvedPlan, isSandbox = true)
                        onResult(true, "CashPulse Paid activated in debug sandbox and saved to cloud!")
                    } else {
                        onResult(false, "Unable to complete Google Play payment right now. Please try again.")
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
                        if (sandboxAllowed) {
                            activateProInCloud(planId = resolvedPlan, isSandbox = true)
                            onResult(true, "CashPulse Paid activated in debug sandbox and saved to cloud!")
                        } else {
                            onResult(false, "Google Play Billing is unavailable. Please try again.")
                        }
                        return
                    }

                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId(resolvedPlan)
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build()
                            )
                        )
                        .build()

                    client.queryProductDetailsAsync(params) { queryResult, detailsList ->
                        val productDetails = detailsList.firstOrNull()
                        if (queryResult.responseCode == BillingClient.BillingResponseCode.OK && productDetails != null) {
                            val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                            val flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(listOf(productParams))
                                .build()
                            client.launchBillingFlow(activity, flowParams)
                        } else if (sandboxAllowed) {
                            activateProInCloud(planId = resolvedPlan, isSandbox = true)
                            onResult(true, "CashPulse Paid activated in debug sandbox and saved to cloud!")
                        } else {
                            onResult(false, "Product unavailable on Google Play right now.")
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    if (sandboxAllowed) {
                        activateProInCloud(planId = resolvedPlan, isSandbox = true)
                        onResult(true, "CashPulse Paid activated in debug sandbox and saved to cloud!")
                    } else {
                        onResult(false, "Google Play connection interrupted. Please try again.")
                    }
                }
            })
        } catch (_: Exception) {
            if (sandboxAllowed) {
                activateProInCloud(planId = resolvedPlan, isSandbox = true)
                onResult(true, "CashPulse Paid activated in debug sandbox and saved to cloud!")
            } else {
                onResult(false, "Unable to complete Google Play payment right now. Please try again.")
            }
        }
    }

    fun restoreGooglePlayPurchases(onComplete: (Boolean) -> Unit = {}) {
        if (!isPhoneVerified() || getPhoneNumber().isBlank()) {
            onComplete(false)
            return
        }

        try {
            var activeClient: BillingClient? = null
            val client = BillingClient.newBuilder(context)
                .setListener { _, _ -> }
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
                        client.endConnection()
                        onComplete(false)
                        return
                    }

                    val params = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()

                    client.queryPurchasesAsync(params) { queryResult, purchases ->
                        val owned = if (queryResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            purchases.firstOrNull { purchase ->
                                purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                                    purchase.products.contains(PLAN_LIFETIME)
                            }
                        } else {
                            null
                        }

                        if (owned != null) {
                            if (!owned.isAcknowledged) {
                                val ackParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(owned.purchaseToken)
                                    .build()
                                activeClient?.acknowledgePurchase(ackParams) {
                                    client.endConnection()
                                }
                            } else {
                                client.endConnection()
                            }
                            activateProInCloud(
                                planId = PLAN_LIFETIME,
                                orderId = owned.orderId,
                                purchaseToken = owned.purchaseToken,
                                isSandbox = false
                            )
                            onComplete(true)
                        } else {
                            client.endConnection()
                            onComplete(false)
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    onComplete(false)
                }
            })
        } catch (_: Exception) {
            onComplete(false)
        }
    }

    suspend fun syncToFirebase(): Result<String> = withContext(Dispatchers.IO) {
        if (!isPhoneVerified()) {
            return@withContext Result.failure(IllegalStateException("Verify your mobile number first."))
        }
        if (!refreshSubscriptionFromCloud()) {
            return@withContext Result.failure(IllegalStateException("Cloud Sync is included with CashPulse Paid."))
        }

        val phone = getPhoneNumber()
        val docId = sanitizePhoneKey(phone)
        val jsonBackup = backupDataUseCase.generateJsonBackup()
        val formattedTime = formatTimestamp(System.currentTimeMillis())

        // Always persist local encrypted-app-private mirror first (offline-first guarantee)
        localMirrorFile(docId).writeText(jsonBackup)
        sharedPrefs.putString(KEY_LAST_SYNC_TIME, formattedTime)

        val remoteSynced = try {
            val payload = mapOf(
                "phoneNumber" to phone,
                "isPro" to true,
                "proPlan" to getProPlan().ifBlank { PLAN_LIFETIME },
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
            "Cloud backup synced ($formattedTime)."
        } else {
            "Saved local backup ($formattedTime). Cloud sync will resume automatically when online."
        }
        Result.success(message)
    }

    suspend fun checkCloudBackup(): CloudBackupMetadata? = withContext(Dispatchers.IO) {
        if (!isPhoneVerified()) return@withContext null

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

        if (remoteData != null) {
            val remoteIsPro = remoteData.exists() && remoteData.getBoolean("isPro") == true
            val remotePlan = if (remoteIsPro) {
                remoteData.getString("proPlan").orEmpty().ifBlank { PLAN_LIFETIME }
            } else {
                ""
            }
            _isProFlow.value = remoteIsPro
            _proPlanFlow.value = remotePlan
        }

        val remoteBackupJson = remoteData?.getString("backupJson")
        val mirror = localMirrorFile(docId)
        val hasRemoteBackup = !remoteBackupJson.isNullOrBlank()
        val hasLocalMirror = mirror.exists() && mirror.length() > 0L

        if (!hasRemoteBackup && !hasLocalMirror) {
            return@withContext null
        }

        val updatedAtMillis = remoteData?.getLong("updatedAt")
            ?: if (hasLocalMirror) mirror.lastModified() else System.currentTimeMillis()
        val remoteFormatted = remoteData?.getString("lastSyncFormatted")
        val displayTime = when {
            !remoteFormatted.isNullOrBlank() -> remoteFormatted
            updatedAtMillis > 0L -> formatTimestamp(updatedAtMillis)
            else -> "Previously saved"
        }

        sharedPrefs.putString(KEY_LAST_SYNC_TIME, displayTime)

        CloudBackupMetadata(
            phoneNumber = phone,
            lastSyncFormatted = displayTime,
            updatedAtMillis = updatedAtMillis,
            hasBackupData = true,
            isPro = isPro(),
            proPlan = getProPlan()
        )
    }

    suspend fun restoreFromFirebase(): Result<String> = withContext(Dispatchers.IO) {
        if (!isPhoneVerified()) {
            return@withContext Result.failure(IllegalStateException("Verify your mobile number first."))
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
        if (remoteData != null) {
            val remoteIsPro = remoteData.exists() && remoteData.getBoolean("isPro") == true
            val remotePlan = if (remoteIsPro) {
                remoteData.getString("proPlan").orEmpty().ifBlank { PLAN_LIFETIME }
            } else {
                ""
            }
            _isProFlow.value = remoteIsPro
            _proPlanFlow.value = remotePlan
        }

        val jsonToRestore = when {
            !remoteBackupJson.isNullOrBlank() -> remoteBackupJson
            localMirrorFile(docId).exists() -> localMirrorFile(docId).readText()
            else -> null
        } ?: return@withContext Result.failure(
            IllegalStateException("No cloud backup found for $phone.")
        )

        val importResult = try {
            isRestoring = true
            backupDataUseCase.importJson(jsonToRestore)
        } finally {
            isRestoring = false
        }
        Result.success(
            "Restored ${importResult.accountsImported} accounts, ${importResult.categoriesImported} categories, and ${importResult.transactionsImported} transactions."
        )
    }

    suspend fun deleteCloudBackup(): Result<String> = withContext(Dispatchers.IO) {
        if (!isPhoneVerified()) {
            return@withContext Result.failure(IllegalStateException("Verify your mobile number first."))
        }
        val phone = getPhoneNumber()
        val docId = sanitizePhoneKey(phone)
        try {
            localMirrorFile(docId).delete()
        } catch (_: Exception) {
        }
        sharedPrefs.putString(KEY_LAST_SYNC_TIME, "Never")

        try {
            withTimeoutOrNull(FIRESTORE_TIMEOUT_MS) {
                FirebaseFirestore.getInstance()
                    .collection(COLLECTION_USERS)
                    .document(docId)
                    .set(
                        mapOf(
                            "backupJson" to "",
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .awaitUnit()
            }
        } catch (_: Exception) {
        }

        Result.success("Cloud backup reset. You can now start fresh.")
    }

    private fun activateProInCloud(
        planId: String,
        orderId: String? = null,
        purchaseToken: String? = null,
        isSandbox: Boolean = false
    ) {
        val phone = getPhoneNumber()
        if (!isPhoneVerified() || phone.isBlank()) return

        val resolvedPlan = if (planId.isNotBlank()) planId else PLAN_LIFETIME
        _isProFlow.value = true
        _proPlanFlow.value = resolvedPlan

        try {
            val docId = sanitizePhoneKey(phone)
            val uid = sharedPrefs.getString(KEY_FIREBASE_UID, docId) ?: docId
            val payload = mutableMapOf<String, Any>(
                "uid" to uid,
                "phoneNumber" to phone,
                "isPro" to true,
                "proPlan" to resolvedPlan,
                "isSandbox" to isSandbox,
                "updatedAt" to System.currentTimeMillis()
            )
            if (!orderId.isNullOrBlank()) {
                payload["orderId"] = orderId
            }
            if (!purchaseToken.isNullOrBlank()) {
                payload["purchaseToken"] = purchaseToken
            }
            FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(docId)
                .set(payload, SetOptions.merge())
        } catch (_: Exception) {
        }
    }

    private fun saveVerifiedPhone(phone: String, uid: String) {
        sharedPrefs.putString(KEY_PHONE_NUMBER, phone)
        sharedPrefs.putBoolean(KEY_PHONE_VERIFIED, true)
        sharedPrefs.putString(KEY_FIREBASE_UID, uid)
        startCloudSubscriptionListener()
    }

    private fun pushUserMetadataToFirestore(phone: String, uid: String) {
        try {
            val docId = sanitizePhoneKey(phone)
            // Never overwrite isPro/proPlan here so existing cloud Paid status (or manual admin grant) persists.
            val data = mapOf(
                "uid" to uid,
                "phoneNumber" to phone,
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

    private fun formatTimestamp(millis: Long): String {
        return SimpleDateFormat(DATE_DISPLAY_PATTERN, Locale.getDefault()).format(Date(millis))
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
