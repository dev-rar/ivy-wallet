package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.MonthlyBudgetDao
import com.ivy.data.db.dao.write.WriteMonthlyBudgetDao
import com.ivy.data.db.entity.BudgetGroupEntity
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.db.entity.MonthlyBudgetPlanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.annotations.VisibleForTesting
import java.util.UUID

@VisibleForTesting
class FakeMonthlyBudgetDao : MonthlyBudgetDao, WriteMonthlyBudgetDao {
    private val plans = mutableListOf<MonthlyBudgetPlanEntity>()
    private val groups = mutableListOf<BudgetGroupEntity>()
    private val items = mutableListOf<BudgetItemEntity>()

    override suspend fun findPlanByPeriod(period: String): MonthlyBudgetPlanEntity? {
        return plans.find { it.period == period }
    }

    override fun observePlanByPeriod(period: String): Flow<MonthlyBudgetPlanEntity?> {
        return flowOf(plans.find { it.period == period })
    }

    override suspend fun findAllPlans(): List<MonthlyBudgetPlanEntity> {
        return plans.sortedByDescending { it.period }
    }

    override suspend fun findGroupsByPeriod(period: String): List<BudgetGroupEntity> {
        return groups.filter { it.planPeriod == period }.sortedBy { it.orderNum }
    }

    override fun observeGroupsByPeriod(period: String): Flow<List<BudgetGroupEntity>> {
        return flowOf(groups.filter { it.planPeriod == period }.sortedBy { it.orderNum })
    }

    override suspend fun findAllGroups(): List<BudgetGroupEntity> {
        return groups
    }

    override suspend fun findItemsByPeriod(period: String): List<BudgetItemEntity> {
        return items.filter { it.planPeriod == period }.sortedBy { it.orderNum }
    }

    override fun observeItemsByPeriod(period: String): Flow<List<BudgetItemEntity>> {
        return flowOf(items.filter { it.planPeriod == period }.sortedBy { it.orderNum })
    }

    override suspend fun findItemsByGroupId(groupId: UUID): List<BudgetItemEntity> {
        return items.filter { it.groupId == groupId }.sortedBy { it.orderNum }
    }

    override suspend fun findItemById(id: UUID): BudgetItemEntity? {
        return items.find { it.id == id }
    }

    override suspend fun findAllItems(): List<BudgetItemEntity> {
        return items
    }

    override suspend fun savePlan(plan: MonthlyBudgetPlanEntity) {
        plans.removeIf { it.period == plan.period }
        plans.add(plan)
    }

    override suspend fun savePlans(plans: List<MonthlyBudgetPlanEntity>) {
        plans.forEach { savePlan(it) }
    }

    override suspend fun saveGroup(group: BudgetGroupEntity) {
        groups.removeIf { it.id == group.id }
        groups.add(group)
    }

    override suspend fun saveGroups(groups: List<BudgetGroupEntity>) {
        groups.forEach { saveGroup(it) }
    }

    override suspend fun saveItem(item: BudgetItemEntity) {
        items.removeIf { it.id == item.id }
        items.add(item)
    }

    override suspend fun saveItems(items: List<BudgetItemEntity>) {
        items.forEach { saveItem(it) }
    }

    override suspend fun deletePlanByPeriod(period: String) {
        plans.removeIf { it.period == period }
    }

    override suspend fun deleteGroupById(id: UUID) {
        groups.removeIf { it.id == id }
    }

    override suspend fun deleteGroupsByPeriod(period: String) {
        groups.removeIf { it.planPeriod == period }
    }

    override suspend fun deleteItemById(id: UUID) {
        items.removeIf { it.id == id }
    }

    override suspend fun deleteItemsByGroupId(groupId: UUID) {
        items.removeIf { it.groupId == groupId }
    }

    override suspend fun deleteItemsByPeriod(period: String) {
        items.removeIf { it.planPeriod == period }
    }

    override suspend fun deleteAllPlans() {
        plans.clear()
    }

    override suspend fun deleteAllGroups() {
        groups.clear()
    }

    override suspend fun deleteAllItems() {
        items.clear()
    }
}
