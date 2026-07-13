package com.ovi.codetrack.shared.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ovi.codetrack.shared.presentation.model.Difficulty
import com.ovi.codetrack.shared.domain.model.RoadmapProblem
import com.ovi.codetrack.shared.presentation.theme.EasyColor
import com.ovi.codetrack.shared.presentation.theme.HardColor
import com.ovi.codetrack.shared.presentation.theme.MediumColor
import com.ovi.codetrack.shared.presentation.viewmodels.RoadmapViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(
    onNavigateToAdd: (String?, String?, String?) -> Unit,
    viewModel: RoadmapViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Curriculum Roadmap",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Follow this curated list of problems to master Data Structures and Algorithms. Start from the top and work your way down.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(uiState.problems) { problem ->
                val isSolved = uiState.solvedProblemIds.contains(problem.id.toString())
                RoadmapProblemItem(
                    problem = problem,
                    isSolved = isSolved,
                    onLogIt = { onNavigateToAdd(problem.id.toString(), problem.title, problem.difficulty.name) }
                )
            }
        }
    }
}

@Composable
fun RoadmapProblemItem(
    problem: RoadmapProblem,
    isSolved: Boolean,
    onLogIt: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val difficultyColor = when(problem.difficulty) {
        Difficulty.EASY -> EasyColor
        Difficulty.MEDIUM -> MediumColor
        Difficulty.HARD -> HardColor
        Difficulty.UNKNOWN -> MaterialTheme.colorScheme.onSurface
    }

    val contentAlpha = if (isSolved) 0.6f else 1f

    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable { uriHandler.openUri(problem.url) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark or Empty Circle
            Icon(
                imageVector = if (isSolved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSolved) "Solved" else "Unsolved",
                tint = if (isSolved) EasyColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = problem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = contentAlpha)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.clip(CircleShape).background(difficultyColor.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Text(text = problem.difficulty.name, style = MaterialTheme.typography.labelMedium, color = difficultyColor.copy(alpha = contentAlpha), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            if (!isSolved) {
                IconButton(
                    onClick = onLogIt,
                    modifier = Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Log It", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}
