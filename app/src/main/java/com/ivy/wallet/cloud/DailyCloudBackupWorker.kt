package com.ivy.wallet.cloud

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ivy.data.cloud.CashPulseCloudAccountManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyCloudBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val cloudAccountManager: CashPulseCloudAccountManager
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (cloudAccountManager.isPro() && cloudAccountManager.isPhoneVerified()) {
            cloudAccountManager.syncToFirebase()
        }
        return Result.success()
    }
}
