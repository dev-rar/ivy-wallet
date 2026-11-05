# Budgeting & Investment Extension Plan

## Overview
This document specifies the architecture, data models, financial rules, phased roadmap, and verification strategy for extending Ivy Wallet with EveryDollar-style zero-based monthly personal finance capabilities and account-level investment tracking.

---

## 1. Scope & Exclusions

### 1.1 In-Scope Capabilities
- **Income & Expense Tracking**: Categorization, splitting, refunds, and adjustments using Ivy Wallet's existing transaction model.
- **Cash & Bank Accounts**: Liquidity management, multi-currency support (with explicit base currency conversions), and account-level balances.
- **Monthly Zero-Based Budgeting (EveryDollar-style)**:
  - Monthly periods with planned income lines.
  - Budget groups and budget items.
  - Planned allocations: `Left to Assign = Planned Income + Explicit Carryover - Planned Allocations`.
  - Copying previous month structure and planned amounts without duplicating actual financial transactions.
  - Planned vs. Actual totals and remaining allocation tracking.
- **Transaction Assignments & Splits**: Connecting transactions to budget items; splitting a transaction across multiple budget items with exact sum validation; handling refunds and reversals.
- **Savings & Sinking Funds**: Multi-month capital reservations with opening balances, monthly allocations, accumulated balances, and spending tracking without inflating recorded assets or double-counting backing cash.
- **Bills, Recurring Payments & Reminders**: Extension of the recurrence engine for expected amounts, due dates, paid/skipped occurrences, and payment linking.
- **Paycheck & Cash-Flow Planning**: Forecasting cash adequacy across scheduled events within a monthly period against liquid cash buffers.
- **Debt Balances & Repayments**: Tracking liability balances, principal payoffs, and interest allocations building on Ivy's loan infrastructure.
- **Account-Level Investment Tracking**:
  - Support for asset classes such as PSX brokerage accounts, mutual funds, and family business investments.
  - Tracking contributions and withdrawals via inter-account transfers (neither consumption spending nor earned income).
  - Recording manual dated valuations and maintaining valuation history.
  - Recording received investment distributions (cash dividends/yield) vs return of capital.
  - Calculating account-level performance: $\text{Value Change} = \text{Ending Value} - \text{Beginning Value} - \text{Contributions} + \text{Withdrawals}$.
  - Brokerage cash vs investment valuation distinction (avoiding double counting).
  - Business loan principal vs equity value distinction.
- **Net Worth**: Total recorded assets minus outstanding liabilities at specified valuation dates.
- **Reports, Imports, Exports & Backups**: Planned vs actual reports, net worth over time, multi-category export, CSV mapping, and zero-data-loss JSON/ZIP backup/restore.

### 1.2 Explicitly Deferred Capabilities
- Individual stock holdings, tickers, and share quantities.
- Stock purchase lots, order routing, and capital gains tax lots.
- Dividend declarations and automated entitlement calculations.
- Corporate action calendars and dividend payout schedules.
- Live stock market tickers and mutual fund NAV web feeds.
- Stock watchlists, technical charts, and financial news/research.
- Speculative 5-, 10-, 15-, or 20-year investment return projections.
- Long-term financial independence / retire-early (FIRE) roadmap calculators.
- External human coaching or subscription educational content.
- Web or iOS clients and proprietary cloud synchronization servers.

---

## 2. Preservation Requirements

1. **Extend, Do Not Rebuild**: Build strictly on Ivy Wallet's Kotlin / Jetpack Compose architecture and Room database.
2. **Visual & Design System Preservation**: Reuse `:ivy-design` (`UI.colors`, `UI.typo`, `UI.shapes`), Material3 components, navigation layouts, typography, spacing, and accessibility patterns.
3. **No Screen Replacements**: Keep the existing Home (`HomeTab`), Accounts (`AccountsTab`), and navigation flows. Add new features via natural entry points (e.g., within `HomeMoreMenu`, screen headers, or modal drawers).
4. **Preserve Existing Semantics**:
   - Accounts, balances, transactions, transfers, categories, and tags must continue functioning without mandatory budget assignments.
   - Existing timeless/filter-based budgets must remain operable if the user chooses not to use zero-based monthly planning.
5. **Protect User Data & Migrations**:
   - Strictly versioned, additive Room migrations (`Migration130to131_...`).
   - No destructive database drops or table resets.
   - Preserve all foreign keys, UUIDs, timestamps, and relational integrity.
   - Maintain JSON/ZIP backup and restore compatibility (`IvyWalletCompleteData`).
6. **Offline Operation**: No mandatory network dependencies; exchange rates and investment valuations must support 100% offline manual operation.

---

## 3. Repository Findings & Baseline Architecture

### 3.1 Repository Structure & Tech Stack
- **Languages**: Kotlin 2.0.20, Jetpack Compose (Compiler 2.0.20, Compose 1.6.8, Material3 1.2.1).
- **Architecture**: Modular Android app using MVI / UDF with `ComposeViewModel<State, Event>`, Flow/Coroutines, Arrow-kt (`Either`, `Option`), and Hilt DI.
- **Persistence**: Room DB v2.6.1 (`ivywallet.db`), schema version **130**, DataStore for preferences.
- **Key Modules**:
  - `:app`: Application entry point, Hilt setup, build variants.
  - `:feature:home`: Home tab, header, customer journey, and `HomeMoreMenu`.
  - `:feature:accounts`: Accounts tab, balances, account details.
  - `:feature:budgets`: Current budget screen and viewmodel.
  - `:feature:categories`: Category listing and creation.
  - `:feature:edit-transaction`: Transaction creation and edit screen.
  - `:feature:loans`: Loan and loan record tracking (BORROW/LEND).
  - `:feature:planned-payments`: Recurrence rules and scheduled items.
  - `:feature:reports`: Reporting, filtering, and chart overviews.
  - `:feature:import-data`: CSV import pipeline (`CSVImporterV2`).
  - `:shared:data:core`: Room database (`IvyRoomDatabase`), DAOs, entities, migrations, and `BackupDataUseCase`.
  - `:shared:data:model`: Domain models (`Account`, `Transaction`, `Value`, `AssetCode`).
  - `:shared:domain`: Domain use cases (`AccountBalanceUseCase`, `ExportCsvUseCase`).
  - `:shared:ui:navigation`: Compose navigation stack (`Screen`, `Screens`, `NavigationRoot`).

### 3.2 Baseline Build Environment Assessment
- **Host OS**: Windows 11 / x64.
- **Git Remote & SSH**: Verified and configured. Remote set to `git@github-personal:dev-rar/ivy-wallet.git` utilizing personal SSH key `~/.ssh/id_ed25519_personal` with successful GitHub authentication for `dev-rar`.
- **JDK Status**: `java` and `javac` are currently not available in the execution environment PATH. Running `.\gradlew.bat --version` returns:
  `ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.`
- **Android SDK**: Android command line tools / SDK 35/36 not detected in default environment paths.
- **Baseline Build Status**: Documented as **BLOCKED on local environment prerequisites (JDK 17+ and Android SDK)**. The repository source code and build scripts are well-formed and ready for development.

### 3.3 Android Application Identity, Signing & Personal Fork Path
- **Namespace & App ID**: `com.ivy.wallet` (Debug suffix: `.debug` -> `com.ivy.wallet.debug`).
- **Signing**:
  - Debug builds use `debug.jks` located at repository root with known credentials (`storePassword = "IVY7834!DEbug"`, `keyAlias = "debug"`).
  - Release builds reference `sign.jks` via environment variables.
- **Safe Installation Path**:
  - Installing personal fork builds with `.debug` suffix allows side-by-side installation alongside any existing production Ivy Wallet app on an Android device.
  - Data transfer between existing app and the personal fork will be executed using Ivy Wallet's built-in **Backup & Restore** (`.zip` / `.json` export/import), preventing data loss or package signature conflicts.

---

## 4. Feature Comparison Matrix

| Capability | Existing Ivy Wallet Implementation | Gap Identified | Proposed Extension | Existing Behavior Affected |
| :--- | :--- | :--- | :--- | :--- |
| **Accounts** | `AccountEntity` in `shared/data/core`, `Account` model in `shared/data/model`. Generic accounts with currency, color, icon, and `includeInBalance`. | No account classification (Cash, Bank, Investment, Liability). No investment-specific metadata (institution, opening value, stale valuation threshold). | Add optional `AccountType` (`CASH`, `BANK`, `INVESTMENT`, `LIABILITY`) and investment metadata to account definition with backwards-compatible defaults (`AccountType.REGULAR`). | None. Existing accounts default to `REGULAR` (treated as standard liquid account). |
| **Monetary Arithmetic** | Double in Room entities; `PositiveValue`, `Value` (wrapping `Double`) in domain models; `BigDecimal` in parts of `EditTransactionViewModel` and `HomeViewModel`. | Floating point representation in entities can introduce rounding imprecision in zero-based split allocations. | Implement exact cent/minor-unit rounding rules or `BigDecimal` decimal helpers for budget allocation arithmetic (`ValueRounding`). | None. Storage remains compatible `Double`, arithmetic normalized. |
| **Transactions & Splits** | `TransactionEntity` (`INCOME`, `EXPENSE`, `TRANSFER`) with 1-to-1 account, category, and amount. | No split transactions (one transaction cannot be allocated to multiple categories or budget items). | Introduce `TransactionSplitEntity` allowing 1-to-N allocations while keeping parent transaction amount as the authoritative single balance impact. | None. Single transactions continue working normally; splits only active when configured. |
| **Transfers & Investments** | `TransactionType.TRANSFER` moves money between accounts without altering net income/expense (unless user setting `transfersAsIncomeExpense` is on). | Transfers to investment accounts are currently not distinguishable from bank-to-bank transfers, making it hard to fulfill an "Investment" budget allocation without falsely registering as spending. | Allow a transfer to an investment account to be tagged with a budget item allocation for zero-based budgeting, while preserving its transfer identity in financial P&L reports. | None. Existing transfers remain non-spending transfers. |
| **Budgets** | `BudgetEntity` (`name`, `amount`, `categoryIdsSerialized`, `accountIdsSerialized`). Static monthly limit per category. Filter-based spent calculation against current month. | No monthly budget instances (cannot plan for future/past months). No planned income. No zero-based "left to assign" balancing. No budget groups. | Introduce `MonthlyBudgetPlanEntity`, `BudgetGroupEntity`, and `BudgetItemEntity` keyed by month/year period (`YYYY-MM`). Preserve legacy `BudgetEntity` for users using classic mode. | Classic budgets continue to work in parallel. Monthly mode accessible as dedicated budgeting workflow. |
| **Sinking Funds & Savings Goals** | Single `bufferAmount` in `SettingsEntity` displayed as a "Savings goal" buffer in `HomeMoreMenu`. | No multi-fund tracking, no multi-month accumulation, no fund-specific spending vs reservation tracking. | Introduce `SinkingFundEntity` linked to budget items, tracking accumulated balance across months (`Opening + Allocation - Spending = Closing`). | Home buffer remains functional; sinking funds add multi-goal capability. |
| **Debt / Loans** | `LoanEntity` and `LoanRecordEntity` in `:feature:loans`. Supports BORROW/LEND, payments, and interest. | Loans are isolated from the monthly budget plan and net worth summary. Repayments are not auto-assigned to debt budget lines. | Connect loan payments to debt budget items in the monthly plan. Include loan liabilities in the Net Worth calculation. | None. Existing loan screens and data structures remain intact. |
| **Investment Valuations** | Not supported. | No tracking of equity, mutual fund NAVs, business investments, valuation dates, or historical performance. | Introduce `InvestmentValuationEntity` recording dated manual valuations and notes per investment account. | None. Standalone new entity. |
| **Net Worth** | Not supported. | Only total liquid account balance is calculated in `AccountsViewModel`. | Introduce `NetWorthUseCase` aggregating liquid accounts, latest investment valuations, and subtracting outstanding loan/debt liabilities. | None. Total account balance display preserved; Net Worth added as dedicated metric/view. |
| **Recurrence & Bills** | `PlannedPaymentRuleEntity` in `:feature:planned-payments`. Daily, weekly, monthly, yearly rules. | No direct linking between planned bills and monthly budget line allocations or paycheck planning timeline. | Add optional `budgetItemId` link to `PlannedPaymentRuleEntity` and projected cash-flow timeline. | None. Existing planned payments continue operating seamlessly. |
| **Paycheck Planning** | Not supported. | Income is assumed generic; no visual timeline matching scheduled paychecks to due bills before the month ends. | Create a Cash-Flow / Paycheck Planning view projecting cash balance after each scheduled income and bill event. | None. Optional planning tool. |
| **Month Close & Carryover**| Not supported. | Months roll over purely by date calendar without review, reconciliation, or carryover decisions. | Provide Month Close flow: review unassigned transactions, resolve overspending, carry over fund balances, and roll forward unallocated funds. | None. Opt-in month close workflow. |
| **Reports** | `:feature:reports` and `:feature:piechart`. Displays Income vs Expense totals, category breakdowns, and transaction lists. | No Planned vs Actual budget comparison report. No Net Worth history report. No Investment performance tracking. | Add reports for: (1) Planned vs Actual by budget group/item, (2) Net Worth composition, (3) Investment value changes. | Existing reports remain unchanged. |
| **Imports & Exports** | CSV import for various apps (`feature:import-data`), CSV export of transactions (`ExportCsvUseCase`), and JSON/ZIP full backup (`BackupDataUseCase`). | CSV import cannot map to budget items or investment valuations. Backup does not include new entities. | Extend `IvyWalletCompleteData` with new tables with default empty lists. Enhance CSV export/import with budget and investment fields. | Full backwards and forwards compatibility of backups preserved. |

---

## 5. Financial Model & Calculation Rules

### 5.1 Distinct Financial Concepts
1. **Account**: A container of financial value (`CASH`, `BANK`, `INVESTMENT`, `LIABILITY`).
2. **Transaction**: Recorded historical money movement (`INCOME`, `EXPENSE`, `TRANSFER`). Source of actual activity.
3. **Budget Allocation**: Intended spending or saving plan for a specific calendar month (`YYYY-MM`).
4. **Fund (Sinking Fund)**: A cross-period reservation of money for an irregular or future expense.
5. **Schedule (Planned Payment)**: An expected future event, completely separate from actual transactions until recorded.
6. **Valuation**: A dated point-in-time assessment of an asset or liability.

### 5.2 Mathematical & Calculation Rules
- **Zero-Based Budget Balance**:
  $$\text{Left to Assign} = \text{Planned Income} + \text{Explicit Carryover} - \sum \text{Planned Allocations}$$
  - A zero-based budget is balanced when $\text{Left to Assign} = 0.00$.
  - Planned income and planned allocations are planning figures; assigning money does **not** generate accounting transactions.
- **Transfers & Net Income**:
  - An internal transfer between two owned accounts ($A \to B$) changes account balances but has zero effect on total liquid wealth and is **never** counted as income or expense in P&L reports.
- **Investment Contributions & Withdrawals**:
  - A transfer from a Bank account to an Investment account fulfills an "Investment Contribution" budget allocation, but in financial reports remains an equity transfer.
  - An investment withdrawal transfers money back to cash/bank without creating earned income.
- **Investment Performance (Account-Level Value Change)**:
  $$\Delta V = V_{\text{end}} - V_{\text{begin}} - C + W$$
  Where:
  - $V_{\text{end}}$ = Ending valuation for period.
  - $V_{\text{begin}}$ = Beginning valuation for period.
  - $C$ = Total contributions during period.
  - $W$ = Total withdrawals during period.
  - Clearly labeled as **Value Change (Net Growth)**, never as an annualized IRR/CAGR if data is incomplete.
- **Brokerage Cash Rule**:
  - If an investment account includes brokerage cash in its manual valuation, that brokerage cash must **not** be tracked as a separate cash account in Ivy Wallet.
- **Family Business Investment Rule**:
  - Loans to a business are tracked as Assets/Debts under Loans (`BORROW`/`LEND`), whereas equity ownership is tracked as an Investment Account. Loan repayments reduce loan principal, not equity value.
- **Sinking Fund Balance**:
  $$\text{Closing Fund Balance} = \text{Opening Balance} + \sum \text{Allocations} + \sum \text{Adjustments} - \sum \text{Spending}$$
  - Sinking funds do not create money; the total of all sinking funds must be compared against actual liquid bank/cash balances to warn of over-allocation.
- **Current Net Worth**:
  $$\text{Net Worth} = \sum \text{Liquid Accounts} + \sum \text{Latest Investment Valuations} - \sum \text{Outstanding Liabilities}$$
  - Assets and liabilities are included exactly once.
  - Currency conversion to Base Currency uses the latest available exchange rates from `ExchangeRateEntity`.

---

## 6. Phase Checklist & Roadmap

- [x] **Phase 0 — Audit and Repository-Specific Plan** `Complete`
- [x] **Phase 1 — Compatibility and Data Foundations** `Complete`
- [ ] **Phase 2 — Monthly Zero-Based Budgeting** `Not started`
- [ ] **Phase 3 — Transaction Assignments and Splits** `Not started`
- [ ] **Phase 4 — Investment Accounts, Debts, and Net Worth** `Not started`
- [ ] **Phase 5 — Sinking Funds and Savings Goals** `Not started`
- [ ] **Phase 6 — Bills, Recurrence, and Reminders** `Not started`
- [ ] **Phase 7 — Paycheck and Cash-Flow Planning** `Not started`
- [ ] **Phase 8 — Month Close and Carryover** `Not started`
- [ ] **Phase 9 — Reports and Exports** `Not started`
- [ ] **Phase 10 — Statement Imports and Matching** `Not started`
- [ ] **Phase 11 — Complete Daily-Use Verification** `Not started`

---

## 7. Detailed Phase Specifications

### Phase 1 — Compatibility and Data Foundations
- **Objective**: Establish baseline test fixtures, non-destructive migration conventions, and foundational data structures.
- **Key Tasks**:
  - Define additive Room database migration `Migration130to131`.
  - Extend `IvyWalletCompleteData` with null-safe default collections for all upcoming extensions.
  - Create legacy-data fixtures representing typical existing Ivy Wallet databases (accounts, categories, transactions, loans).
  - Verify baseline backup/restore integrity against existing fixtures.
  - Establish domain types and value wrappers for budget periods (`YearMonth`) and account classifications.
- **Relevant Files**:
  - `shared/data/core/src/main/java/com/ivy/data/db/IvyRoomDatabase.kt`
  - `shared/data/core/src/main/java/com/ivy/data/backup/IvyWalletCompleteData.kt`
  - `shared/data/core/src/main/java/com/ivy/data/backup/BackupDataUseCase.kt`
  - `shared/data/core/src/androidTest/java/com/ivy/data/db/IvyRoomDatabaseMigrationTest.kt`

### Phase 2 — Monthly Zero-Based Budgeting
- **Objective**: Deliver a full monthly budgeting screen following EveryDollar's zero-based paradigm.
- **Key Tasks**:
  - Implement entities: `MonthlyBudgetPlanEntity`, `BudgetGroupEntity`, `BudgetItemEntity`.
  - DAOs and Repositories for monthly plans, groups, and items.
  - Monthly navigation: swipe or picker to select month/year (`YYYY-MM`).
  - Budget groups (Income, Housing, Food, Transportation, Giving, Lifestyle, Savings, Debt, Investments).
  - Planned income lines and planned allocation inputs.
  - Zero-based header: `Left to Assign = Planned Income + Carryover - Planned Allocations`.
  - "Copy Previous Month" feature (copies groups, items, and planned amounts, zeroing actuals).
  - UI entry point in `BudgetScreen` or `HomeMoreMenu` allowing toggle between Classic Budget and Monthly Plan.
- **Relevant Files**:
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/MonthlyBudgetPlanEntity.kt`
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/BudgetGroupEntity.kt`
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/BudgetItemEntity.kt`
  - `feature/budgets/src/main/java/com/ivy/budgets/monthly/...`
  - `feature/budgets/src/main/java/com/ivy/budgets/BudgetScreen.kt`

### Phase 3 — Transaction Assignments and Splits
- **Objective**: Connect actual transaction activity to monthly budget items and support multi-category splits.
- **Key Tasks**:
  - Entity `TransactionSplitEntity` (`transactionId`, `budgetItemId`, `categoryId`, `amount`, `note`).
  - Add optional `budgetItemId` to `TransactionEntity` (or manage via split table).
  - Update `EditTransactionScreen` with optional "Budget Item" picker and "Split Transaction" modal.
  - Split validation: sum of split parts must exactly equal parent transaction amount.
  - Unassigned transaction review screen/sheet.
  - Reversals, refunds, and date edits recalculate affected budget periods.
- **Relevant Files**:
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/TransactionSplitEntity.kt`
  - `feature/edit-transaction/src/main/java/com/ivy/transaction/EditTransactionViewModel.kt`
  - `feature/edit-transaction/src/main/java/com/ivy/transaction/EditTransactionScreen.kt`
  - `shared/domain/src/main/java/com/ivy/domain/usecase/transaction/...`

### Phase 4 — Investment Accounts, Debts, and Net Worth
- **Objective**: Support account-level investment tracking (PSX brokerage, mutual funds, family business) and unified Net Worth.
- **Key Tasks**:
  - Extend `AccountEntity` with `accountType` (`CASH`, `BANK`, `INVESTMENT`, `LIABILITY`), institution name, and stale threshold.
  - Entity `InvestmentValuationEntity` (`accountId`, `valuationDate`, `value`, `notes`).
  - Record manual valuations with date and note.
  - Calculate account-level performance: $\Delta V = V_{\text{end}} - V_{\text{begin}} - C + W$.
  - Distinguish cash yield/dividends from capital returns.
  - Unified Net Worth calculation (`Assets - Liabilities`).
  - Net Worth card in Accounts Tab or Home Screen.
- **Relevant Files**:
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/AccountEntity.kt`
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/InvestmentValuationEntity.kt`
  - `feature/accounts/src/main/java/com/ivy/accounts/AccountsViewModel.kt`
  - `feature/accounts/src/main/java/com/ivy/accounts/AccountsTab.kt`
  - `shared/domain/src/main/java/com/ivy/domain/usecase/networth/NetWorthUseCase.kt`

### Phase 5 — Sinking Funds and Savings Goals
- **Objective**: Enable cross-month multi-fund savings reservations.
- **Key Tasks**:
  - Entity `SinkingFundEntity` (`budgetItemId`, `targetAmount`, `targetDate`, `openingBalance`).
  - Monthly carryover logic: Fund Balance rolls over month-to-month.
  - Spending against a fund reduces the fund balance.
  - Visual progress bar and goal tracking.
  - Solvency check: ensure total fund balances $\le$ total liquid cash in bank accounts.
- **Relevant Files**:
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/SinkingFundEntity.kt`
  - `feature/budgets/src/main/java/com/ivy/budgets/funds/...`
  - `shared/domain/src/main/java/com/ivy/domain/usecase/funds/SinkingFundUseCase.kt`

### Phase 6 — Bills, Recurrence, and Reminders
- **Objective**: Track upcoming bills and connect them to monthly budget items.
- **Key Tasks**:
  - Add optional `budgetItemId` to `PlannedPaymentRuleEntity`.
  - Occurrence status tracking (Scheduled, Overdue, Paid, Skipped).
  - Single-occurrence edits vs series edits.
  - Notification scheduling with Android 13+ permission and timezone handling.
- **Relevant Files**:
  - `shared/data/core/src/main/java/com/ivy/data/db/entity/PlannedPaymentRuleEntity.kt`
  - `feature/planned-payments/src/main/java/com/ivy/planned/...`

### Phase 7 — Paycheck and Cash-Flow Planning
- **Objective**: Match scheduled income events to scheduled expenses during the month to avoid cash shortfalls.
- **Key Tasks**:
  - Cash-flow timeline ordering scheduled income, bills, and flexible spending.
  - Projected daily cash balance curve: Starting cash + Scheduled Inflows - Scheduled Outflows.
  - Shortfall detection and warnings when projected balance drops below buffer.
- **Relevant Files**:
  - `feature/cashflow/...`
  - `shared/domain/src/main/java/com/ivy/domain/usecase/cashflow/CashFlowProjectionUseCase.kt`

### Phase 8 — Month Close and Carryover
- **Objective**: Formalize the month transition workflow.
- **Key Tasks**:
  - Month-close wizard: review unassigned transactions, resolve overspent budget items, inspect remaining planned balances.
  - Carryover options: roll unspent money into general carryover or assign to specific sinking funds.
  - Idempotent close/reopen state tracking (`MonthlyBudgetPlanEntity.isClosed`).
- **Relevant Files**:
  - `feature/budgets/src/main/java/com/ivy/budgets/monthclose/...`

### Phase 9 — Reports and Exports
- **Objective**: Comprehensive reporting and data export.
- **Key Tasks**:
  - Planned vs Actual report by category and group.
  - Historical Net Worth trend.
  - Investment valuation and contribution history.
  - Enhanced CSV export including budget items, split rows, and investment valuations.
- **Relevant Files**:
  - `feature/reports/src/main/java/com/ivy/reports/...`
  - `shared/domain/src/main/java/com/ivy/domain/usecase/csv/ExportCsvUseCase.kt`

### Phase 10 — Statement Imports and Matching
- **Objective**: Import bank/card statements with duplicate detection and budget matching.
- **Key Tasks**:
  - Generic configurable CSV column mapper (Date, Amount, Description, Category/Type).
  - Support common Pakistani banking formats (e.g., standard statement exports).
  - Duplicate detection based on timestamp, account, and amount.
  - Smart budget item suggestion based on payee/title history.
- **Relevant Files**:
  - `feature/import-data/src/main/java/com/ivy/importdata/csv/...`

### Phase 11 — Complete Daily-Use Verification
- **Objective**: End-to-end verification of all workflows in real-world scenarios.
- **Key Tasks**:
  - Complete 12-step user journey from monthly setup to month close.
  - Migration verification with legacy database fixtures.
  - Backup and restore full-cycle test.
  - Offline operation and UI theme / accessibility tests.
  - Final documentation update and release readiness.

---

## 8. Verification Results & Audit Summary (Phase 0)

- **Unmodified Baseline Status**:
  - Repository structure, modules, Room schemas, and build configurations audited in detail.
  - Build execution attempted: environment lacks JDK 17+ and Android SDK command line tools. Blocker documented.
- **Git Remote & Fork Configuration**:
  - Verified and configured: `origin` is set to `git@github-personal:dev-rar/ivy-wallet.git` utilizing personal SSH identity `~/.ssh/id_ed25519_personal`. Remote fetch succeeded.
- **Preservation & Architecture**:
  - All existing entities, DAOs, screens, and viewmodels cataloged with exact file references.
  - Safe extension points identified without altering existing behavior or introducing breaking schema changes.
- **Documentation**:
  - Plan established at `docs/budgeting-extension-plan.md`.

## 9. Verification Results (Phase 1 — Compatibility and Data Foundations)

- **Environment & Build Baseline**:
  - Microsoft OpenJDK 17.0.21 installed and configured with `JAVA_HOME`.
  - Android SDK Command-line tools (latest) and platforms (`platforms;android-35`, `build-tools;35.0.0`, `platform-tools;37.0.1`) configured in `local.properties`.
  - Full project test tasks compile cleanly; Gradle configuration cache primed and operational.
- **Domain Foundations**:
  - Established domain types in `:shared:data:model`:
    - `com.ivy.data.model.AccountType` (`REGULAR`, `CASH`, `BANK`, `INVESTMENT`, `LIABILITY`)
    - `com.ivy.data.model.budget.BudgetPeriod` (`YYYY-MM` value class with strict validation, `Comparable`, and month arithmetic)
    - `com.ivy.data.model.budget.BudgetGroupType`
    - `com.ivy.data.model.budget.BudgetItemType`
- **Unit Testing**:
  - `BudgetPeriodTest` verifies `parseOrNull`, invalid format rejection, month navigation (`next`/`previous` over year boundaries), and temporal ordering.
- **Legacy Compatibility & Backup Regression**:
  - Created representative legacy fixture: `shared/data/core/src/test/resources/backups/legacy-representative-wallet.json` containing cash, bank, savings accounts, income and expense categories, multi-type transactions (transfers, income, expense), loans, loan records, planned payments, and settings in PKR.
  - Added regression test `backup compatibility with legacy representative wallet` in `BackupDataUseCaseTest`.
  - Verified round-trip import, export, and re-import data integrity via `./gradlew :shared:data:model:test :shared:data:core:test`. (154 actionable tasks: 78 executed, 76 up-to-date, BUILD SUCCESSFUL).



## 10. Verification Results (Phase 2 — Monthly Zero-Based Budgeting)

- **Room Schema & Migration 130 to 131**:
  - Implemented Room entities in `shared/data/core`:
    - `MonthlyBudgetPlanEntity` (period `YYYY-MM`, plannedIncome, carryoverAmount, isClosed, createdAt)
    - `BudgetGroupEntity` (planPeriod, name, groupType, orderNum)
    - `BudgetItemEntity` (groupId, planPeriod, name, plannedAmount, itemType, categoryId, orderNum)
  - Created Room DAOs: `MonthlyBudgetDao`, `WriteMonthlyBudgetDao`, and in-memory `FakeMonthlyBudgetDao`.
  - Created Room migration `Migration130to131_BudgetingFoundations` and bumped `IvyRoomDatabase` version to 131. Exported Room schema generated at `shared/data/core/schemas/com.ivy.data.db.IvyRoomDatabase/131.json`.
  - Extended `IvyWalletCompleteData` backup container with default empty lists for full backwards and forwards serialization compatibility.
- **Repositories & Domain Use Cases**:
  - `MonthlyBudgetRepository`: Handles plan creation, default EveryDollar group seeding, group additions/deletions, item modifications, and deep "Copy Previous Month" duplication.
  - `GetMonthlyPlanSummaryUseCase`: Aggregates planned income, carryover, planned allocations, calculates real-time actual transaction spending matched by category, and enforces zero-based balancing:
    $$\text{Left to Assign} = \text{Planned Income} + \text{Carryover} - \sum \text{Planned Allocations}$$
  - `CopyPreviousMonthPlanUseCase` & `SaveBudgetItemUseCase`: Domain use cases for monthly workflow.
  - Unit tests in `MonthlyPlanSummaryTest` verifying zero-based balance math, item progress, and remaining calculations.
- **UI Architecture & Material Compose Screen**:
  - Top tab switcher (`TabSelector`): Seamless toggle between `[ Monthly Plan ]` and `[ Classic ]` category budgets.
  - `MonthlyPeriodHeader`: Arrow navigation across past/future months (`YYYY-MM`) with "Copy Plan from [Previous Month]" quick action.
  - `ZeroBasedSummaryBanner`: Real-time status badge ("Every Dollar/Rupee has a job" / "X left to assign" / "Over-assigned"), planned income, carryover, and total allocations.
  - `BudgetGroupSection` & `BudgetItemRow`: Group cards with + Add Item button, category dot, item name, planned vs spent, remaining amount, and visual progress bar.
  - `EditBudgetItemModal` & `EditIncomeModal`: Material bottom sheets to edit planned income, carryovers, budget items, and category linkages.
- **Test & Build Verification**:
  - `./gradlew.bat :shared:data:core:test` -> BUILD SUCCESSFUL (all unit and migration tests pass).
  - `./gradlew.bat :shared:domain:testDebugUnitTest` -> BUILD SUCCESSFUL.
  - `./gradlew.bat :feature:budgets:testDebugUnitTest` -> BUILD SUCCESSFUL.
  - `./gradlew.bat :app:assembleDebug` -> BUILD SUCCESSFUL (`app-debug.apk` built: 105.1 MB).
