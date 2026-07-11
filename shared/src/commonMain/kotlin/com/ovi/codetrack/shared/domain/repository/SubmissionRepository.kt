package com.ovi.codetrack.shared.domain.repository

import com.ovi.codetrack.shared.domain.model.Submission
import com.ovi.codetrack.shared.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface SubmissionRepository {
    
    /**
     * Observes all submissions for the current user from the local Room database,
     * which acts as the Single Source of Truth (SSOT).
     */
    fun observeSubmissions(): Flow<List<Submission>>
    
    /**
     * Calculates and observes user statistics based on the local database.
     */
    fun observeUserStats(): Flow<UserStats>

    /**
     * Adds a new submission. It saves to the local Room database first (offline-first),
     * and queues a sync to Firestore.
     */
    suspend fun addSubmission(submission: Submission): Result<Unit>
    
    /**
     * Deletes a submission locally and remotely.
     */
    suspend fun deleteSubmission(submissionId: String): Result<Unit>
    
    /**
     * Initiates a real-time snapshot listener on Firestore for the current user's submissions
     * to keep the local Room database synced.
     */
    fun startRealtimeSync()
    
    /**
     * Stops the real-time sync.
     */
    fun stopRealtimeSync()
}
