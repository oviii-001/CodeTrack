package com.ovi.codetrack.shared.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SubmissionEntity::class], version = 1)
abstract class CodeTrackDatabase : RoomDatabase() {
    abstract fun submissionDao(): SubmissionDao
}
