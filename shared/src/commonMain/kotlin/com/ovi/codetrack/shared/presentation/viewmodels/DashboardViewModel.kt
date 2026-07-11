package com.ovi.codetrack.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val error: String? = null
)

class DashboardViewModel(
    private val repository: SubmissionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeUserStats(),
        repository.observeSubmissions()
    ) { stats, submissions ->
        DashboardUiState(
            isLoading = false,
            stats = stats.toUiModel(),
            recentSubmissions = submissions.map { it.toUiModel() }
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
