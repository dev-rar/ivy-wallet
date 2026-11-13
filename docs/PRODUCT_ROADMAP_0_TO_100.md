# CashPulse — Product & Engineering Roadmap (0 → 100)

> **App Name:** **CashPulse** (*Smart Cash Flow, Budgeting & Personal Accounting*)
> **Base:** Extended commercial edition built on top of open-source Ivy Wallet (GNU GPLv3)
> **Overall Progress:** `15 / 100` (Phase 0 Completed)

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
- [ ] Finalize **CashPulse** visual assets: dark-metallic + emerald green + gold-chip adaptive vector launcher icon (`app/src/main/res/mipmap-*`) and splash screen.
- [ ] Prepare dark-mode Play Store listing graphics (feature banner + device mockups).

### 3.2 Package, Signing & Firebase Project Setup (`app/build.gradle.kts`)
- [ ] Update `applicationId` and `namespace` from `com.ivy.wallet` to unique commercial package (`com.cashpulse.wallet` or `com.cashpulse.app`).
- [ ] Update `resValue("string", "app_name", "CashPulse")` in `app/build.gradle.kts` across debug/release variants.
- [ ] Create dedicated **CashPulse Firebase Project** (Blaze Pay-as-you-go plan required for Phone Auth SMS), enable:
  - **Firebase Authentication** → Phone Sign-In provider
  - **Cloud Firestore**
  - **Firebase App Check** (Play Integrity API) + Crashlytics
- [ ] Replace `app/google-services.json` with the CashPulse Firebase config and add production SHA-1 / SHA-256 signing fingerprints.
- [ ] Generate production release keystore (`sign.jks`) and store signing credentials safely offline.

### 3.3 GNU GPLv3 License Compliance (`LICENSE`, `feature/attributions`)
- [ ] Retain GNU GPLv3 `LICENSE` in repo and clearly attribute the original Ivy Wallet open-source project in *Settings → About / Attributions*.
- [ ] Publish public source mirror for the Android client code as required by GPLv3 (Firebase rules, Cloud Functions, and backend secrets stay private).

---

## 4. Phase 2: Phone Number + SMS OTP Auth & Offline-First Firebase Sync (25% → 45%)

> **Core Guarantee:** Local Room DB handles 100% of reads/writes offline. Firebase Firestore provides durable cloud backup & cross-device restore for Pro subscribers.

### 4.1 Phone Number Registration & SMS OTP Verification (`FirebaseAuth`)
- [ ] Add `com.google.firebase:firebase-auth-ktx`, `firebase-firestore-ktx`, and Play Integrity / App Check dependencies.
- [ ] Build **Phone + OTP Registration / Login Flow** (accessible in Onboarding and Settings):
  - Country code selector + Phone number formatting/validation (E.164 format, e.g., `+92 3XX XXXXXXX`).
  - Send 6-digit OTP via `PhoneAuthProvider.verifyPhoneNumber(...)` with `60s` resend countdown timer.
  - SMS Retriever / auto-verification support + manual 6-digit OTP pin entry screen.
- [ ] Store authenticated user record (`uid`, `phoneNumber`, `createdAt`, `isPro`) locally in `DataStore` and remotely in Firestore `/users/{uid}/profile`.

### 4.2 SMS Cost & Abuse Protection
- [ ] Enable **Firebase App Check (Play Integrity)** so bots/emulators cannot trigger paid SMS OTP requests.
- [ ] Configure Firebase Authentication **SMS region policy** (allowlist target countries) and daily per-IP / per-phone rate limits to keep Blaze SMS billing under strict control.

### 4.3 Offline-First Local Room DB + Firebase Firestore Sync (Paid Tier)
- [ ] Enable Firestore offline persistence (`FirebaseFirestoreSettings`) + local Room dirty-tracking (`updatedAtEpochMillis`, `isDeleted`, `lastSyncedAt`).
- [ ] Implement `FirebaseSyncRepository` syncing collections under `/users/{uid}/`:
  - `accounts/{accountId}`
  - `categories/{categoryId}` (including monthly `budget`)
  - `transactions/{txId}` (including split lines & reconciliation flags)
  - `loans/{loanId}` & `loanRecords/{recordId}`
  - `plannedPayments/{ruleId}`
- [ ] Implement `CloudSyncWorker` (`WorkManager`) that runs on connectivity changes & periodically (every few hours / 24h) + manual **"Sync Now"** button in Settings.
- [ ] **Reinstall / New Device Restore**:
  - When an existing paid user enters their phone number and verifies the SMS OTP on a fresh install, automatically detect `/users/{uid}` cloud data and restore their complete financial history into local Room DB.
- [ ] Write strict **Firestore Security Rules**:
  ```javascript
  match /users/{userId}/{collection}/{docId} {
    allow read: if request.auth != null && request.auth.uid == userId;
    allow write: if request.auth != null && request.auth.uid == userId
      && get(/databases/$(database)/documents/users/$(userId)/subscription/status).data.isPro == true;
  }
  ```

---

## 5. Phase 3: Core Accounting & Bookkeeping Features (45% → 70%)

### 5.1 Account Ledger with Running Balance & Reconciliation (`feature/accounts`, `feature/transactions`)
- [ ] Compute cumulative **Running Balance** after every chronological entry inside single-account transaction histories.
- [ ] Add `isReconciled: Boolean = false` column on transaction entity with a Room database migration.
- [ ] Build **"Reconcile Account" Wizard**:
  1. User enters actual real-world bank/cash closing balance.
  2. App compares `Book Balance` vs. `Actual Balance` and highlights the discrepancy.
  3. One-tap posts an automatic `Reconciliation Adjustment` transaction and marks reconciled items as cleared.

### 5.2 True Balance Sheet & Net Worth Statement (`feature/reports`, `feature/accounts`)
- [ ] Extend `Account` classification (`Asset`: Cash, Bank, Investment, Receivable) vs. (`Liability`: CreditCard, Overdraft, Payable), integrating `feature/loans`.
- [ ] Add **Balance Sheet / Net Worth** view in `feature/reports`:
  - **Total Assets** − **Total Liabilities** = **Net Worth (Equity)**
  - **Debt-to-Asset Ratio** and historical Net Worth trend chart.

### 5.3 Split Transactions / Multi-Category Journal Entries (`feature/edit-transaction`)
- [ ] Add `TransactionSplitEntity` (`parentTransactionId`, `categoryId`, `amount`, `note`) with Room migration & Firestore sync support.
- [ ] Update `EditTransactionScreen` with **"Split Across Categories"** validating `sum(splits) == transaction.amount`.
- [ ] Update `ReportViewModel` & category budget aggregators so split amounts aggregate accurately per category while the bank account ledger shows the single payment.

### 5.4 Reimbursable Expenses & Party Sub-Ledgers (`feature/loans`, `feature/transactions`)
- [ ] Add `isReimbursable: Boolean` and optional `counterpartyName / partyId` fields on transactions.
- [ ] Exclude pending reimbursable items from personal category expenses until settled (tracked under **Accounts Receivable**).
- [ ] Add a **Party Sub-Ledger** view showing running `Dr / Cr` balance per client/person with 1-tap settle-up.

### 5.5 Formal P&L Statement & PDF Export (`feature/reports`)
- [ ] Group categories into **Operating Income**, **Fixed Expenses**, and **Variable Expenses** for a structured **Profit & Loss (P&L)** statement.
- [ ] Implement on-device Android `PdfDocument` generator creating a printable **Monthly / Annual Financial Statement PDF** shareable via `FileProvider`.

---

## 6. Phase 4: Smart Bank Automation — Zero-Server-Cost Bank Sync (70% → 85%)

### 6.1 Bank SMS & Push Notification Auto-Tracker
- [ ] Implement an opt-in `BankNotificationListenerService` (`NotificationListenerService`) — strictly on-device, zero external data transmission.
- [ ] Build extensible local regex/rule parser for bank & mobile wallet debit/credit notifications (`Amount`, `Currency`, `TransactionType`, `Account Last-4`, `Merchant`).
- [ ] Add bank/account mapping rules (`"Bank xx4821" → "Main Bank Account"`) and a **"Pending Auto-Captured Transactions"** 1-tap review queue.

### 6.2 Smart Bank Statement Importer (`feature/import-data`)
- [ ] Enhance CSV/Excel/PDF bank statement importer with column mapping (Date, Description, Debit, Credit, Balance).
- [ ] Add fingerprint-based duplicate detection (`date + amount + normalizedDescription`) so overlapping statement imports never create duplicates.

---

## 7. Phase 5: Payments (Google Pay / Cards), Pro Subscriptions & Payouts (85% → 95%)

### 7.1 Google Play Billing (Google Pay & Card Checkout → Merchant Wallet/Bank Payout)
> **Important Google Play Policy Note:** For digital app features/subscriptions distributed on Google Play (like Pro cloud backup & accounting), Google Play policy **requires** using **Google Play Billing** (which natively displays **Google Pay** and **Credit/Debit Cards** in Google's checkout sheet, and deposits proceeds directly into your linked **Google Payments Merchant Account / Bank Account**).

- [ ] Integrate `com.android.billingclient:billing-ktx` (Billing Library v7).
- [ ] Configure Pro products in **Google Play Console**:
  - `cashpulse_pro_monthly` (Monthly Subscription with optional 7-day free trial)
  - `cashpulse_pro_yearly` (Annual Subscription — best value)
  - `cashpulse_pro_lifetime` (One-time Lifetime Pro unlock)
- [ ] Connect your **Google Payments Merchant Profile** & bank account inside Google Play Console so Google automatically settles all Google Pay and Card subscription payments on payout schedule.

### 7.2 Server-Verified Entitlement in Firebase
- [ ] Build a lightweight **Firebase Cloud Function** (`verifyPurchase`) using Google Play Developer API:
  - Verifies purchase token with Google Play, prevents receipt spoofing, and updates `/users/{uid}/subscription/status` (`isPro`, `planType`, `expiryTimeMillis`).
  - Grants immediate Firestore cloud sync access tied to the user's phone number (`uid`).
- [ ] Build **CashPulse Pro Paywall Screen** matching the dark-metallic credit-card UI (Free vs. Pro comparison, clear trial/pricing details, Restore Purchases button).
- [ ] *(Optional — Web / Direct Enterprise Billing Outside Play Store)*: Add **Google Pay API (`com.google.android.gms:play-services-wallet`) + Stripe PaymentIntent** if ever selling web/B2B desktop companion plans outside the Play Store.

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
| **Phase 1** | Rebrand to CashPulse, GPLv3 Attribution, Firebase Project & Signing | Not Started | 0% |
| **Phase 2** | Phone + SMS OTP Auth & Offline-First Firestore Sync (Pro) | Not Started | 0% |
| **Phase 3** | Running Ledger, Reconciliation, Balance Sheet, Splits, PDF | Not Started | 0% |
| **Phase 4** | Bank Notification Auto-Tracker & Smart Statement Import | Not Started | 0% |
| **Phase 5** | Google Play Billing (Google Pay / Card) + Merchant Payouts | Not Started | 0% |
| **Phase 6** | Release QA, Security Rules Verification & Play Store Launch | Not Started | 0% |
