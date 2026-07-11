# CodeTrack 🚀

A robust, cross-platform application built with **Compose Multiplatform (CMP)** to help developers track their daily algorithm problem-solving consistency. Targeting both **Android** and **Windows (JVM)**, this project serves as a comprehensive dashboard for logging submissions, visualizing progress, and analyzing difficulty metrics over time.

### 🏗️ Why this project?
Consistent practice is the key to mastering data structures and algorithms. I built this tool to scratch my own itch: maintaining a daily streak for interview preparation while ensuring my data is seamlessly synced across my phone and desktop development environment. 

### ✨ Key Technical Highlights
* **Cross-Platform UI:** 100% shared UI using Jetpack Compose Multiplatform (Material 3).
* **Offline-First Architecture:** Instant UI rendering powered by **Room KMP** as the single source of truth.
* **Cloud Synchronization:** Background syncing to **Firebase** (via GitLive Kotlin SDK).
* **Clean Architecture:** Strict separation of Presentation, Domain, and Data layers with Unidirectional Data Flow (UDF).
* **Dependency Injection:** Fully modular DI using **Koin**.
