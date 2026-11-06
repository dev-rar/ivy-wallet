package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration131to132_TransactionSplits : Migration(131, 132) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `transaction_splits`")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `transaction_splits` (
                `id` TEXT NOT NULL,
                `transactionId` TEXT NOT NULL,
                `budgetItemId` TEXT,
                `categoryId` TEXT,
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

