package com.ovi.codetrack

import android.app.Application
import com.ovi.codetrack.shared.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class CodeTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        
        initKoin(
            listOf(
                module {
                    single<android.content.Context> { this@CodeTrackApplication }
                }
            )
        )
    }
}
