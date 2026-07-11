package com.ovi.codetrack.shared.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SubmissionDto(
    val id: String,
    val problemId: Int,
    val problemName: String,
    val difficulty: String,
    val tags: List<String>,
    val timeTakenMinutes: Int,
    val timestamp: Long,
    val notes: String
)
