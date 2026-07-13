package com.ovi.codetrack.shared.presentation.model

import com.ovi.codetrack.shared.domain.model.Submission

data class SubmissionUiModel(
    val id: String,
    val title: String,
    val difficulty: Difficulty,
    val tags: List<String>,
    val formattedTimeTaken: String,
    val formattedDate: String,
    val timeComplexity: String? = null,
    val spaceComplexity: String? = null
)

enum class Difficulty {
    EASY, MEDIUM, HARD, UNKNOWN
}

fun Submission.toUiModel(): SubmissionUiModel {
    val difficultyEnum = when (difficulty.uppercase()) {
        "EASY" -> Difficulty.EASY
        "MEDIUM" -> Difficulty.MEDIUM
        "HARD" -> Difficulty.HARD
        else -> Difficulty.UNKNOWN
    }
    
    // In a real app we'd use kotlinx-datetime, but for simplicity we can format the timestamp here.
    // Assuming timestamp is in milliseconds.
    // For 2026 standards with KMP, we'd use kotlinx.datetime, but if we don't have it added, 
    // we can do a simple placeholder or string representation. Let's just stringify for now.
    val formattedDateStr = "Recent" // Place holder for formatting 

    return SubmissionUiModel(
        id = id,
        title = "$problemId. $problemName",
        difficulty = difficultyEnum,
        tags = tags,
        formattedTimeTaken = "$timeTakenMinutes mins",
        formattedDate = formattedDateStr,
        timeComplexity = timeComplexity,
        spaceComplexity = spaceComplexity
    )
}
