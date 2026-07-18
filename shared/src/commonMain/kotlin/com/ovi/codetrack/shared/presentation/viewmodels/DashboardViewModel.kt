package com.ovi.codetrack.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovi.codetrack.shared.domain.model.RoadmapProblem
import com.ovi.codetrack.shared.domain.repository.SubmissionRepository
import com.ovi.codetrack.shared.presentation.model.SubmissionUiModel
import com.ovi.codetrack.shared.presentation.model.UserStatsUiModel
import com.ovi.codetrack.shared.presentation.model.toUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

data class DashboardUiState(
    val isLoading: Boolean = true,
    val stats: UserStatsUiModel? = null,
    val recentSubmissions: List<SubmissionUiModel> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val nextTask: RoadmapProblem? = null,
    val userName: String = "",
    val error: String? = null,
    // Daily target
    val dailyTarget: Int = 3,
    val todaySolvedCount: Int = 0,
    val todayTasks: List<RoadmapProblem> = emptyList()
)

class DashboardViewModel(
    private val repository: SubmissionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeUserStats(),
        repository.observeSubmissions()
    ) { stats, submissions ->
        // Next Task Calculation
        val solvedIds = submissions.map { it.problemId }.toSet()
        val nextTask = com.ovi.codetrack.shared.domain.model.ProblemRoadmap.problems.firstOrNull { it.id !in solvedIds }

        // Streak Calculation
        val TIMEZONE_OFFSET_MS = 6 * 60 * 60 * 1000L // UTC+6 BDT
        val DAY_MS = 24 * 60 * 60 * 1000L
        fun getDayNumber(timestamp: Long) = (timestamp + TIMEZONE_OFFSET_MS) / DAY_MS

        val solvedDays = submissions.map { getDayNumber(it.timestamp) }.toSet().sortedDescending()
        
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var prevDay: Long? = null

        val ascendingDays = solvedDays.reversed()
        for (day in ascendingDays) {
            if (prevDay == null || day == prevDay + 1) {
                tempStreak++
            } else {
                tempStreak = 1
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            prevDay = day
        }

        if (solvedDays.isNotEmpty()) {
            currentStreak = 1
            var lastDay = solvedDays.first()
            for (i in 1 until solvedDays.size) {
                val day = solvedDays[i]
                if (day == lastDay - 1) {
                    currentStreak++
                    lastDay = day
                } else {
                    break
                }
            }
        }

        // ===== Daily Target Calculation =====
        val todayDayNumber = getDayNumber(System.currentTimeMillis())
        val todaySolvedCount = submissions.count { getDayNumber(it.timestamp) == todayDayNumber }
        
        // Get next unsolved problems for today's goals (up to dailyTarget count)
        val dailyTarget = 3
        val todayTasks = com.ovi.codetrack.shared.domain.model.ProblemRoadmap.problems
            .filter { it.id !in solvedIds }
            .take(dailyTarget)

        val currentUser = Firebase.auth.currentUser
        val name = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.substringBefore("@")
            ?: "Developer"

        DashboardUiState(
            isLoading = false,
            stats = stats.toUiModel(),
            recentSubmissions = submissions.map { it.toUiModel() },
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            nextTask = nextTask,
            userName = name,
            dailyTarget = dailyTarget,
            todaySolvedCount = todaySolvedCount,
            todayTasks = todayTasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    init {
        repository.startRealtimeSync()
    }
    
    fun logout() {
        repository.stopRealtimeSync()
        viewModelScope.launch {
            Firebase.auth.signOut()
        }
    }

    override fun onCleared() {
        repository.stopRealtimeSync()
        super.onCleared()
    }
}
