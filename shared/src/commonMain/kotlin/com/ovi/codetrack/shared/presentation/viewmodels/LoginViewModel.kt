package com.ovi.codetrack.shared.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(error = "Email and password cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, pass)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                // Try creating an account if sign in fails (for simplicity in this prototype)
                try {
                    auth.createUserWithEmailAndPassword(email, pass)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                } catch (e2: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = e2.message ?: "Authentication failed") }
                }
            }
        }
    }
}
