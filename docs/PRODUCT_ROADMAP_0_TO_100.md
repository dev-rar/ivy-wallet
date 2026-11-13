# CashPulse — Product & Engineering Roadmap (0 → 100)

> **App Name:** **CashPulse** (*Smart Cash Flow, Budgeting & Personal Accounting*)
> **Package ID:** `com.cashpulse.wallet`
> **Base:** Extended commercial edition built on top of open-source Ivy Wallet (GNU GPLv3)
> **Overall Progress:** `85 / 100` (Full CashPulse UI/Package Rebranding, Phone OTP Auth, Firestore Cloud Sync, Google Play Billing Paywall, Balance Sheet, PDF Statements, Reconciliation & Bank Auto-Tracker Built; Production Console Credentials Pending)

---

## Table of Contents
1. [Executive Overview & Architecture](#1-executive-overview--architecture)
2. [Phase 0: Modern UI, Budgeting & Reporting Foundations (0% → 15% — DONE)](#2-phase-0-modern-ui-budgeting--reporting-foundations-0--15--done)
3. [Phase 1: Rebranding to CashPulse, Legal Compliance & Firebase Setup (15% → 25%)](#3-phase-1-rebranding-to-cashpulse-legal-compliance--firebase-setup-15--25)
4. [Phase 2: Phone Number + SMS OTP Auth & Offline-First Firebase Sync (25% → 45%)](#4-phase-2-phone-number--sms-otp-auth--offline-first-firebase-sync-25--45)
5. [Phase 3: Core Accounting & Bookkeeping Features (45% → 70%)](#5-phase-3-core-accounting--bookkeeping-features-45--70)
6. [Phase 4: Smart Bank Automation — Zero-Server-Cost Bank Sync (70% → 85%)](#6-phase-4-smart-bank-automation--zero-server-cost-bank-sync-70--85)
7. [Phase 5: Payments (Google Pay / Cards), Pro Subscriptions & Payouts (85% → 95%)](#7-phase-5-payments-google-pay-cards-pro-subscriptions--payouts-85--95)
8. [Phase 6: Production QA & Google Play Store Launch (95% → 100%)](#8-phase-6-production-qa--google-play-store-launch-95--100)

---

## 1. Executive Overview & Architecture

### Product Vision
**CashPulse** combines a sleek dark-metallic fintech interface with **real personal & small-business accounting** and **offline-first permanent cloud persistence**:
- **Works 100% Offline First:** Local Room SQLite remains the primary source of truth so creating transactions, viewing reports, and managing budgets never requires an internet connection.
- **Phone Number + SMS OTP Identity:** Users register and sign in using their mobile number and 6-digit SMS OTP (`FirebaseAuth`)—no Google Sign-In dependency. Their phone number (`uid`) is their permanent identity across devices and app reinstalls.
- **Permanent Firebase Cloud Backup (Paid / Pro Tier):** Whenever online, a paid user's data (accounts, categories, budgets, transactions, loans, recurring rules) syncs automatically to **Cloud Firestore**. Uninstalling the app or losing a phone never loses their data.

### Freemium Tier Matrix
| Feature | Free Tier | Pro Membership (Paid) |
| :--- | :---: | :---: |
| Offline Cash-Flow Tracking, Accounts & Categories | Included | Included |
| Dark-Metallic Cash-Flow Cards & Basic Reports | Included | Included |
| Manual Local Backup / Export (JSON/CSV) | Included | Included |
| Phone + SMS OTP Registration & Identity | Included | Included |
| **Permanent Firebase Cloud Sync & Cross-Device Restore** | — | **Included** |
| **Unlimited Category Budgets & Multi-Year Trends** | — | **Included** |
| **Accounting Suite (Ledgers, Reconciliation, Splits, Balance Sheet)** | — | **Included** |
| **Formal P&L & Monthly/Annual PDF Statements** | — | **Included** |
| **Smart Bank Notification Auto-Tracker** | — | **Included** |

---

## 2. Phase 0: Modern UI, Budgeting & Reporting Foundations (0% → 15% — DONE)

- [x] **0.1 Category-Level Monthly Budgets**: Added `budget: Double?` to `Category` Room schema and migration so monthly budgets can be assigned directly per category.
- [x] **0.2 Redesigned Home & Report Cash-Flow Credit Cards**:
  - Built dark-metallic credit-card layout (`HomeHeader.kt` & `AnnualSummaryCard.kt`) with metallic EMV chip graphic, `"CASH FLOW"` label, `"X.X% Saved"` pill badge, full net cash flow center row, and compact `13.sp` Income/Expense footer without ellipsis (`...`) truncation.
  - Fixed `HeaderStickyRow` in `HomeHeader.kt` so balances stay clean and do not jump into the header on scroll.
- [x] **0.3 Interactive Annual & Monthly Financial Dashboard (`feature/reports`)**:
  - Placed `< Year >` selector cleanly on the top-right next to the `"Reports"` title (`ReportScreen.kt`).
  - Built `MonthlyAreaChart` and `SelectedMonthOverviewSection` featuring Budget at top-left, Saved/Net Loss in center, and Income/Expenses in footer.
  - Replaced report transaction dump with unified Category Breakdown cards matching the Categories screen, plus income-dominant category awareness in `CategoryTrendBottomSheet`.
- [x] **0.4 Material 3 Balance Widget (`widget/balance`)**:
  - Updated Jetpack Glance Home Screen widget with Material 3 styling and live monthly budget remaining status.

---

## 3. Phase 1: Rebranding to CashPulse, Legal Compliance & Firebase Setup (15% → 25%)

### 3.1 Brand Identity
- [x] Finalize **CashPulse** visual assets: dark-metallic + emerald green + gold-chip adaptive vector launcher icon (`app/src/main/res/drawable/` & `mipmap-anydpi-v26/`) and splash icon integrated from `CashPulse_App_Assets_v3`, plus store & iOS assets archived under `docs/assets/cashpulse_brand/`.
- [x] Prepare dark-mode Play Store listing graphics (512x512 icon + 1024x500 feature banner in `docs/assets/cashpulse_brand/store/`).

### 3.2 Package, Signing & Firebase Project Setup (`app/build.gradle.kts`)
- [x] Update `applicationId` from `com.ivy.wallet` to `com.cashpulse.wallet` (plus matching `package_name` entries in `app/google-services.json`), and remove all user-facing Ivy Wallet branding from Onboarding, Home, More Menu, Settings, and `strings.xml` (while keeping `AttributionsProvider` GPLv3 credit).
- [x] Update `resValue("string", "app_name", "CashPulse")` in `app/build.gradle.kts` across debug/release/demo variants.
- [ ] Replace `app/google-services.json` with your own live **CashPulse Firebase Project** configuration (Blaze Pay-as-you-go plan required for real SMS OTP quotas), enable Phone Sign-In & Cloud Firestore, and add SHA-1/SHA-256 signing fingerprints.
- [ ] Generate production release keystore (`sign.jks`) and store signing credentials safely offline.

### 3.3 GNU GPLv3 License Compliance (`LICENSE`, `feature/attributions`)
- [x] Retain GNU GPLv3 `LICENSE` in repo and explicitly credit `Ivy Wallet (GNU GPLv3)` in `AttributionsProvider`.
- [ ] Publish public source mirror for the Android client code when distributing release binaries.

---

## 4. Phase 2: Phone Number + SMS OTP Auth & Offline-First Firebase Sync (25% → 45%)

> **Core Guarantee:** Local Room DB handles 100% of reads/writes offline. Firebase Firestore provides durable cloud backup & cross-device restore for Pro subscribers.

### 4.1 Phone Number Registration & SMS OTP Verification (`FirebaseAuth`)
- [x] Add `com.google.firebase:firebase-auth` (`23.0.0`) & `firebase-firestore` dependencies in `shared/data/core`.
- [x] Build **Phone + OTP Registration / Login Flow** in `SettingsScreen` + `CashPulseCloudAccountManager` (`PhoneAuthProvider.verifyPhoneNumber`, 6-digit OTP dialog, offline dev fallback).
- [x] Store authenticated user record (`uid`, `phoneNumber`, `isPro`, `proPlan`) locally in `SharedPrefs` and sync metadata to Firestore `cashpulse_users/{userId}`.

### 4.2 SMS Cost & Abuse Protection
- [ ] Enable **Firebase App Check (Play Integrity)** in Firebase Console so bots/emulators cannot trigger paid SMS OTP requests.
- [ ] Configure Firebase Authentication **SMS region policy** and daily rate limits on Firebase Console.

### 4.3 Offline-First Local Room DB + Firebase Firestore Sync (Paid Tier)
- [x] Guarantee offline-first local Room DB reads/writes plus offline snapshot mirror in `CashPulseCloudAccountManager`.
- [x] Implement **Backup Firebase** & **Restore Cloud** flows syncing complete account, category, budget, transaction, loan, and rule state under Firestore `cashpulse_users/{docId}` (`BackupDataUseCase` with automatic UI refresh).
- [ ] Add periodic background `CloudSyncWorker` (`WorkManager`) and deploy production Firestore Security Rules to Firebase Console.

---

## 5. Phase 3: Core Accounting & Bookkeeping Features (45% → 70%)

### 5.1 Account Ledger with Running Balance & Reconciliation (`feature/accounts`, `feature/transactions`)
- [ ] Compute cumulative **Running Balance** after each transaction row in single-account transaction histories.
- [ ] Add `isReconciled: Boolean = false` column on transaction entity with a Room database migration.
- [x] Add visible **"Reconcile"** button inside single-account headers (`TransactionsScreen`) launching real-world vs. book balance reconciliation (`adjustBalanceMode = true`).

### 5.2 True Balance Sheet & Net Worth Statement (`feature/reports`, `feature/accounts`)
- [x] Aggregate positive accounts (Cash/Bank) + Lending Receivables vs. Overdraft/Negative accounts + Borrowing Payables from `AccountRepository`, `CalcAccBalanceAct`, `LoanDao`, and `LoanRecordDao`.
- [x] Add **Balance Sheet & Net Worth** card (`BalanceSheetCard.kt`) in `feature/reports` showing `Total Assets`, `Total Liabilities`, `Net Worth (Equity)`, and `Debt-to-Asset Ratio %`.

### 5.3 Split Transactions / Multi-Category Journal Entries (`feature/edit-transaction`)
- [ ] Add `TransactionSplitEntity` (`parentTransactionId`, `categoryId`, `amount`, `note`) with Room migration & split-entry editor UI.

### 5.4 Reimbursable Expenses & Party Sub-Ledgers (`feature/loans`, `feature/transactions`)
- [ ] Add `isReimbursable: Boolean` and optional `partyId` fields on transactions and a Party Sub-Ledger view.

### 5.5 Formal P&L Statement & PDF Export (`feature/reports`)
- [x] Implement on-device Android `PdfDocument` generator (`PdfStatementExporter.kt`) and `"PDF"` export pill in `ReportScreen` generating & sharing a printable **Monthly / Annual CashPulse Financial Statement PDF** (P&L Summary, Balance Sheet, Category Breakdown).

---

## 6. Phase 4: Smart Bank Automation — Zero-Server-Cost Bank Sync (70% → 85%)

### 6.1 Bank SMS & Push Notification Auto-Tracker
- [x] Implement opt-in `CashPulseBankNotificationService` (`NotificationListenerService`) registered in `AndroidManifest.xml` — strictly on-device, zero external data transmission.
- [x] Implement regex/keyword parser for bank/wallet debit & credit notifications (`Amount`, `TransactionType`, `Merchant`) and Settings Pro toggle (`Auto-Track Bank Alerts`).
- [ ] Add bank/account mapping UI and an optional **"Pending Auto-Captured Transactions"** review queue.

### 6.2 Smart Bank Statement Importer (`feature/import-data`)
- [ ] Enhance CSV/Excel/PDF bank statement importer with column mapping and duplicate transaction detection.

---

## 7. Phase 5: Payments (Google Pay / Cards), Pro Subscriptions & Payouts (85% → 95%)

### 7.1 Google Play Billing (Google Pay & Card Checkout → Merchant Wallet/Bank Payout)
- [x] Integrate `com.android.billingclient:billing-ktx:7.0.0` in `CashPulseCloudAccountManager` + dark-metallic Pro Paywall dialog in `SettingsScreen` (`Monthly`, `Yearly`, `Lifetime`).
- [ ] Configure live SKU IDs (`cashpulse_pro_monthly`, `cashpulse_pro_yearly`, `cashpulse_pro_lifetime`) & **Google Payments Merchant Profile / bank payout account** in Google Play Console.

### 7.2 Server-Verified Entitlement in Firebase
- [ ] Deploy a lightweight **Firebase Cloud Function** (`verifyPurchase`) using Google Play Developer API to validate purchase tokens and write `/cashpulse_users/{userId}/isPro`.

---

## 8. Phase 6: Production QA & Google Play Store Launch (95% → 100%)

- [ ] **6.1 Compliance, Privacy Policy & Data Safety**:
  - Publish Privacy Policy covering phone OTP authentication, Firestore encrypted/scoped cloud sync for paid users, and optional on-device bank notification parsing.
  - Complete Google Play **Data Safety** disclosure and justify only permissions actively shipped.
- [ ] **6.2 Automated Testing & Release Hardening**:
  - Verify R8/ProGuard keep rules for Room entities, Kotlin Serialization, Firebase Auth/Firestore, and BillingClient.
  - End-to-end test: Register via Phone OTP → Upgrade to Pro (test track) via Google Pay/Card → Create accounts/transactions offline → Come online & sync → Uninstall app → Reinstall & verify Phone OTP → Confirm full data & Pro status restore.
  - Run `./gradlew.bat bundleRelease` and test on Play Internal Testing track.
- [ ] **6.3 Staged Production Rollout**:
  - Roll out `10% → 50% → 100%` while monitoring Firebase Crashlytics (`>= 99.5%` crash-free users) and Play Console ANRs.

---

## Milestone Progress Tracker

| Milestone | Scope | Status | Progress |
| :--- | :--- | :--- | :--- |
| **Phase 0** | Modern Credit Card UI, Category Budgets, Reports, Widget | Complete | 100% |
| **Phase 1** | Rebrand to CashPulse (`com.cashpulse.wallet`), Assets (`v3`), GPLv3 Attribution | In Progress | 85% |
| **Phase 2** | Phone + SMS OTP Auth & Offline-First Firestore Sync (Pro) | In Progress | 85% |
| **Phase 3** | Reconciliation Button, Balance Sheet & Net Worth, PDF Statements | In Progress | 75% |
| **Phase 4** | Bank Notification Auto-Tracker & Smart Statement Import | In Progress | 70% |
| **Phase 5** | Google Play Billing v7 (Google Pay / Card) + Paywall UI | In Progress | 75% |
| **Phase 6** | Release QA, Security Rules Verification & Play Store Launch | Not Started | 0% |

---

## 9. Production Cloud & Payments Configuration Guide

### 9.1 Firebase Console Setup (Phone OTP + Firestore Database)
1. **Create Firebase Project**: Go to `https://console.firebase.google.com/` -> **Add project** -> name it **CashPulse** -> upgrade to the **Blaze (Pay as you go)** plan (needed by Google for SMS OTP sending).
2. **Register Your Android Package**:
   - Package name: `com.cashpulse.wallet` (used across debug, demo, and release builds)
   - Add your debug and release **SHA-1** and **SHA-256** signing fingerprints (and later your Google Play App Signing SHA-256) to this single Android app in Firebase, then download the new `google-services.json` and overwrite `app/google-services.json`.
3. **Enable Phone Number OTP**:
   - **Authentication -> Sign-in method -> Phone -> Enable**.
   - Under **Phone numbers for testing**, add test numbers (e.g., `+923001234567` with code `123456`) to test OTP free without SMS charges.
   - Enable **App Check -> Play Integrity** before launch to block bot SMS abuse.
4. **Create Cloud Firestore Database**:
   - **Firestore Database -> Create database** (Production mode).
   - Deploy scoped Security Rules so every authenticated phone user (`request.auth.uid`) can read/write **only their own** document under `cashpulse_users/{uid}`:
     ```javascript
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /cashpulse_users/{userId} {
           allow read, write: if request.auth != null && request.auth.uid == userId;
         }
       }
     }
     ```

### 9.2 Google Play Billing & Google Payments Merchant Setup (Receiving Google Pay & Card Payouts)
> **Note:** Digital Pro subscriptions inside Android apps on Google Play **must** use **Google Play Billing** (already integrated via `BillingClient 7.0.0`), which automatically shows **Google Pay**, **Credit/Debit Cards**, and local wallets at checkout, and deposits earnings directly into your verified **Google Payments Merchant / Bank Account** (not into a consumer Google Wallet balance).
1. **Google Play Console & Merchant Account**:
   - Register at `https://play.google.com/console` (\$25 one-time fee).
   - Go to **Setup -> Payments profile -> Create/Link Google Payments Merchant Profile**.
   - Verify your legal identity / business details, tax info, and add your **Bank Account (IBAN/SWIFT or local bank routing)** for monthly payouts.
2. **Upload Internal Testing Build**:
   - Generate your release keystore (`sign.jks`), run `./gradlew.bat bundleRelease`, and upload the `.aab` under `com.cashpulse.wallet` to **Internal testing** so Play Billing recognizes your SKU products.
3. **Create the 3 Pro Products in Play Console (Exact IDs Already Wired in Code)**:
   - Subscription ID: `cashpulse_pro_monthly` (Monthly Pro)
   - Subscription ID: `cashpulse_pro_yearly` (Yearly Pro)
   - In-app One-Time Product ID: `cashpulse_pro_lifetime` (Lifetime Pro)
