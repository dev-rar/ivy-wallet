package com.ivy.data.db.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ivy.base.kotlinxserilzation.KSerializerInstant
import com.ivy.base.kotlinxserilzation.KSerializerUUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Keep
@Serializable
@Entity(
    tableName = "sinking_funds",
    indices = [
        Index(value = ["budgetItemId"], unique = true)
    ]
)
data class SinkingFundEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
    @SerialName("budgetItemId")
    @Serializable(with = KSerializerUUID::class)
    val budgetItemId: UUID,
    @SerialName("targetAmount")
    val targetAmount: Double = 0.0,
    @SerialName("targetDate")
    @Serializable(with = KSerializerInstant::class)
    val targetDate: Instant? = null,
    @SerialName("openingBalance")
    val openingBalance: Double = 0.0,
    @SerialName("notes")
    val notes: String? = null
)

