# CashPulse (`com.cashpulse.wallet`)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

**CashPulse** is a personal finance, budgeting, and accounting Android app built with **Kotlin & Jetpack Compose**. It is an independent fork and modified derivative of the unmaintained [Ivy Wallet](https://github.com/Ivy-Apps/ivy-wallet) project, maintained and distributed under the **GNU General Public License v3.0 (GPLv3)**.

---

## Copyright, License & Trademark Notice

- **Source License (GNU GPLv3):** All source code in this repository is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for full terms and [NOTICE](NOTICE) for a summary of CashPulse modifications (2026).
- **Disclaimer of Warranty:** This software is distributed on an "AS-IS" basis without any warranty of any kind, per Sections 15 and 16 of the GPLv3.
- **Trademarks & Brand Assets:** The **CashPulse** name, logos, and brand graphics are proprietary trademarks and visual brand assets of CashPulse and are not granted under the GPLv3 code license.
- **Backend Infrastructure:** Hosted Firebase project configuration (`app/google-services.json`), Firestore security deployments, and release signing keystores (`sign.jks`) are private to CashPulse and intentionally excluded from public source control.

---

## Building Locally

### Requirements
- Java 17+
- Android SDK (compileSdk 35 / targetSdk 36 / minSdk 28)

### 1. Firebase Config
Copy the placeholder file or provide your own Firebase configuration:
```powershell
copy app\google-services.example.json app\google-services.json
```
*(If omitted, Gradle copies `app/google-services.example.json` to `app/google-services.json` automatically for offline debug builds.)*

### 2. Build Debug APK
```powershell
.\gradlew.bat :app:assembleDebug
```

### 3. Build Signed Release Bundle (`.aab`) for Google Play
Set the release signing environment variables before running Gradle (never commit `sign.jks` or passwords):
```powershell
$env:SIGNING_STORE_PASSWORD = "<store-password>"
$env:SIGNING_KEY_ALIAS      = "<key-alias>"
$env:SIGNING_KEY_PASSWORD   = "<key-password>"
.\gradlew.bat :app:bundleRelease
```
