package com.ivy.domain.usecase.budget

import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.repository.MonthlyBudgetRepository
import javax.inject.Inject

class CopyPreviousMonthPlanUseCase @Inject constructor(
    private val monthlyBudgetRepository: MonthlyBudgetRepository
) {
    suspend operator fun invoke(targetPeriod: BudgetPeriod): Boolean {
        return monthlyBudgetRepository.copyPreviousMonth(targetPeriod)
    }
}

