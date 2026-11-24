package com.ivy.subscription

import android.app.Activity

sealed interface SubscriptionEvent {
    data class SendMobileOtp(
        val activity: Activity?,
        val phoneNumber: String
    ) : SubscriptionEvent

    data class VerifyMobileOtp(
        val phoneNumber: String,
        val otpCode: String,
        val launchedFromOnboarding: Boolean
    ) : SubscriptionEvent

    data class SelectPlan(
        val activity: Activity?,
        val planId: String
    ) : SubscriptionEvent

    data object BackupCloud : SubscriptionEvent
    data object RestoreCloud : SubscriptionEvent
    data class SetBankAutoTrack(val enabled: Boolean) : SubscriptionEvent
    data object SignOutOnline : SubscriptionEvent
    data object ResetOtpStep : SubscriptionEvent
    data object ContinueToWalletSetup : SubscriptionEvent
    data object ClearMessage : SubscriptionEvent
}
