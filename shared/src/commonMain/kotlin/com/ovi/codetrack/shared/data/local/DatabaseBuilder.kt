package com.ovi.codetrack.shared.data.local

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<CodeTrackDatabase>
