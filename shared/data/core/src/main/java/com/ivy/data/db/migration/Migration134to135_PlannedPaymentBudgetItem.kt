package com.ivy.data.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration134to135_PlannedPaymentBudgetItem : Migration(134, 135) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `planned_payment_rules` ADD COLUMN `budgetItemId` TEXT DEFAULT NULL"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_planned_payment_rules_budgetItemId` ON `planned_payment_rules` (`budgetItemId`)"
        )
    }
}

