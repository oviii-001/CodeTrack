# Project Specification: LeetCode Tracker (CMP)

## 1. Executive Summary
**Project Name:** CodeTrack (or similar)
**Platform Targets:** Android (Mobile), JVM (Windows PC)
**Objective:** A cross-platform application to track, analyze, and visualize daily LeetCode problem-solving progress.
**Architecture:** Clean Architecture + MVVM 

## 2. Technology Stack (2026 Standards)
* **UI Framework:** Jetpack Compose Multiplatform (Material 3).
* **Architecture Pattern:** Clean Architecture (Presentation, Domain, Data) with Unidirectional Data Flow (UDF).
* **Dependency Injection:** **Koin** (Standard for KMP, replacing Hilt).
* **Local Database (Caching):** **Room KMP** (Yes, Room now supports KMP!).
* **Preferences/Settings:** **DataStore KMP**.
* **Remote Backend:** **Firebase** via `gitlive-firebase-kotlin-sdk` (Firestore for DB, Firebase Auth for login).
* **Asynchronous Processing:** Kotlin Coroutines & StateFlow.

## 3. Modular Source Set Structure
In a standard Compose Multiplatform project, your code will be divided into specific source sets. Here is how we will structure the separation of concerns:

* **`commonMain` (90% of the code):**
    * **UI Layer:** Compose UI screens, ViewModels (using `lifecycle-viewmodel-compose` KMP), UI states.
    * **Domain Layer:** Use cases (e.g., `LogProblemUseCase`, `GetWeeklyStatsUseCase`), Models.
    * **Data Layer:** Room Database setup, Repository interfaces, GitLive Firebase integration.
* **`androidMain` (5% of the code):**
    * Android-specific initialization (e.g., Koin Android context).
    * Platform-specific Firebase initialization.
    * `MainActivity.kt`.
* **`desktopMain` (5% of the code):**
    * JVM window configuration (window size, title, desktop icon).
    * Desktop-specific Firebase REST fallbacks (if needed for Auth).

## 4. Data & Firebase Strategy

### Database Schema (Firestore)
We will use a NoSQL document structure in Firestore optimized for quick reads.
* **Collection: `users`**
    * `Document: {userId}` -> Fields: `email`, `joinDate`, `totalSolved`
* **Collection: `submissions`** (Sub-collection under `users/{userId}`)
    * `Document: {submissionId}` -> Fields: 
        * `problemId` (Int)
        * `problemName` (String)
        * `difficulty` (String: Easy, Medium, Hard)
        * `topicTags` (List<String>: Arrays, DP, Graphs)
        * `timeTakenMinutes` (Int)
        * `timestamp` (Long/Date)
        * `notes` (String)

### Repository Pattern (Offline-First)
To make the app snappy on both your PC and phone, we will implement an **Offline-First strategy**:
1.  **Read:** The UI observes a `Flow` from the local **Room Database**.
2.  **Sync:** When the app opens (or a swipe-to-refresh is triggered), it fetches new records from **Firestore** and inserts them into Room.
3.  **Write:** When you log a new LeetCode problem, the Repository writes it to Room *first*, and then queues a sync to Firestore.

## 5. Implementation Roadmap (Phases)

### Phase 1: Foundation & Auth
* Setup the KMP project using the JetBrains wizard.
* Configure the GitLive Firebase SDK and Koin for dependency injection.
* Implement Firebase Auth. *Senior Tip: For Desktop JVM, standard Google Sign-In is complex. Use Email/Password auth to save time, or use the REST API approach for desktop OAuth.*

### Phase 2: Core Data Layer
* Set up Room KMP (`@Database`, `@Dao`, `@Entity`).
* Create the `SubmissionRepository` to handle the logic between Room and Firestore.
* Write unit tests for the repository logic using mock data.

### Phase 3: Domain & ViewModels
* Create the `ViewModel` using StateFlow. 
* **Example State:**
    ```kotlin
    data class HomeUiState(
        val isLoading: Boolean = false,
        val todaySolvedCount: Int = 0,
        val recentSubmissions: List<Submission> = emptyList(),
        val error: String? = null
    )
    ```

### Phase 4: UI & Desktop Polish (Compose Multiplatform)
* **Dashboard Screen:** A summary of today's progress, a heat map (similar to GitHub contributions), and pie charts for Easy/Medium/Hard breakdowns.
* **Add Submission Screen:** A form to log the problem name, URL, difficulty, time spent, and lessons learned.
* **Responsive Design:** Use `BoxWithConstraints` to render a navigation rail on the Windows desktop app, and a bottom navigation bar on the Android app.
