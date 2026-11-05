package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Keep
@Serializable
@Entity(
    tableName = "monthly_budget_plans",
    indices = [
        Index(value = ["period"], unique = true)
    ]
)
data class MonthlyBudgetPlanEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
    @SerialName("period")
    val period: String, // Format: YYYY-MM
    @SerialName("plannedIncome")
    val plannedIncome: Double = 0.0,
    @SerialName("carryoverAmount")
    val carryoverAmount: Double = 0.0,
    @SerialName("isClosed")
    val isClosed: Boolean = false,
    @SerialName("createdAt")
    val createdAt: Long = System.currentTimeMillis()
)
