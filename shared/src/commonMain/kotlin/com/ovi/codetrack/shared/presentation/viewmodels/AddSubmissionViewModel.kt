package com.ovi.codetrack.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovi.codetrack.shared.data.remote.LeetCodeApiService
import com.ovi.codetrack.shared.data.remote.LeetCodeProblem
import com.ovi.codetrack.shared.domain.model.ProblemRoadmap
import com.ovi.codetrack.shared.domain.model.Submission
import com.ovi.codetrack.shared.domain.repository.SubmissionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddSubmissionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    // Problem lookup state
    val isLookingUp: Boolean = false,
    val lookedUpProblem: LeetCodeProblem? = null,
    val lookupError: String? = null,
    // Whether the problem came pre-filled from roadmap
    val isPreFilled: Boolean = false
)

class AddSubmissionViewModel(
    private val repository: SubmissionRepository,
    private val leetCodeApi: LeetCodeApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSubmissionUiState())
    val uiState: StateFlow<AddSubmissionUiState> = _uiState.asStateFlow()
    
    private var lookupJob: Job? = null

    /**
     * Called when screen opens with pre-filled data from roadmap.
     * Looks up the problem in the local roadmap to get tags.
     */
    fun preFilFromRoadmap(problemId: String, problemName: String, difficulty: String) {
        val roadmapProblem = ProblemRoadmap.problems.firstOrNull { it.id.toString() == problemId }
        _uiState.update {
            it.copy(
                isPreFilled = true,
                lookedUpProblem = LeetCodeProblem(
                    questionId = problemId,
                    title = problemName,
                    titleSlug = problemName.lowercase().replace(" ", "-"),
                    difficulty = difficulty,
                    topicTags = roadmapProblem?.tags ?: emptyList()
                )
            )
        }
    }

    /**
     * Debounced problem lookup: first checks roadmap, then calls LeetCode API.
     */
    fun lookupProblem(query: String) {
        lookupJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(lookedUpProblem = null, lookupError = null, isLookingUp = false) }
            return
        }

        lookupJob = viewModelScope.launch {
            delay(600) // debounce 600ms

            _uiState.update { it.copy(isLookingUp = true, lookupError = null) }

            // 1. Try local roadmap first
            val problemId = query.toIntOrNull()
            val roadmapMatch = if (problemId != null) {
                ProblemRoadmap.problems.firstOrNull { it.id == problemId }
            } else {
                ProblemRoadmap.problems.firstOrNull { 
                    it.title.contains(query, ignoreCase = true)
                }
            }

            if (roadmapMatch != null) {
                _uiState.update {
                    it.copy(
                        isLookingUp = false,
                        lookedUpProblem = LeetCodeProblem(
                            questionId = roadmapMatch.id.toString(),
                            title = roadmapMatch.title,
                            titleSlug = roadmapMatch.title.lowercase().replace(" ", "-"),
                            difficulty = roadmapMatch.difficulty.name,
                            topicTags = roadmapMatch.tags
                        )
                    )
                }
                return@launch
            }

            // 2. Call LeetCode API
            if (problemId != null) {
                // Search by slug derived from the number - try direct slug lookup
                val result = leetCodeApi.fetchProblemBySlug(query)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLookingUp = false, lookedUpProblem = result.getOrNull()) }
                } else {
                    // Could not find - show a gentle message
                    _uiState.update {
                        it.copy(
                            isLookingUp = false,
                            lookedUpProblem = null,
                            lookupError = "Problem not found. You can enter details manually."
                        )
                    }
                }
            } else {
                // Search by title slug
                val slug = query.trim().lowercase().replace(" ", "-")
                val result = leetCodeApi.fetchProblemBySlug(slug)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLookingUp = false, lookedUpProblem = result.getOrNull()) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLookingUp = false,
                            lookedUpProblem = null,
                            lookupError = "Problem not found. You can enter details manually."
                        )
                    }
                }
            }
        }
    }

    fun clearLookup() {
        _uiState.update { it.copy(lookedUpProblem = null, lookupError = null, isPreFilled = false) }
    }

    fun addSubmission(
        problemIdStr: String,
        problemName: String,
        difficulty: String,
        tagsStr: String,
        timeTakenStr: String,
        timeComplexityStr: String,
        spaceComplexityStr: String,
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
            timeComplexity = timeComplexityStr.takeIf { it.isNotBlank() },
            spaceComplexity = spaceComplexityStr.takeIf { it.isNotBlank() },
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
