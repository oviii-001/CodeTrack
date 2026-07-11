# Software Requirements Specification (SRS)
**Project Name:** CodeTrack (Compose Multiplatform LeetCode Tracker)
**Version:** 1.0
**Target Platforms:** Android (Mobile) & Windows (JVM/Desktop)

---

## 1. Introduction
### 1.1 Purpose
This document specifies the software requirements for **CodeTrack**, a cross-platform application designed to help software engineering students and developers track their daily LeetCode problem-solving progress. This document is intended for developers, AI coding assistants (e.g., Google Antigravity), and academic evaluators.

### 1.2 Scope
CodeTrack is a Compose Multiplatform (CMP) application that allows users to log completed algorithmic problems, view progress statistics (heatmaps, difficulty breakdowns), and synchronize data across mobile (Android) and desktop (Windows). It employs an offline-first architecture to ensure a seamless experience regardless of internet connectivity.

---

## 2. Overall Description
### 2.1 Product Perspective
The system acts as a personal tracking dashboard. It operates independently on the user's devices but relies on Firebase (Firestore & Authentication) as a centralized cloud backend for cross-device synchronization.

### 2.2 Operating Environment
* **Mobile Environment:** Android 8.0 (API Level 26) and above.
* **Desktop Environment:** Windows 10/11 running Java Virtual Machine (JVM 17+).
* **Cloud Environment:** Firebase (Firestore, Authentication).

### 2.3 User Classes
* **Primary User:** Software Engineering students preparing for Big Tech interviews who need a fast, distraction-free way to log their daily coding practice and visualize their consistency.

---

## 3. Functional Requirements (FR)

### FR-1: User Authentication
* **FR-1.1:** The system shall allow users to register and log in using an Email and Password combination (via Firebase Authentication).
* **FR-1.2:** The system shall maintain the user's session securely across app restarts.
* **FR-1.3:** The system shall allow users to log out safely, clearing local user-specific cache.

### FR-2: Dashboard and Analytics
* **FR-2.1:** The system shall display the user's total solved problems, broken down by difficulty (Easy, Medium, Hard).
* **FR-2.2:** The system shall display a list of the 10 most recent problem submissions.
* **FR-2.3:** The system shall display a daily consistency tracker (e.g., a heatmap or streak counter) based on submission timestamps.

### FR-3: Submission Logging
* **FR-3.1:** The system shall allow users to add a new submission record with the following fields: Problem ID, Problem Name, Difficulty, Topic Tags (e.g., DP, Sliding Window), Time Taken (minutes), and Notes.
* **FR-3.2:** The system shall validate inputs (e.g., Problem ID must be numeric, Problem Name cannot be empty) before saving.
* **FR-3.3:** The system shall allow users to view, edit, or delete their past submissions.

### FR-4: Offline-First Synchronization Strategy
* **FR-4.1 (Local Write):** When a user adds a submission, the system shall immediately save it to the local Room Database KMP.
* **FR-4.2 (Cloud Sync):** The system shall attempt to synchronize local additions/edits with Firestore in the background via the GitLive Firebase SDK.
* **FR-4.3 (Local Read):** The system shall always read dashboard and submission data from the local Room database to ensure immediate UI updates.

---

## 5. Non-Functional Requirements (NFR)

### NFR-1: Performance & Responsiveness
* **NFR-1.1:** The dashboard screen must render within 1.5 seconds on both Android and Windows platforms upon launch.
* **NFR-1.2:** UI state changes (e.g., navigating from the Dashboard to the Add Submission screen) must execute smoothly at 60 FPS without jank.

### NFR-2: Reliability & Availability
* **NFR-2.1 (Offline Support):** The application must provide 100% core functionality (viewing stats, adding logs) without an active internet connection.
* **NFR-2.2:** Data conflicts between the local database and Firestore shall favor the most recent timestamp.

### NFR-3: Usability
* **NFR-3.1:** The UI shall strictly follow Material Design 3 guidelines.
* **NFR-3.2 (Responsive Design):** The desktop application shall utilize a Navigation Rail on the left side, while the Android application shall utilize a Bottom Navigation Bar.

### NFR-4: Portability & Maintainability
* **NFR-4.1:** At least 85% of the codebase (UI, Domain, Data logic) must reside in the `commonMain` module.
* **NFR-4.2:** Dependency Injection shall be implemented using Koin to abstract platform-specific dependencies (like database drivers).

---

## 6. System Architecture (Clean Architecture)

### 6.1 Presentation Layer (`commonMain`)
* **UI:** Jetpack Compose Multiplatform.
* **State Management:** ViewModels (`lifecycle-viewmodel-compose` KMP) exposing `StateFlow`. Unidirectional Data Flow (UDF) is mandatory.

### 6.2 Domain Layer (`commonMain`)
* **Entities:** Pure Kotlin data classes (e.g., `Submission`, `UserStats`).
* **Use Cases:** Classes handling business logic (e.g., `CalculateStreakUseCase`, `SyncSubmissionsUseCase`).
* **Repository Interfaces:** Definitions for data operations.

### 6.3 Data Layer
* **Local Data Source:** Room Database (KMP compatible) configured in `commonMain` with actual driver instantiations in `androidMain` and `desktopMain`.
* **Remote Data Source:** GitLive Firebase Kotlin SDK interacting with Firestore REST/Native APIs.

---

## 7. Database Schema Reference

### 7.1 Room Entity (Local)
```kotlin
@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey val id: String, // UUID mapped to Firestore Document ID
    val problemId: Int,
    val problemName: String,
    val difficulty: String,
    val tags: String, // Comma separated
    val timeTaken: Int,
    val timestamp: Long,
    val notes: String,
    val isSynced: Boolean // Used for background sync queue
)
```

### 7.2 Firestore Document (Remote)
* **Path:** `users/{user_email}/submissions/{submission_id}`
* **Fields:** Match `SubmissionEntity` (excluding `isSynced`).
