# 💎 ExpensePulse — Enterprise Native Android Expense Tracker

[![Build & Release APK](https://github.com/Sakthisabariesh/Tracker/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Sakthisabariesh/Tracker/actions/workflows/build-apk.yml)
[![Direct Download](https://img.shields.io/badge/Download-Android%20APK-00E676?style=for-the-badge&logo=android&logoColor=black)](https://github.com/Sakthisabariesh/Tracker/releases)

A modern, high-performance, offline-first personal finance application built with **Jetpack Compose**, **Material 3**, and **Clean Architecture**.

---

## 📥 Direct APK Download

You can download and install the latest APK directly onto your Android device:

👉 **[Download Latest APK from GitHub Releases](https://github.com/Sakthisabariesh/Tracker/releases)**

1. Tap on the latest release at the link above.
2. Under **Assets**, click **`ExpensePulse.apk`**.
3. Open the downloaded file on your Android phone and tap **Install**.

---

## ✨ Features & Architecture

- **💎 Enterprise Visual Design**: Obsidian dark mode with custom geometric emerald branding and titanium typography.
- **⚡ Dual Income & Expense Tracking (+ / -)**: Track both income (Salary, Freelance, Investments) and expenses (Food, Travel, Bills, Shopping) with automatic Net Savings calculation.
- **📊 7-Day Spending Activity Trend**: Pure Compose Canvas 120Hz bar chart with interactive floating tooltips and weekend distinction.
- **📅 Spending Heatmap Calendar**: Monthly heatmap calendar with spend intensity dots and single-tap daily itemized inspector.
- **⚡ < 3-Second Quick Log**: Custom oversized numeric keypad bottom sheet with instant category and date chips.
- **⚙️ Dynamic Budget Caps**: In-place customizable monthly budget cap and daily spending limit with live recalculated progress rings.
- **🔒 Offline-First Local Storage**: Room database persistence with reactive StateFlow streams and one-tap CSV data export.

---

## 🛠️ Tech Stack

- **UI Framework**: Jetpack Compose & Material 3
- **Language**: Kotlin 2.0.0
- **Database**: SQLite / Room 2.6.1 with Coroutines & StateFlow
- **Architecture**: MVVM + Clean Architecture
- **CI/CD**: GitHub Actions automated APK build and release pipeline
