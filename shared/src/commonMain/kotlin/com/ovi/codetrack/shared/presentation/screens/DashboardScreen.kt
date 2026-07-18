package com.ovi.codetrack.shared.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import codetrack.shared.generated.resources.*
import codetrack.shared.generated.resources.Res
import codetrack.shared.generated.resources.dashboard_title
import codetrack.shared.generated.resources.easy
import codetrack.shared.generated.resources.hard
import codetrack.shared.generated.resources.medium
import codetrack.shared.generated.resources.recent_submissions
import codetrack.shared.generated.resources.total_solved
import com.ovi.codetrack.shared.domain.model.RoadmapProblem
import com.ovi.codetrack.shared.presentation.model.Difficulty
import com.ovi.codetrack.shared.presentation.model.SubmissionUiModel
import com.ovi.codetrack.shared.presentation.model.UserStatsUiModel
import com.ovi.codetrack.shared.presentation.theme.EasyColor
import com.ovi.codetrack.shared.presentation.theme.HardColor
import com.ovi.codetrack.shared.presentation.theme.MediumColor
import com.ovi.codetrack.shared.presentation.viewmodels.DashboardUiState
import com.ovi.codetrack.shared.presentation.viewmodels.DashboardViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.ovi.codetrack.shared.presentation.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAdd: (String?, String?, String?, String?) -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Hey ${uiState.userName} \uD83D\uDC4B",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Welcome Back",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAdd(null, null, null, null) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Log Code", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = CircleShape,
                expanded = true
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Skeleton Stats Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmerEffect()
                )
                // Skeleton Next Task
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
                // Skeleton Submissions List
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerEffect()
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    uiState.stats?.let { stats ->
                        StatsCard(stats, uiState.currentStreak, uiState.longestStreak)
                    }
                }

                if (uiState.todayTasks.isNotEmpty()) {
                    item {
                        TodaysGoalsCard(
                            todaySolvedCount = uiState.todaySolvedCount,
                            dailyTarget = uiState.dailyTarget,
                            tasks = uiState.todayTasks,
                            onLogIt = { task ->
                                onNavigateToAdd(task.id.toString(), task.title, task.difficulty.name, task.tags.joinToString(","))
                            }
                        )
                    }
                }
                
                item {
                    Text(
                        text = stringResource(Res.string.recent_submissions),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (uiState.recentSubmissions.isEmpty()) {
                    item {
                        EmptyState()
                    }
                } else {
                    items(uiState.recentSubmissions) { submission ->
                        SubmissionItem(submission)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun StatsCard(stats: UserStatsUiModel, currentStreak: Int, longestStreak: Int) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StreakBadge(icon = Icons.Default.LocalFireDepartment, label = "Streak", value = currentStreak.toString(), color = Color(0xFFE65100))
                StreakBadge(icon = Icons.Default.EmojiEvents, label = "Best", value = longestStreak.toString(), color = Color(0xFFFFD600))
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LeetCode Problems Solved",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stats.totalSolved,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(stringResource(Res.string.easy), stats.easySolved, EasyColor)
                StatItem(stringResource(Res.string.medium), stats.mediumSolved, MediumColor)
                StatItem(stringResource(Res.string.hard), stats.hardSolved, HardColor)
            }
        }
    }
}

@Composable
fun StreakBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun TodaysGoalsCard(
    todaySolvedCount: Int,
    dailyTarget: Int,
    tasks: List<RoadmapProblem>,
    onLogIt: (RoadmapProblem) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val progress = (todaySolvedCount.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with circular progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Today's Goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (todaySolvedCount >= dailyTarget) "Goal crushed! \uD83D\uDD25" else "${dailyTarget - todaySolvedCount} more to go!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        strokeWidth = 6.dp,
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp,
                    )
                    Text(
                        text = "$todaySolvedCount/$dailyTarget",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Task List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f))
                            .clickable { uriHandler.openUri(task.url) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.difficulty.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (task.difficulty) {
                                    Difficulty.EASY -> EasyColor
                                    Difficulty.MEDIUM -> MediumColor
                                    Difficulty.HARD -> HardColor
                                    Difficulty.UNKNOWN -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { onLogIt(task) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Log It", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.8f))
    }
}

@Composable
fun SubmissionItem(submission: SubmissionUiModel) {
    val color = when(submission.difficulty) {
        Difficulty.EASY -> EasyColor
        Difficulty.MEDIUM -> MediumColor
        Difficulty.HARD -> HardColor
        Difficulty.UNKNOWN -> MaterialTheme.colorScheme.onSurface
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = submission.difficulty.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = submission.formattedTimeTaken,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = submission.formattedDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Code,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No recent submissions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Log a problem to see your progress here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
