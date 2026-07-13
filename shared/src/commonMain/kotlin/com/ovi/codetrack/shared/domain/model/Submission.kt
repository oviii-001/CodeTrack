package com.ovi.codetrack.shared.domain.model

data class Submission(
    val id: String,
    val problemId: Int,
    val problemName: String,
    val difficulty: String,
    val tags: List<String>,
    val timeTakenMinutes: Int,
    val timestamp: Long,
    val timeComplexity: String? = null,
    val spaceComplexity: String? = null,
    val notes: String
)
