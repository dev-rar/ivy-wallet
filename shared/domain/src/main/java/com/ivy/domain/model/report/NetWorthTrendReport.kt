package com.ivy.domain.model.report

data class NetWorthTrendReport(
    val baseCurrency: String,
    val currentNetWorth: Double,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val points: List<NetWorthTrendPoint>
)

data class NetWorthTrendPoint(
    val label: String,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double
)

