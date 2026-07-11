package com.ovi.codetrack.shared.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<CodeTrackDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "codetrack.db")
    return Room.databaseBuilder<CodeTrackDatabase>(
        name = dbFile.absolutePath
    )
}
