package com.ivy.domain.usecase.budget

import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.repository.MonthlyBudgetRepository
import java.util.UUID
import javax.inject.Inject

class SaveBudgetItemUseCase @Inject constructor(
    private val monthlyBudgetRepository: MonthlyBudgetRepository
) {
    suspend fun save(item: BudgetItemEntity) {
        monthlyBudgetRepository.saveItem(item)
    }

    suspend fun delete(itemId: UUID) {
        monthlyBudgetRepository.deleteItem(itemId)
    }
}

