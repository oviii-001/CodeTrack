package com.ovi.codetrack.shared.di

import com.ovi.codetrack.shared.data.local.CodeTrackDatabase
import com.ovi.codetrack.shared.data.repository.SubmissionRepositoryImpl
import com.ovi.codetrack.shared.domain.repository.SubmissionRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import com.ovi.codetrack.shared.presentation.viewmodels.LoginViewModel
import com.ovi.codetrack.shared.presentation.viewmodels.DashboardViewModel
import com.ovi.codetrack.shared.presentation.viewmodels.AddSubmissionViewModel
import org.koin.core.module.dsl.viewModel
import com.ovi.codetrack.shared.data.local.getDatabaseBuilder

import com.ovi.codetrack.shared.presentation.viewmodels.RoadmapViewModel
import com.ovi.codetrack.shared.presentation.viewmodels.HistoryViewModel

val commonModule = module {
    single<SubmissionRepository> { SubmissionRepositoryImpl(get()) }
    single<CodeTrackDatabase> { 
        getDatabaseBuilder()
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    single { get<CodeTrackDatabase>().submissionDao() }
    
    viewModel { LoginViewModel() }
    viewModel { DashboardViewModel(get()) }
    viewModel { AddSubmissionViewModel(get()) }
    viewModel { RoadmapViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
}

fun initKoin(platformModules: List<Module>) {
    startKoin {
        modules(commonModule + platformModules)
    }
}
