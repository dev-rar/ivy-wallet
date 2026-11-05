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
    tableName = "budget_items",
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["planPeriod"]),
        Index(value = ["categoryId"])
    ]
)
data class BudgetItemEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
    @SerialName("groupId")
    @Serializable(with = KSerializerUUID::class)
    val groupId: UUID,
    @SerialName("planPeriod")
    val planPeriod: String, // Format: YYYY-MM
    @SerialName("name")
    val name: String,
    @SerialName("plannedAmount")
    val plannedAmount: Double = 0.0,
    @SerialName("itemType")
    val itemType: String = "STANDARD",
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID? = null,
    @SerialName("orderNum")
    val orderNum: Double = 0.0
)
