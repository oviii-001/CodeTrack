package com.ovi.codetrack.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovi.codetrack.shared.domain.repository.SubmissionRepository
import com.ovi.codetrack.shared.presentation.model.SubmissionUiModel
import com.ovi.codetrack.shared.presentation.model.toUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val isLoading: Boolean = true,
    val submissions: List<SubmissionUiModel> = emptyList()
)

class HistoryViewModel(
    private val repository: SubmissionRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = repository.observeSubmissions()
        .map { submissions ->
            HistoryUiState(
                isLoading = false,
                submissions = submissions.map { it.toUiModel() }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState(isLoading = true)
        )
}
