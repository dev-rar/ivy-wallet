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
    tableName = "investment_valuations",
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["valuationDate"])
    ]
)
data class InvestmentValuationEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
    @SerialName("accountId")
    @Serializable(with = KSerializerUUID::class)
    val accountId: UUID,
    @SerialName("valuationDate")
    @Serializable(with = KSerializerInstant::class)
    val valuationDate: Instant = Instant.now(),
    @SerialName("valuationAmount")
    val valuationAmount: Double = 0.0,
    @SerialName("notes")
    val notes: String? = null
)

