package com.ivy.settings

import com.ivy.base.legacy.Theme

@Suppress("DataClassDefaultValues")
data class SettingsState(
    val currencyCode: String,
    val name: String,
    val currentTheme: Theme,
    val lockApp: Boolean,
    val showNotifications: Boolean,
    val hideCurrentBalance: Boolean,
    val hideIncome: Boolean,
    val treatTransfersAsIncomeExpense: Boolean,
    val startDateOfMonth: String,
    val progressState: Boolean,
    val languageOptionVisible: Boolean,
    val phoneNumber: String = "",
    val isPhoneVerified: Boolean = false,
    val isPro: Boolean = false,
    val proPlan: String = "",
    val lastCloudSync: String = "Never",
    val bankAutoTrackEnabled: Boolean = false,
    val cloudStatusMessage: String? = null,
    val pendingVerificationId: String? = null
)
