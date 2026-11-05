package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration131to132_TransactionSplits : Migration(131, 132) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transaction_splits` (
                `id` BLOB NOT NULL,
                `transactionId` BLOB NOT NULL,
                `budgetItemId` BLOB,
                `categoryId` BLOB,
                `amount` REAL NOT NULL,
                `note` TEXT,
                `orderNum` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transaction_splits_transactionId` ON `transaction_splits` (`transactionId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transaction_splits_budgetItemId` ON `transaction_splits` (`budgetItemId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transaction_splits_categoryId` ON `transaction_splits` (`categoryId`)"
        )
    }
}

