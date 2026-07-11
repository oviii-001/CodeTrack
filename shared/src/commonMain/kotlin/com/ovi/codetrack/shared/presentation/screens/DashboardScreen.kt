package com.ovi.codetrack.shared.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import codetrack.shared.generated.resources.*
import codetrack.shared.generated.resources.Res
import codetrack.shared.generated.resources.app_name
import codetrack.shared.generated.resources.dashboard_title
import codetrack.shared.generated.resources.easy
import codetrack.shared.generated.resources.hard
import codetrack.shared.generated.resources.medium
import codetrack.shared.generated.resources.recent_submissions
import codetrack.shared.generated.resources.total_solved
import com.ovi.codetrack.shared.presentation.model.Difficulty
import com.ovi.codetrack.shared.presentation.model.SubmissionUiModel
import com.ovi.codetrack.shared.presentation.model.UserStatsUiModel
import com.ovi.codetrack.shared.presentation.theme.EasyColor
import com.ovi.codetrack.shared.presentation.theme.HardColor
import com.ovi.codetrack.shared.presentation.theme.MediumColor
import com.ovi.codetrack.shared.presentation.viewmodels.DashboardViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAdd: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.stats?.let { stats ->
                        StatsCard(stats)
                    }
                }
                
                item {
                    Text(
                        text = stringResource(Res.string.recent_submissions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.recentSubmissions) { submission ->
                    SubmissionItem(submission)
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun StatsCard(stats: UserStatsUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.total_solved),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stats.totalSolved,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(stringResource(Res.string.easy), stats.easySolved, EasyColor)
                StatItem(stringResource(Res.string.medium), stats.mediumSolved, MediumColor)
                StatItem(stringResource(Res.string.hard), stats.hardSolved, HardColor)
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = color)
        Text(text = count, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SubmissionItem(submission: SubmissionUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = submission.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val color = when(submission.difficulty) {
                        Difficulty.EASY -> EasyColor
                        Difficulty.MEDIUM -> MediumColor
                        Difficulty.HARD -> HardColor
                        Difficulty.UNKNOWN -> MaterialTheme.colorScheme.onSurface
                    }
                    Text(
                        text = submission.difficulty.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = submission.formattedTimeTaken,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = submission.formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
