package com.ovi.codetrack.shared.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object AndroidDatabaseContext : KoinComponent {
    val context: Context by inject()
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<CodeTrackDatabase> {
    val context = AndroidDatabaseContext.context
    val dbFile = context.getDatabasePath("codetrack.db")
    return Room.databaseBuilder<CodeTrackDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
