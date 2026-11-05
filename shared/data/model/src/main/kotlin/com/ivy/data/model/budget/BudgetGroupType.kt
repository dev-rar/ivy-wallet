package com.ivy.data.model.budget

enum class BudgetGroupType {
    INCOME,
    GIVING,
    SAVINGS,
    HOUSING,
    TRANSPORTATION,
    FOOD,
    LIFESTYLE,
    INSURANCE,
    DEBT,
    INVESTMENT,
    CUSTOM;

    val isIncome: Boolean
        get() = this == INCOME
}
