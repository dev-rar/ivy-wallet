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
    tableName = "transaction_splits",
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["budgetItemId"]),
        Index(value = ["categoryId"])
    ]
)
data class TransactionSplitEntity(
    @PrimaryKey
    @SerialName("id")
    @Serializable(with = KSerializerUUID::class)
    val id: UUID = UUID.randomUUID(),
    @SerialName("transactionId")
    @Serializable(with = KSerializerUUID::class)
    val transactionId: UUID,
    @SerialName("budgetItemId")
    @Serializable(with = KSerializerUUID::class)
    val budgetItemId: UUID? = null,
    @SerialName("categoryId")
    @Serializable(with = KSerializerUUID::class)
    val categoryId: UUID? = null,
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("note")
    val note: String? = null,
    @SerialName("orderNum")
    val orderNum: Double = 0.0
)

