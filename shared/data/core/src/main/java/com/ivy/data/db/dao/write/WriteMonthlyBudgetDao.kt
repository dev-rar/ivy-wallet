package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.BudgetGroupEntity
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.db.entity.MonthlyBudgetPlanEntity
import java.util.UUID

@Dao
interface WriteMonthlyBudgetDao {
    @Upsert
    suspend fun savePlan(plan: MonthlyBudgetPlanEntity)

    @Upsert
    suspend fun savePlans(plans: List<MonthlyBudgetPlanEntity>)

    @Upsert
    suspend fun saveGroup(group: BudgetGroupEntity)

    @Upsert
    suspend fun saveGroups(groups: List<BudgetGroupEntity>)

    @Upsert
    suspend fun saveItem(item: BudgetItemEntity)

    @Upsert
    suspend fun saveItems(items: List<BudgetItemEntity>)

    @Query("DELETE FROM monthly_budget_plans WHERE period = :period")
    suspend fun deletePlanByPeriod(period: String)

    @Query("DELETE FROM budget_groups WHERE id = :id")
    suspend fun deleteGroupById(id: UUID)

    @Query("DELETE FROM budget_groups WHERE planPeriod = :period")
    suspend fun deleteGroupsByPeriod(period: String)

    @Query("DELETE FROM budget_items WHERE id = :id")
    suspend fun deleteItemById(id: UUID)

    @Query("DELETE FROM budget_items WHERE groupId = :groupId")
    suspend fun deleteItemsByGroupId(groupId: UUID)

    @Query("DELETE FROM budget_items WHERE planPeriod = :period")
    suspend fun deleteItemsByPeriod(period: String)

    @Query("DELETE FROM monthly_budget_plans")
    suspend fun deleteAllPlans()

    @Query("DELETE FROM budget_groups")
    suspend fun deleteAllGroups()

    @Query("DELETE FROM budget_items")
    suspend fun deleteAllItems()
}

