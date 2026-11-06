package com.ivy.domain.usecase.report

import com.ivy.base.threading.DispatchersProvider
import com.ivy.domain.model.report.NetWorthTrendPoint
import com.ivy.domain.model.report.NetWorthTrendReport
import com.ivy.domain.usecase.account.CalculateNetWorthUseCase
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetNetWorthHistoryUseCase @Inject constructor(
    private val calculateNetWorthUseCase: CalculateNetWorthUseCase,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend operator fun invoke(): NetWorthTrendReport = withContext(dispatchersProvider.io) {
        val current = calculateNetWorthUseCase()

        // Generate trend summary points
        val currentPoint = NetWorthTrendPoint(
            label = "Current",
            totalAssets = current.totalAssets,
            totalLiabilities = current.totalLiabilities,
            netWorth = current.netWorth
        )

        NetWorthTrendReport(
            baseCurrency = current.baseCurrency,
            currentNetWorth = current.netWorth,
            totalAssets = current.totalAssets,
            totalLiabilities = current.totalLiabilities,
            points = listOf(currentPoint)
        )
    }
}

