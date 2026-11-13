package com.ivy.settings

import android.app.Activity
import com.ivy.domain.RootScreen

sealed interface SettingsEvent {
    data class SetCurrency(val newCurrency: String) : SettingsEvent
    data class SetName(val newName: String) : SettingsEvent
    data class ExportToCsv(val rootScreen: RootScreen) : SettingsEvent
    data class BackupData(val rootScreen: RootScreen) : SettingsEvent
    data object SwitchTheme : SettingsEvent
    data class SetLockApp(val lockApp: Boolean) : SettingsEvent
    data class SetShowNotifications(val showNotifications: Boolean) : SettingsEvent
    data class SetHideCurrentBalance(val hideCurrentBalance: Boolean) : SettingsEvent
    data class SetHideIncome(val hideIncome: Boolean) : SettingsEvent
    data class SetTransfersAsIncomeExpense(val treatTransfersAsIncomeExpense: Boolean) :
        SettingsEvent

    data class SetStartDateOfMonth(val startDate: Int) : SettingsEvent
    data object DeleteCloudUserData : SettingsEvent
    data object DeleteAllUserData : SettingsEvent
    data object SwitchLanguage : SettingsEvent

    data class SendPhoneOtp(val activity: Activity?, val phoneNumber: String) : SettingsEvent
    data class VerifyPhoneOtp(val phoneNumber: String, val otpCode: String) : SettingsEvent
    data object LogoutPhone : SettingsEvent
    data class UpgradeToPro(val activity: Activity?, val planId: String) : SettingsEvent
    data object SyncToFirebaseCloud : SettingsEvent
    data object RestoreFromFirebaseCloud : SettingsEvent
    data class SetBankAutoTrack(val enabled: Boolean) : SettingsEvent
    data object ClearCloudMessage : SettingsEvent
}
