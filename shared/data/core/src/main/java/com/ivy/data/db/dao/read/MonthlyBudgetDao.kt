package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.BudgetGroupEntity
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.db.entity.MonthlyBudgetPlanEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MonthlyBudgetDao {
    @Query("SELECT * FROM monthly_budget_plans WHERE period = :period LIMIT 1")
    suspend fun findPlanByPeriod(period: String): MonthlyBudgetPlanEntity?

    @Query("SELECT * FROM monthly_budget_plans WHERE period = :period LIMIT 1")
    fun observePlanByPeriod(period: String): Flow<MonthlyBudgetPlanEntity?>

    @Query("SELECT * FROM monthly_budget_plans ORDER BY period DESC")
    suspend fun findAllPlans(): List<MonthlyBudgetPlanEntity>

    @Query("SELECT * FROM budget_groups WHERE planPeriod = :period ORDER BY orderNum ASC")
    suspend fun findGroupsByPeriod(period: String): List<BudgetGroupEntity>

    @Query("SELECT * FROM budget_groups WHERE planPeriod = :period ORDER BY orderNum ASC")
    fun observeGroupsByPeriod(period: String): Flow<List<BudgetGroupEntity>>

    @Query("SELECT * FROM budget_groups")
    suspend fun findAllGroups(): List<BudgetGroupEntity>

    @Query("SELECT * FROM budget_items WHERE planPeriod = :period ORDER BY orderNum ASC")
    suspend fun findItemsByPeriod(period: String): List<BudgetItemEntity>

    @Query("SELECT * FROM budget_items WHERE planPeriod = :period ORDER BY orderNum ASC")
    fun observeItemsByPeriod(period: String): Flow<List<BudgetItemEntity>>

    @Query("SELECT * FROM budget_items WHERE groupId = :groupId ORDER BY orderNum ASC")
    suspend fun findItemsByGroupId(groupId: UUID): List<BudgetItemEntity>

    @Query("SELECT * FROM budget_items WHERE id = :id LIMIT 1")
    suspend fun findItemById(id: UUID): BudgetItemEntity?

    @Query("SELECT * FROM budget_items")
    suspend fun findAllItems(): List<BudgetItemEntity>
}

