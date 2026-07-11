package com.ovi.codetrack.shared.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionDao {
    @Query("SELECT * FROM submissions ORDER BY timestamp DESC")
    fun observeAllSubmissions(): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions ORDER BY timestamp DESC")
    suspend fun getAllSubmissions(): List<SubmissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SubmissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<SubmissionEntity>)

    @Query("DELETE FROM submissions WHERE id = :id")
    suspend fun deleteSubmission(id: String)
    
    @Query("SELECT * FROM submissions WHERE isSynced = 0")
    suspend fun getUnsyncedSubmissions(): List<SubmissionEntity>
    
    @Query("UPDATE submissions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    @Query("DELETE FROM submissions")
    suspend fun clearAll()
}
