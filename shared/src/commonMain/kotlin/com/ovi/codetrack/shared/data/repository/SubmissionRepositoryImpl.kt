package com.ovi.codetrack.shared.data.repository

import com.ovi.codetrack.shared.data.local.SubmissionDao
import com.ovi.codetrack.shared.data.local.SubmissionEntity
import com.ovi.codetrack.shared.data.remote.SubmissionDto
import com.ovi.codetrack.shared.domain.model.Submission
import com.ovi.codetrack.shared.domain.model.UserStats
import com.ovi.codetrack.shared.domain.repository.SubmissionRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubmissionRepositoryImpl(
    private val dao: SubmissionDao
) : SubmissionRepository {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val scope = CoroutineScope(Dispatchers.Default)
    private var syncJob: Job? = null

    private fun getUserId(): String? = auth.currentUser?.uid

    override fun observeSubmissions(): Flow<List<Submission>> {
        return dao.observeAllSubmissions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeUserStats(): Flow<UserStats> {
        return dao.observeAllSubmissions().map { entities ->
            val total = entities.size
            val easy = entities.count { it.difficulty.equals("Easy", ignoreCase = true) }
            val medium = entities.count { it.difficulty.equals("Medium", ignoreCase = true) }
            val hard = entities.count { it.difficulty.equals("Hard", ignoreCase = true) }
            UserStats(total, easy, medium, hard)
        }
    }

    override suspend fun addSubmission(submission: Submission): Result<Unit> {
        return try {
            // Offline-first: save locally
            val entity = submission.toEntity(isSynced = false)
            dao.insertSubmission(entity)
            
            // Trigger sync immediately if network is available
            syncPendingSubmissions()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSubmission(submissionId: String): Result<Unit> {
        return try {
            dao.deleteSubmission(submissionId)
            
            val userId = getUserId() ?: return Result.success(Unit)
            firestore.collection("users").document(userId)
                .collection("submissions").document(submissionId)
                .delete()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun startRealtimeSync() {
        val userId = getUserId() ?: return
        
        syncJob?.cancel()
        syncJob = scope.launch {
            try {
                // Initial sync from local to remote for any unsynced items
                syncPendingSubmissions()
                
                // Real-time listener for remote changes
                firestore.collection("users").document(userId)
                    .collection("submissions")
                    .snapshots
                    .collect { snapshot ->
                        val remoteSubmissions = snapshot.documents.map { doc ->
                            doc.data(SubmissionDto.serializer())
                        }
                        
                        // Upsert into local database
                        val entities = remoteSubmissions.map { it.toEntity(isSynced = true) }
                        dao.insertSubmissions(entities)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun stopRealtimeSync() {
        syncJob?.cancel()
    }
    
    private suspend fun syncPendingSubmissions() {
        val userId = getUserId() ?: return
        val unsynced = dao.getUnsyncedSubmissions()
        
        for (entity in unsynced) {
            try {
                val dto = entity.toDto()
                firestore.collection("users").document(userId)
                    .collection("submissions").document(dto.id)
                    .set(SubmissionDto.serializer(), dto)
                dao.markAsSynced(entity.id)
            } catch (e: Exception) {
                // Ignore and try again later
            }
        }
    }

    // Mappers
    private fun SubmissionEntity.toDomain() = Submission(
        id = id,
        problemId = problemId,
        problemName = problemName,
        difficulty = difficulty,
        tags = tags.split(",").filter { it.isNotBlank() },
        timeTakenMinutes = timeTakenMinutes,
        timestamp = timestamp,
        timeComplexity = timeComplexity,
        spaceComplexity = spaceComplexity,
        notes = notes
    )

    private fun Submission.toEntity(isSynced: Boolean) = SubmissionEntity(
        id = id,
        problemId = problemId,
        problemName = problemName,
        difficulty = difficulty,
        tags = tags.joinToString(","),
        timeTakenMinutes = timeTakenMinutes,
        timestamp = timestamp,
        timeComplexity = timeComplexity,
        spaceComplexity = spaceComplexity,
        notes = notes,
        isSynced = isSynced
    )
    
    private fun SubmissionEntity.toDto() = SubmissionDto(
        id = id,
        problemId = problemId,
        problemName = problemName,
        difficulty = difficulty,
        tags = tags.split(",").filter { it.isNotBlank() },
        timeTakenMinutes = timeTakenMinutes,
        timestamp = timestamp,
        timeComplexity = timeComplexity,
        spaceComplexity = spaceComplexity,
        notes = notes
    )
    
    private fun SubmissionDto.toEntity(isSynced: Boolean) = SubmissionEntity(
        id = id,
        problemId = problemId,
        problemName = problemName,
        difficulty = difficulty,
        tags = tags.joinToString(","),
        timeTakenMinutes = timeTakenMinutes,
        timestamp = timestamp,
        timeComplexity = timeComplexity,
        spaceComplexity = spaceComplexity,
        notes = notes,
        isSynced = isSynced
    )
}
