package com.ovi.codetrack.shared.domain.auth

interface AuthManager {
    suspend fun signInWithGoogle(): Result<String> // Returns the ID token on success
}
