package com.ovi.codetrack

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ovi.codetrack.shared.presentation.navigation.AddSubmissionRoute
import com.ovi.codetrack.shared.presentation.navigation.DashboardRoute
import com.ovi.codetrack.shared.presentation.navigation.LoginRoute
import com.ovi.codetrack.shared.presentation.screens.AddSubmissionScreen
import com.ovi.codetrack.shared.presentation.screens.DashboardScreen
import com.ovi.codetrack.shared.presentation.screens.LoginScreen
import com.ovi.codetrack.shared.presentation.theme.CodeTrackTheme
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@Composable
fun App() {
    CodeTrackTheme {
        val navController = rememberNavController()
        val currentUser = Firebase.auth.currentUser
        
        val startDestination: Any = if (currentUser != null) {
            DashboardRoute
        } else {
            LoginRoute
        }

        NavHost(navController = navController, startDestination = startDestination) {
            composable<LoginRoute> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(DashboardRoute) {
                            popUpTo(LoginRoute) { inclusive = true }
                        }
                    }
                )
            }
            
            composable<DashboardRoute> {
                DashboardScreen(
                    onNavigateToAdd = {
                        navController.navigate(AddSubmissionRoute)
                    },
                    onLogout = {
                        navController.navigate(LoginRoute) {
                            popUpTo(DashboardRoute) { inclusive = true }
                        }
                    }
                )
            }
            
            composable<AddSubmissionRoute> {
                AddSubmissionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}