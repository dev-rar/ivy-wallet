package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration132to133_InvestmentsAndNetWorth : Migration(132, 133) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `accounts` ADD COLUMN `accountType` TEXT NOT NULL DEFAULT 'BANK'"
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `investment_valuations` (
                `id` TEXT NOT NULL,
                `accountId` TEXT NOT NULL,
                `valuationDate` INTEGER NOT NULL,
                `valuationAmount` REAL NOT NULL,
                `notes` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_investment_valuations_accountId` ON `investment_valuations` (`accountId`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_investment_valuations_valuationDate` ON `investment_valuations` (`valuationDate`)"
        )
    }
}

