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
    tableName = "budget_groups",
    indices = [
        Index(value = ["planPeriod"]),
        Index(value = ["planPeriod", "name"])
    ]
)
data class BudgetGroupEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
    @SerialName("planPeriod")
    val planPeriod: String, // Format: YYYY-MM
    @SerialName("name")
    val name: String,
    @SerialName("groupType")
    val groupType: String,
    @SerialName("orderNum")
    val orderNum: Double = 0.0
)
