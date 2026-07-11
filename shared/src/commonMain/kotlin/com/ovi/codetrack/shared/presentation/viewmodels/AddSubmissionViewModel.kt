package com.ovi.codetrack.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovi.codetrack.shared.domain.model.Submission
import com.ovi.codetrack.shared.domain.repository.SubmissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddSubmissionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AddSubmissionViewModel(
    private val repository: SubmissionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSubmissionUiState())
    val uiState: StateFlow<AddSubmissionUiState> = _uiState.asStateFlow()

    fun addSubmission(
        problemIdStr: String,
        problemName: String,
        difficulty: String,
        tagsStr: String,
        timeTakenStr: String,
        notes: String
    ) {
        val problemId = problemIdStr.toIntOrNull()
        val timeTaken = timeTakenStr.toIntOrNull()

        if (problemId == null || problemName.isBlank() || difficulty.isBlank() || timeTaken == null) {
            _uiState.update { it.copy(error = "Please fill all required fields correctly.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        val submission = Submission(
            id = uuid(),
            problemId = problemId,
            problemName = problemName,
            difficulty = difficulty,
            tags = tagsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            timeTakenMinutes = timeTaken,
            timestamp = System.currentTimeMillis(),
            notes = notes
        )

        viewModelScope.launch {
            val result = repository.addSubmission(submission)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "Failed to save submission") }
            }
        }
    }

    private fun uuid(): String {
        val chars = "0123456789abcdef"
        return (1..32).map { chars.random() }.joinToString("")
    }
}
