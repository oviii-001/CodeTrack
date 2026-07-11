package com.ovi.codetrack.shared.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey val id: String,
    val problemId: Int,
    val problemName: String,
    val difficulty: String,
    val tags: String,
    val timeTakenMinutes: Int,
    val timestamp: Long,
    val notes: String,
    val isSynced: Boolean
)
