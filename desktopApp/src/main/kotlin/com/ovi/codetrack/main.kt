package com.ovi.codetrack

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import com.ovi.codetrack.shared.di.initKoin
import org.koin.dsl.module

fun main() {
    initKoin(listOf(module {}))
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "CodeTrack",
        ) {
            App()
        }
    }
}