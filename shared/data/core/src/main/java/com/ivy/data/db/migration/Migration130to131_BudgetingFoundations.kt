package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration130to131_BudgetingFoundations : Migration(130, 131) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `monthly_budget_plans` (
                `id` TEXT NOT NULL,
                `period` TEXT NOT NULL,
                `plannedIncome` REAL NOT NULL,
                `carryoverAmount` REAL NOT NULL,
                `isClosed` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_monthly_budget_plans_period` ON `monthly_budget_plans` (`period`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `budget_groups` (
                `id` TEXT NOT NULL,
                `planPeriod` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `groupType` TEXT NOT NULL,
                `orderNum` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_groups_planPeriod` ON `budget_groups` (`planPeriod`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_groups_planPeriod_name` ON `budget_groups` (`planPeriod`, `name`)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `budget_items` (
                `id` TEXT NOT NULL,
                `groupId` TEXT NOT NULL,
                `planPeriod` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `plannedAmount` REAL NOT NULL,
                `itemType` TEXT NOT NULL,
                `categoryId` TEXT,
                `orderNum` REAL NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_items_groupId` ON `budget_items` (`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_items_planPeriod` ON `budget_items` (`planPeriod`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_budget_items_categoryId` ON `budget_items` (`categoryId`)")
    }
}

