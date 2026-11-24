package com.ivy.subscription

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.base.legacy.SharedPrefs
import com.ivy.data.cloud.CashPulseCloudAccountManager
import com.ivy.navigation.Navigation
import com.ivy.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val cloudAccountManager: CashPulseCloudAccountManager,
    @Suppress("unused") private val sharedPrefs: SharedPrefs,
    private val nav: Navigation,
    @ApplicationContext private val context: Context
) : ComposeViewModel<SubscriptionState, SubscriptionEvent>() {

    private var phoneNumber by mutableStateOf(cloudAccountManager.getPhoneNumber())
    private var isPhoneVerified by mutableStateOf(cloudAccountManager.isPhoneVerified())
    private var isPro by mutableStateOf(cloudAccountManager.isPro())
    private var proPlan by mutableStateOf(cloudAccountManager.getProPlan())
    private var lastCloudSync by mutableStateOf(cloudAccountManager.getLastSyncTime())
    private var bankAutoTrackEnabled by mutableStateOf(cloudAccountManager.isBankAutoTrackEnabled())
    private var statusMessage by mutableStateOf<String?>(null)
    private var pendingVerificationId by mutableStateOf<String?>(null)
    private var isLoading by mutableStateOf(false)
    private var launchedFromOnboarding by mutableStateOf(false)
    private var hasRedirectedPaidUser = false

    fun bindLaunchContext(fromOnboarding: Boolean) {
        launchedFromOnboarding = fromOnboarding
    }

    fun redirectPaidUser() {
        if (hasRedirectedPaidUser || !cloudAccountManager.isPro()) return
        hasRedirectedPaidUser = true
        if (launchedFromOnboarding) {
            cloudAccountManager.pendingContinueOnboarding = true
        }
        nav.back()
    }

    @Composable
    override fun uiState(): SubscriptionState {
        LaunchedEffect(Unit) {
            refreshState()
        }
        return SubscriptionState(
            phoneNumber = phoneNumber,
            isPhoneVerified = isPhoneVerified,
            isPro = isPro,
            proPlan = proPlan,
            lastCloudSync = lastCloudSync,
            bankAutoTrackEnabled = bankAutoTrackEnabled,
            statusMessage = statusMessage,
            otpSent = pendingVerificationId != null,
            isLoading = isLoading
        )
    }

    private fun refreshState() {
        phoneNumber = cloudAccountManager.getPhoneNumber()
        isPhoneVerified = cloudAccountManager.isPhoneVerified()
        isPro = cloudAccountManager.isPro()
        proPlan = cloudAccountManager.getProPlan()
        lastCloudSync = cloudAccountManager.getLastSyncTime()
        bankAutoTrackEnabled = cloudAccountManager.isBankAutoTrackEnabled()
    }

    override fun onEvent(event: SubscriptionEvent) {
        when (event) {
            is SubscriptionEvent.SendMobileOtp -> sendMobileOtp(event.activity, event.phoneNumber)
            is SubscriptionEvent.VerifyMobileOtp -> verifyMobileOtp(
                rawPhone = event.phoneNumber,
                otpCode = event.otpCode,
                fromOnboarding = event.launchedFromOnboarding
            )
            is SubscriptionEvent.SelectPlan -> selectPlan(event.activity)
            SubscriptionEvent.BackupCloud -> backupCloud()
            SubscriptionEvent.RestoreCloud -> restoreCloud()
            is SubscriptionEvent.SetBankAutoTrack -> setBankAutoTrack(event.enabled)
            SubscriptionEvent.SignOutOnline -> signOutOnline()
            SubscriptionEvent.ResetOtpStep -> {
                pendingVerificationId = null
                statusMessage = null
            }
            SubscriptionEvent.ContinueToWalletSetup -> continueToWalletSetup()
            SubscriptionEvent.ClearMessage -> statusMessage = null
        }
    }

    private fun sendMobileOtp(activity: Activity?, rawPhone: String) {
        statusMessage = "Sending verification code..."
        cloudAccountManager.sendOtp(
            activity = activity,
            rawPhone = rawPhone,
            onCodeSent = { verificationId, note ->
                pendingVerificationId = verificationId
                statusMessage = note
            },
            onVerified = { _ ->
                pendingVerificationId = null
                refreshState()
                viewModelScope.launch {
                    handlePostVerification(fromOnboarding = launchedFromOnboarding)
                }
            },
            onError = { message ->
                statusMessage = message
            }
        )
    }

    private fun verifyMobileOtp(
        rawPhone: String,
        otpCode: String,
        fromOnboarding: Boolean
    ) {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Verifying code & checking your cloud backup..."
            val result = cloudAccountManager.verifyOtp(
                verificationId = pendingVerificationId,
                otpCode = otpCode,
                rawPhone = rawPhone
            )
            isLoading = false
            result.fold(
                onSuccess = {
                    pendingVerificationId = null
                    refreshState()
                    handlePostVerification(fromOnboarding = fromOnboarding)
                },
                onFailure = { err ->
                    statusMessage = err.localizedMessage ?: "Verification failed. Please check the code."
                }
            )
        }
    }

    private suspend fun handlePostVerification(fromOnboarding: Boolean) {
        val backup = cloudAccountManager.checkCloudBackup()
        cloudAccountManager.detectedCloudBackup = backup?.takeIf { it.hasBackupData }
        refreshState()

        if (fromOnboarding && (cloudAccountManager.detectedCloudBackup != null || cloudAccountManager.isPro())) {
            cloudAccountManager.pendingContinueOnboarding = true
            nav.back()
            return
        }

        if (fromOnboarding) {
            statusMessage = "Mobile number verified! Choose a tier below or continue to wallet setup."
        } else if (cloudAccountManager.isPro()) {
            redirectPaidUser()
        } else {
            statusMessage = if (backup != null && backup.hasBackupData) {
                "Mobile number verified. Cloud backup from ${backup.lastSyncFormatted} is linked to your account."
            } else {
                "Mobile number verified."
            }
        }
    }

    private fun selectPlan(activity: Activity?) {
        if (cloudAccountManager.isPro()) {
            redirectPaidUser()
            return
        }
        cloudAccountManager.upgradeToPro(
            activity = activity,
            planId = CashPulseCloudAccountManager.PLAN_LIFETIME
        ) { success, msg ->
            viewModelScope.launch {
                statusMessage = msg
                refreshState()
                if (success && cloudAccountManager.isPro()) {
                    redirectPaidUser()
                }
            }
        }
    }

    private fun backupCloud() {
        viewModelScope.launch {
            isLoading = true
            val result = cloudAccountManager.syncToFirebase()
            isLoading = false
            result.fold(
                onSuccess = { msg ->
                    statusMessage = msg
                    refreshState()
                },
                onFailure = { err ->
                    statusMessage = err.localizedMessage ?: "Cloud backup failed. Please try again."
                }
            )
        }
    }

    private fun restoreCloud() {
        viewModelScope.launch {
            isLoading = true
            val result = cloudAccountManager.restoreFromFirebase()
            isLoading = false
            result.fold(
                onSuccess = { msg ->
                    statusMessage = msg
                    refreshState()
                },
                onFailure = { err ->
                    statusMessage = err.localizedMessage ?: "Cloud restore failed. Please try again."
                }
            )
        }
    }

    private fun setBankAutoTrack(enabled: Boolean) {
        if (enabled && !cloudAccountManager.isPro()) {
            statusMessage = "Upgrade to Paid (\$100 one-time) to unlock Bank Alert Auto-Tracking."
            return
        }
        cloudAccountManager.setBankAutoTrackEnabled(enabled)
        bankAutoTrackEnabled = enabled
        if (enabled) {
            statusMessage = "Enable 'CashPulse Bank Auto-Tracker' in Notification Access settings to auto-log bank alerts."
            try {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }

    private fun signOutOnline() {
        cloudAccountManager.logoutPhone()
        pendingVerificationId = null
        refreshState()
        statusMessage = "Signed out of online account. Your local data remains safe on this device."
    }

    private fun continueToWalletSetup() {
        cloudAccountManager.pendingContinueOnboarding = true
        nav.back()
    }
}
