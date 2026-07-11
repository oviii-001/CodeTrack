package com.ovi.codetrack.shared.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import codetrack.shared.generated.resources.*
import codetrack.shared.generated.resources.Res
import codetrack.shared.generated.resources.add_submission_title
import codetrack.shared.generated.resources.difficulty_hint
import codetrack.shared.generated.resources.notes_hint
import codetrack.shared.generated.resources.problem_id_hint
import codetrack.shared.generated.resources.problem_name_hint
import codetrack.shared.generated.resources.save_button
import codetrack.shared.generated.resources.tags_hint
import codetrack.shared.generated.resources.time_taken_hint
import com.ovi.codetrack.shared.presentation.viewmodels.AddSubmissionViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubmissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSubmissionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var problemId by remember { mutableStateOf("") }
    var problemName by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Easy") }
    var tags by remember { mutableStateOf("") }
    var timeTaken by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.add_submission_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = problemId,
                onValueChange = { problemId = it },
                label = { Text(stringResource(Res.string.problem_id_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = problemName,
                onValueChange = { problemName = it },
                label = { Text(stringResource(Res.string.problem_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Simplistic difficulty selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Easy", "Medium", "Hard").forEach { diff ->
                    FilterChip(
                        selected = difficulty == diff,
                        onClick = { difficulty = diff },
                        label = { Text(diff) }
                    )
                }
            }

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text(stringResource(Res.string.tags_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = timeTaken,
                onValueChange = { timeTaken = it },
                label = { Text(stringResource(Res.string.time_taken_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(Res.string.notes_hint)) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    viewModel.addSubmission(
                        problemIdStr = problemId,
                        problemName = problemName,
                        difficulty = difficulty,
                        tagsStr = tags,
                        timeTakenStr = timeTaken,
                        notes = notes
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(Res.string.save_button))
                }
            }
        }
    }
}
