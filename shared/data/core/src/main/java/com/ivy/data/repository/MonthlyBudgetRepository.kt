package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.MonthlyBudgetDao
import com.ivy.data.db.dao.write.WriteMonthlyBudgetDao
import com.ivy.data.db.entity.BudgetGroupEntity
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.db.entity.MonthlyBudgetPlanEntity
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlyBudgetRepository @Inject constructor(
    private val monthlyBudgetDao: MonthlyBudgetDao,
    private val writeMonthlyBudgetDao: WriteMonthlyBudgetDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    fun observePlan(period: BudgetPeriod): Flow<MonthlyBudgetPlanEntity?> =
        monthlyBudgetDao.observePlanByPeriod(period.value)

    suspend fun findPlan(period: BudgetPeriod): MonthlyBudgetPlanEntity? =
        withContext(dispatchersProvider.io) {
            monthlyBudgetDao.findPlanByPeriod(period.value)
        }

    fun observeGroups(period: BudgetPeriod): Flow<List<BudgetGroupEntity>> =
        monthlyBudgetDao.observeGroupsByPeriod(period.value)

    suspend fun findGroups(period: BudgetPeriod): List<BudgetGroupEntity> =
        withContext(dispatchersProvider.io) {
            monthlyBudgetDao.findGroupsByPeriod(period.value)
        }

    fun observeItems(period: BudgetPeriod): Flow<List<BudgetItemEntity>> =
        monthlyBudgetDao.observeItemsByPeriod(period.value)

    suspend fun findItems(period: BudgetPeriod): List<BudgetItemEntity> =
        withContext(dispatchersProvider.io) {
            monthlyBudgetDao.findItemsByPeriod(period.value)
        }

    suspend fun getOrCreatePlan(period: BudgetPeriod): MonthlyBudgetPlanEntity =
        withContext(dispatchersProvider.io) {
            val existing = monthlyBudgetDao.findPlanByPeriod(period.value)
            if (existing != null) {
                return@withContext existing
            }

            // Create new plan
            val newPlan = MonthlyBudgetPlanEntity(
                id = UUID.randomUUID(),
                period = period.value,
                plannedIncome = 0.0,
                carryoverAmount = 0.0,
                isClosed = false,
                createdAt = System.currentTimeMillis()
            )
            writeMonthlyBudgetDao.savePlan(newPlan)

            // Seed default groups
            val defaultGroups = createDefaultGroups(period)
            writeMonthlyBudgetDao.saveGroups(defaultGroups)

            newPlan
        }

    suspend fun savePlan(plan: MonthlyBudgetPlanEntity) = withContext(dispatchersProvider.io) {
        writeMonthlyBudgetDao.savePlan(plan)
    }

    suspend fun updatePlannedIncome(period: BudgetPeriod, income: Double) =
        withContext(dispatchersProvider.io) {
            val plan = getOrCreatePlan(period)
            writeMonthlyBudgetDao.savePlan(plan.copy(plannedIncome = income))
        }

    suspend fun updateCarryover(period: BudgetPeriod, carryover: Double) =
        withContext(dispatchersProvider.io) {
            val plan = getOrCreatePlan(period)
            writeMonthlyBudgetDao.savePlan(plan.copy(carryoverAmount = carryover))
        }

    suspend fun saveGroup(group: BudgetGroupEntity) = withContext(dispatchersProvider.io) {
        writeMonthlyBudgetDao.saveGroup(group)
    }

    suspend fun deleteGroup(groupId: UUID) = withContext(dispatchersProvider.io) {
        writeMonthlyBudgetDao.deleteItemsByGroupId(groupId)
        writeMonthlyBudgetDao.deleteGroupById(groupId)
    }

    suspend fun saveItem(item: BudgetItemEntity) = withContext(dispatchersProvider.io) {
        writeMonthlyBudgetDao.saveItem(item)
    }

    suspend fun deleteItem(itemId: UUID) = withContext(dispatchersProvider.io) {
        writeMonthlyBudgetDao.deleteItemById(itemId)
    }

    suspend fun copyPreviousMonth(targetPeriod: BudgetPeriod): Boolean =
        withContext(dispatchersProvider.io) {
            val prevPeriod = targetPeriod.previous()
            val prevPlan = monthlyBudgetDao.findPlanByPeriod(prevPeriod.value) ?: return@withContext false
            val prevGroups = monthlyBudgetDao.findGroupsByPeriod(prevPeriod.value)
            if (prevGroups.isEmpty()) return@withContext false
            val prevItems = monthlyBudgetDao.findItemsByPeriod(prevPeriod.value)

            // Ensure target plan exists, copy planned income and carryover
            val targetPlan = getOrCreatePlan(targetPeriod).copy(
                plannedIncome = prevPlan.plannedIncome,
                carryoverAmount = prevPlan.carryoverAmount
            )
            writeMonthlyBudgetDao.savePlan(targetPlan)

            // Clear any blank target groups/items before copying
            writeMonthlyBudgetDao.deleteItemsByPeriod(targetPeriod.value)
            writeMonthlyBudgetDao.deleteGroupsByPeriod(targetPeriod.value)

            val groupIdMap = mutableMapOf<UUID, UUID>()
            val newGroups = prevGroups.map { oldGroup ->
                val newGroupId = UUID.randomUUID()
                groupIdMap[oldGroup.id] = newGroupId
                oldGroup.copy(
                    id = newGroupId,
                    planPeriod = targetPeriod.value
                )
            }
            writeMonthlyBudgetDao.saveGroups(newGroups)

            val newItems = prevItems.mapNotNull { oldItem ->
                val newGroupId = groupIdMap[oldItem.groupId] ?: return@mapNotNull null
                oldItem.copy(
                    id = UUID.randomUUID(),
                    groupId = newGroupId,
                    planPeriod = targetPeriod.value
                )
            }
            writeMonthlyBudgetDao.saveItems(newItems)

            true
        }

    private fun createDefaultGroups(period: BudgetPeriod): List<BudgetGroupEntity> {
        val defs = listOf(
            BudgetGroupType.INCOME to "Income",
            BudgetGroupType.GIVING to "Giving",
            BudgetGroupType.SAVINGS to "Savings",
            BudgetGroupType.HOUSING to "Housing",
            BudgetGroupType.TRANSPORTATION to "Transportation",
            BudgetGroupType.FOOD to "Food",
            BudgetGroupType.LIFESTYLE to "Lifestyle",
            BudgetGroupType.INSURANCE to "Insurance",
            BudgetGroupType.DEBT to "Debt",
            BudgetGroupType.INVESTMENT to "Investment",
        )
        return defs.mapIndexed { index, (type, name) ->
            BudgetGroupEntity(
                id = UUID.randomUUID(),
                planPeriod = period.value,
                name = name,
                groupType = type.name,
                orderNum = (index + 1).toDouble()
            )
        }
    }
}
