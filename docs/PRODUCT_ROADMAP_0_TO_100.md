# Product & Engineering Roadmap (0 → 100)

> **Purpose:** Master tracking document for evolving this extended version of Ivy Wallet into a commercial-ready, publishable **Personal & Small-Business Accounting + Smart Cash-Flow App** on Google Play.
>
> **Overall Progress:** `15 / 100` (Phase 0 Completed)

---

## Table of Contents
1. [Executive Overview & Product Vision](#1-executive-overview--product-vision)
2. [Phase 0: Modern UI, Budgeting & Reporting Foundations (0% → 15% — DONE)](#2-phase-0-modern-ui-budgeting--reporting-foundations-0--15--done)
3. [Phase 1: Rebranding, Legal Compliance & App Identity (15% → 25%)](#3-phase-1-rebranding-legal-compliance--app-identity-15--25)
4. [Phase 2: Google Sign-In & Google Drive Cloud Sync (25% → 45%)](#4-phase-2-google-sign-in--google-drive-cloud-sync-25--45)
5. [Phase 3: Core Accounting & Bookkeeping Features (45% → 70%)](#5-phase-3-core-accounting--bookkeeping-features-45--70)
6. [Phase 4: Smart Bank Automation — Zero-Server-Cost Bank Sync (70% → 85%)](#6-phase-4-smart-bank-automation--zero-server-cost-bank-sync-70--85)
7. [Phase 5: Monetization & Pro Subscription System (85% → 95%)](#7-phase-5-monetization--pro-subscription-system-85--95)
8. [Phase 6: Production QA & Google Play Store Launch (95% → 100%)](#8-phase-6-production-qa--google-play-store-launch-95--100)

---

## 1. Executive Overview & Product Vision

### Product Positioning
Transform the open-source Ivy Wallet foundation into a modern fintech & accounting app built around:
- **Cash-Flow-First Design:** Dark-metallic credit-card hero summaries on Home & Reports.
- **True Accounting Capabilities:** Running-balance account ledgers, reconciliation, balance sheets (Assets vs. Liabilities), split journal entries, reimbursable tracking, and formal P&L / PDF statements.
- **Zero-Server-Cost Cloud & Automation:** Google Sign-In + user-owned Google Drive backup/restore and on-device bank SMS/notification transaction parsing.

### Naming & Positioning Candidates
| Name | Tagline | Focus |
| :--- | :--- | :--- |
| **LedgerFlow** *(Recommended)* | *Smart Personal Accounting & Cash Flow* | Balances fintech aesthetics with accounting trust |
| **CashPulse** | *Cash Flow & Budget Tracker* | Everyday cash-flow & spending awareness |
| **VaultFlow** | *Budgets, Ledgers & Cash Flow* | Security-oriented finance management |
| **FinCrest** | *Personal Finance & Statements* | Reporting and financial clarity |
| **PocketLedger** | *Accounts, Budgets & Reports* | Clean, approachable bookkeeping |

### Freemium Membership Model
- **Free Tier (Forever Usable Offline):** Manual income/expense/transfer tracking, accounts, categories, basic budgets & reports, manual local JSON/ZIP export & import.
- **Pro Tier (Monthly / Yearly / Lifetime):** Automated Google Drive daily backup & 1-tap restore, smart bank notification auto-tracker, formal PDF/Excel financial statements, unlimited budgets & historical multi-year insights, and advanced accounting tools (split entries, reconciliation, party sub-ledgers).

---

## 2. Phase 0: Modern UI, Budgeting & Reporting Foundations (0% → 15% — DONE)

- [x] **0.1 Category-Level Monthly Budgets**: Added `budget: Double?` to `Category` Room schema and migration so monthly budgets can be assigned directly per category.
- [x] **0.2 Redesigned Home & Report Cash-Flow Credit Cards**:
  - Added dark-metallic credit-card layout (`HomeHeader.kt` & `AnnualSummaryCard.kt`) with EMV chip graphic, `"CASH FLOW"` label, `"X.X% Saved"` pill badge, full net cash flow center row, and compact `13.sp` Income/Expense footer without ellipsis (`...`) truncation.
  - Fixed `HeaderStickyRow` in `HomeHeader.kt` so balances no longer overlap the top navigation bar while scrolling.
- [x] **0.3 Interactive Annual & Monthly Financial Dashboard (`feature/reports`)**:
  - Moved `< Year >` selector cleanly to the top-right next to the `"Reports"` title (`ReportScreen.kt`).
  - Built `MonthlyAreaChart` and `SelectedMonthOverviewSection` featuring Budget at top-left, Saved/Net Loss in center, and Income/Expenses in footer.
  - Replaced report transaction dump with unified Category Breakdown cards matching the Categories screen, plus income-dominant category awareness in `CategoryTrendBottomSheet`.
- [x] **0.4 Material 3 Balance Widget (`widget/balance`)**:
  - Updated Jetpack Glance Home Screen widget with Material 3 styling and live monthly budget remaining status.

---

## 3. Phase 1: Rebranding, Legal Compliance & App Identity (15% → 25%)

### 3.1 Brand Identity
- [ ] Finalize app name, brand tagline, and accent color tokens.
- [ ] Design adaptive vector launcher icons & splash screen assets (`app/src/main/res/mipmap-*`, `res/drawable/`).
- [ ] Update Play Store graphics strategy (icon, feature graphic, dark-mode phone mockups).

### 3.2 Package, Signing & Build Configuration (`app/build.gradle.kts`)
- [ ] Change `applicationId` and `namespace` from `com.ivy.wallet` to unique commercial package (e.g., `com.<brand>.ledgerflow`).
- [ ] Update `resValue("string", "app_name", ...)` for both debug and release build types in `app/build.gradle.kts`.
- [ ] Create dedicated **Firebase Project**, enable Crashlytics & Auth, and replace `app/google-services.json`.
- [ ] Generate a new production upload keystore (`sign.jks`), document backup location offline, and wire signing environment variables (`SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`).

### 3.3 GNU GPLv3 License Compliance (`LICENSE`, `feature/attributions`)
- [ ] Retain the `LICENSE` (GNU GPLv3) in the repository.
- [ ] Update `feature/attributions` and Settings → About section to explicitly credit Ivy Wallet and its contributors under GPLv3 alongside a link to the public client source repository.
- [ ] Verify no Ivy Wallet trademarks or store-brand strings remain that could trigger Play Store impersonation rejection.

---

## 4. Phase 2: Google Sign-In & Google Drive Cloud Sync (25% → 45%)

> **Architecture Goal:** Zero backend infrastructure cost (`$0`/month) by storing encrypted/JSON backups inside each user's personal Google Drive.

### 4.1 Google Authentication (`shared/data/core` & `feature/settings`)
- [ ] Add Android Credential Manager / `GoogleSignInClient` OAuth 2.0 integration in a dedicated `cloud-sync` helper or inside `shared/data/core`.
- [ ] Request minimal Drive scope: `DriveScopes.DRIVE_APPDATA` (hidden app folder) or `DriveScopes.DRIVE_FILE` (user-visible folder for easy manual access).
- [ ] Persist signed-in profile metadata (`email`, `displayName`, `photoUrl`, `lastCloudSyncEpochMillis`) in `DataStore`.
- [ ] Build **"Account & Cloud Sync" Card** at the top of `feature/settings` showing sign-in button, connected Google account badge, and last backup timestamp.

### 4.2 Google Drive Backup & Restore Engine
- [ ] Hook into existing `BackupDataUseCase` (`shared/data/core/src/main/java/com/ivy/core/domain/pure/backup/BackupDataUseCase.kt` / related backup package) to generate the deterministic JSON/ZIP snapshot.
- [ ] Implement `GoogleDriveSyncRepository`:
  - `uploadBackup(snapshotBytes, fileName)` (create or update backup file in Drive with timestamp metadata).
  - `listAvailableBackups()` (return latest backup size & modified date).
  - `downloadAndRestoreBackup(fileId)` (atomic local restore with conflict warning).
- [ ] Add **1-Tap "Backup Now"** and **"Restore from Google Drive"** actions in Settings.
- [ ] Hook **"Sign in with Google to Restore"** into `feature/onboarding` so users switching phones recover all data immediately.

### 4.3 Automated Daily Cloud Backup (`WorkManager`)
- [ ] Create `CloudBackupWorker` (`CoroutineWorker`) triggered via `WorkManager` every 24 hours with `NetworkType.CONNECTED` (and optional Wi-Fi-only toggle).
- [ ] Gate automatic background sync behind **Pro Entitlement** (manual local backup remains free forever).

---

## 5. Phase 3: Core Accounting & Bookkeeping Features (45% → 70%)

### 5.1 Account Ledger with Running Balance & Reconciliation (`feature/accounts`, `feature/transactions`)
- [ ] Compute cumulative **Running Balance** after every chronological entry inside single-account transaction histories.
- [ ] Add `isReconciled: Boolean = false` column on transaction entity with a Room database migration.
- [ ] Build **"Reconcile Account" Wizard**:
  1. User inputs actual real-world bank/cash closing balance.
  2. App compares `Book Balance` vs. `Actual Balance` and highlights the discrepancy.
  3. One-tap posts an automatic `Reconciliation Adjustment` transaction and marks reconciled items as cleared.

### 5.2 True Balance Sheet & Net Worth Statement (`feature/reports`, `feature/accounts`)
- [ ] Extend `Account` classification (`AssetType`: Cash, Bank, Investment, Receivable) vs. Liabilities (`CreditCard`, `Overdraft`, `Payable`), integrating existing `feature/loans` balances.
- [ ] Add **Balance Sheet / Net Worth** summary view in `feature/reports` calculating:
  - **Total Assets** − **Total Liabilities** = **Net Worth (Equity)**
  - **Debt-to-Asset Ratio** and historical Net Worth trend curve.

### 5.3 Split Transactions / Multi-Category Journal Entries (`feature/edit-transaction`)
- [ ] Introduce `TransactionSplitEntity` (`parentTransactionId`, `categoryId`, `amount`, `note`) with Room foreign key & migration.
- [ ] Update `EditTransactionScreen` with a **"Split Across Categories"** option that enforces `sum(splits) == transaction.amount`.
- [ ] Update `ReportViewModel` & category budget aggregators so split amounts roll up accurately into each category while the account ledger retains the single bank deduction/deposit.

### 5.4 Reimbursable Expenses & Party Sub-Ledgers (`feature/loans`, `feature/transactions`)
- [ ] Add `isReimbursable: Boolean` and optional `counterpartyName / partyId` fields on transactions.
- [ ] Exclude pending reimbursable items from personal monthly category expenses until settled (tracked under **Pending Reimbursements / Accounts Receivable**).
- [ ] Add a **Party Sub-Ledger** view showing running `Debit / Credit` balance per person, client, or vendor with settle-up shortcut.

### 5.5 Formal P&L Statement & PDF Export (`feature/reports`)
- [ ] Group categories into **Operating Income**, **Fixed Expenses**, and **Variable Expenses** for a structured **Profit & Loss (P&L)** breakdown.
- [ ] Implement on-device Android `PdfDocument` exporter generating a multi-section **Monthly / Annual Financial Statement PDF** (Cash Flow summary, P&L table, Category Breakdown, and Balance Sheet) shareable via `FileProvider`.

---

## 6. Phase 4: Smart Bank Automation — Zero-Server-Cost Bank Sync (70% → 85%)

### 6.1 Bank SMS & Push Notification Auto-Tracker
- [ ] Implement an opt-in `BankNotificationListenerService` (`NotificationListenerService`) — strictly on-device, zero external data transmission.
- [ ] Build extensible regex/rule-based parser engine supporting debit/credit alerts from major banks & mobile wallets:
  - Extract `Amount`, `Currency`, `TransactionType` (Expense vs. Income), `Account Last-4 / Bank Name`, and `Merchant`.
  - Include bank/account mapping UI (`Bank "HBL xx4821" → Account "HBL Savings"`).
- [ ] Add **"Pending Auto-Captured Transactions"** review queue (plus optional instant auto-post toggle) so users verify new entries in 1 tap.

### 6.2 Smart Bank Statement Importer (`feature/import-data`)
- [ ] Enhance CSV/Excel/PDF bank statement importer with interactive column mapping (Date, Description, Debit, Credit, Balance).
- [ ] Add fingerprint-based duplicate detection (`date + amount + normalizedDescription`) so re-importing overlapping statements never creates duplicate entries.

---

## 7. Phase 5: Monetization & Pro Subscription System (85% → 95%)

### 7.1 Entitlement Architecture (`shared/data/core` or dedicated `:shared:billing` module)
- [ ] Define `ProFeature` gates (`GOOGLE_DRIVE_SYNC`, `BANK_AUTO_CAPTURE`, `PDF_STATEMENTS`, `SPLIT_TRANSACTIONS`, `ACCOUNT_RECONCILIATION`, `UNLIMITED_BUDGETS`).
- [ ] Implement `ProEntitlementManager` exposing `StateFlow<EntitlementState>` combining Google Play Billing state with cached offline grace period.

### 7.2 Google Play Billing v7 Integration & Paywall UI
- [ ] Integrate `com.android.billingclient:billing-ktx` (Billing Library v7).
- [ ] Configure SKUs in Google Play Console: Monthly Subscription (with 7-day free trial), Annual Subscription (~40% discount), and One-Time Lifetime Unlock.
- [ ] Design **Pro Upgrade Paywall Screen** matching the dark-metallic credit card visual language with clear Free vs. Pro comparison table and Restore Purchases action.

---

## 8. Phase 6: Production QA & Google Play Store Launch (95% → 100%)

- [ ] **6.1 Policies & Google OAuth Verification**:
  - Publish Privacy Policy explaining local-first SQLite storage, optional user-owned Google Drive backup, and optional local-only notification parsing.
  - Complete Google Cloud OAuth consent screen & restricted-scope verification (if using `drive.file` / `drive.appdata`).
  - Complete Google Play **Data Safety** form and sensitive permission declarations (only request permissions you actively ship).
- [ ] **6.2 Automated Testing & Release Hardening**:
  - Verify R8/ProGuard keep rules for Room entities, Kotlin Serialization, BillingClient, and Google Drive API models.
  - Test full backup → uninstall → clean install → Google Sign-In → restore flow on physical devices.
  - Run `./gradlew.bat bundleRelease` and smoke-test release `.aab` via Play Internal Testing track.
- [ ] **6.3 Staged Production Rollout**:
  - Publish store listing screenshots showcasing Home Cash-Flow Card, Reports Area Chart & Breakdown, Accounting Ledgers, and Dark/Light themes.
  - Roll out `10% → 50% → 100%` while monitoring Firebase Crashlytics for crash-free sessions `>= 99.5%`.

---

## Milestone Progress Tracker

| Milestone | Scope | Status | Progress |
| :--- | :--- | :--- | :--- |
| **Phase 0** | Modern Credit Card UI, Category Budgets, Reports, Widget | Complete | 100% |
| **Phase 1** | Rebranding, Firebase, Signing, GPLv3 Compliance | Not Started | 0% |
| **Phase 2** | Google Sign-In & Google Drive Auto-Backup/Restore | Not Started | 0% |
| **Phase 3** | Running Ledger, Reconciliation, Balance Sheet, Splits, PDF | Not Started | 0% |
| **Phase 4** | Bank Notification Auto-Tracker & Smart Statement Import | Not Started | 0% |
| **Phase 5** | Free/Pro Gating & Google Play Billing v7 Paywall | Not Started | 0% |
| **Phase 6** | OAuth Review, Release QA & Google Play Launch | Not Started | 0% |
