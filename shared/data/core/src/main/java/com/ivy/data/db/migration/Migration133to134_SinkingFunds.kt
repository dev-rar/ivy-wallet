package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration133to134_SinkingFunds : Migration(133, 134) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sinking_funds` (
                `id` TEXT NOT NULL,
                `budgetItemId` TEXT NOT NULL,
                `targetAmount` REAL NOT NULL,
                `targetDate` INTEGER,
                `openingBalance` REAL NOT NULL,
                `notes` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_sinking_funds_budgetItemId` ON `sinking_funds` (`budgetItemId`)"
        )
    }
}

