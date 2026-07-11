package com.ovi.codetrack

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform