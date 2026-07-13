package com.ovi.codetrack.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ovi.codetrack.shared.domain.model.ProblemRoadmap
import com.ovi.codetrack.shared.domain.model.RoadmapProblem
import com.ovi.codetrack.shared.domain.repository.SubmissionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RoadmapUiState(
    val problems: List<RoadmapProblem> = ProblemRoadmap.problems,
    val solvedProblemIds: Set<String> = emptySet()
)

class RoadmapViewModel(
    private val repository: SubmissionRepository
) : ViewModel() {

    val uiState: StateFlow<RoadmapUiState> = repository.observeSubmissions()
        .map { submissions ->
            val solvedIds = submissions.map { it.problemId.toString() }.toSet()
            RoadmapUiState(solvedProblemIds = solvedIds)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RoadmapUiState()
        )
}
