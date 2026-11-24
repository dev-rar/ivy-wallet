package com.ivy.subscription

import androidx.compose.runtime.Immutable

@Immutable
data class SubscriptionState(
    val phoneNumber: String = "",
    val isPhoneVerified: Boolean = false,
    val isPro: Boolean = false,
    val proPlan: String = "",
    val lastCloudSync: String = "Never",
    val bankAutoTrackEnabled: Boolean = false,
    val statusMessage: String? = null,
    val otpSent: Boolean = false,
    val isLoading: Boolean = false
)
