package com.ovi.codetrack

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ovi.codetrack.shared.presentation.navigation.AddSubmissionRoute
import com.ovi.codetrack.shared.presentation.navigation.DashboardRoute
import com.ovi.codetrack.shared.presentation.navigation.LoginRoute
import com.ovi.codetrack.shared.presentation.navigation.MainRoute
import com.ovi.codetrack.shared.presentation.screens.AddSubmissionScreen
import com.ovi.codetrack.shared.presentation.screens.DashboardScreen
import com.ovi.codetrack.shared.presentation.screens.LoginScreen
import com.ovi.codetrack.shared.presentation.screens.MainScreen
import com.ovi.codetrack.shared.presentation.theme.CodeTrackTheme
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@Composable
fun App() {
    CodeTrackTheme {
        val navController = rememberNavController()
        val currentUser = Firebase.auth.currentUser
        
        val startDestination: Any = if (currentUser != null) {
            MainRoute
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
            
            composable<MainRoute> {
                MainScreen(
                    onNavigateToAdd = { problemId, problemName, difficulty, tags ->
                        navController.navigate(AddSubmissionRoute(problemId, problemName, difficulty, tags))
                    },
                    onLogout = {
                        navController.navigate(LoginRoute) {
                            popUpTo(MainRoute) { inclusive = true }
                        }
                    }
                )
            }
            
            composable<AddSubmissionRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<AddSubmissionRoute>()
                AddSubmissionScreen(
                    initialProblemId = args.problemId ?: "",
                    initialProblemName = args.problemName ?: "",
                    initialDifficulty = args.difficulty ?: "Easy",
                    initialTags = args.tags ?: "",
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}