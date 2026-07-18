package com.ovi.codetrack.shared.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ovi.codetrack.shared.data.remote.LeetCodeProblem
import com.ovi.codetrack.shared.presentation.theme.EasyColor
import com.ovi.codetrack.shared.presentation.theme.HardColor
import com.ovi.codetrack.shared.presentation.theme.MediumColor
import com.ovi.codetrack.shared.presentation.viewmodels.AddSubmissionViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubmissionScreen(
    initialProblemId: String = "",
    initialProblemName: String = "",
    initialDifficulty: String = "Easy",
    initialTags: String = "",
    onNavigateBack: () -> Unit,
    viewModel: AddSubmissionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // State for user inputs
    var searchQuery by remember { mutableStateOf("") }
    var timeTaken by remember { mutableStateOf("") }
    var timeComplexity by remember { mutableStateOf("") }
    var spaceComplexity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }

    // Manual entry fields (only used when not auto-filled)
    var manualProblemId by remember { mutableStateOf("") }
    var manualProblemName by remember { mutableStateOf("") }
    var manualDifficulty by remember { mutableStateOf("Easy") }
    var manualTags by remember { mutableStateOf("") }

    // Pre-fill from roadmap if initial data provided
    LaunchedEffect(initialProblemId) {
        if (initialProblemId.isNotBlank() && initialProblemName.isNotBlank()) {
            viewModel.preFilFromRoadmap(initialProblemId, initialProblemName, initialDifficulty)
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    // Derive the problem data to use for submission
    val activeProblem = uiState.lookedUpProblem

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Solution",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ======= SECTION 1: Problem Selection =======
            if (uiState.isPreFilled && activeProblem != null) {
                // Pre-filled from roadmap — show read-only card
                ProblemInfoCard(problem = activeProblem)

                TextButton(
                    onClick = {
                        viewModel.clearLookup()
                        showManualEntry = true
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Change problem", style = MaterialTheme.typography.labelMedium)
                }
            } else if (!showManualEntry) {
                // Search mode — type to find problem
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Find Problem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.lookupProblem(it)
                            },
                            label = { Text("Problem name (e.g. two-sum)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.isLookingUp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                            )
                        )

                        // Lookup result preview
                        AnimatedVisibility(visible = activeProblem != null && !uiState.isPreFilled) {
                            if (activeProblem != null) {
                                ProblemInfoCard(problem = activeProblem)
                            }
                        }

                        // Lookup error
                        if (uiState.lookupError != null) {
                            Text(
                                text = uiState.lookupError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                TextButton(
                    onClick = { showManualEntry = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enter manually", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                // Manual entry mode
                ManualEntryCard(
                    problemId = manualProblemId,
                    onProblemIdChange = { manualProblemId = it },
                    problemName = manualProblemName,
                    onProblemNameChange = { manualProblemName = it },
                    difficulty = manualDifficulty,
                    onDifficultyChange = { manualDifficulty = it },
                    tags = manualTags,
                    onTagsChange = { manualTags = it },
                    focusManager = focusManager
                )

                TextButton(
                    onClick = {
                        showManualEntry = false
                        searchQuery = ""
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Search instead", style = MaterialTheme.typography.labelMedium)
                }
            }

            // ======= SECTION 2: Performance =======
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = timeTaken,
                        onValueChange = { timeTaken = it },
                        label = { Text("Time Taken (minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = timeComplexity,
                            onValueChange = { timeComplexity = it },
                            label = { Text("Time (e.g. O(N))") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                            )
                        )

                        OutlinedTextField(
                            value = spaceComplexity,
                            onValueChange = { spaceComplexity = it },
                            label = { Text("Space (e.g. O(1))") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                        )
                    )
                }
            }

            // ======= Error Message =======
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // ======= Submit Button =======
            Button(
                onClick = {
                    val finalProblemId: String
                    val finalProblemName: String
                    val finalDifficulty: String
                    val finalTags: String

                    if (activeProblem != null && !showManualEntry) {
                        finalProblemId = activeProblem.questionId
                        finalProblemName = activeProblem.title
                        finalDifficulty = activeProblem.difficulty
                        finalTags = activeProblem.topicTags.joinToString(",")
                    } else {
                        finalProblemId = manualProblemId
                        finalProblemName = manualProblemName
                        finalDifficulty = manualDifficulty
                        finalTags = manualTags
                    }

                    viewModel.addSubmission(
                        problemIdStr = finalProblemId,
                        problemName = finalProblemName,
                        difficulty = finalDifficulty,
                        tagsStr = finalTags,
                        timeTakenStr = timeTaken,
                        timeComplexityStr = timeComplexity,
                        spaceComplexityStr = spaceComplexity,
                        notes = notes
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Solution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ======= Problem Info Card (Read-Only Preview) =======
@Composable
fun ProblemInfoCard(problem: LeetCodeProblem) {
    val difficultyColor = when (problem.difficulty.uppercase()) {
        "EASY" -> EasyColor
        "MEDIUM" -> MediumColor
        "HARD" -> HardColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "#${problem.questionId}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = problem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(difficultyColor.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = problem.difficulty,
                        style = MaterialTheme.typography.labelMedium,
                        color = difficultyColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (problem.topicTags.isNotEmpty()) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    problem.topicTags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ======= Manual Entry Card =======
@Composable
fun ManualEntryCard(
    problemId: String,
    onProblemIdChange: (String) -> Unit,
    problemName: String,
    onProblemNameChange: (String) -> Unit,
    difficulty: String,
    onDifficultyChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Problem Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = problemId,
                onValueChange = onProblemIdChange,
                label = { Text("Problem ID") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )
            )

            OutlinedTextField(
                value = problemName,
                onValueChange = onProblemNameChange,
                label = { Text("Problem Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )
            )

            // Difficulty selector
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DifficultyButton(
                    text = "Easy",
                    isSelected = difficulty == "Easy",
                    activeColor = EasyColor,
                    modifier = Modifier.weight(1f)
                ) { onDifficultyChange("Easy") }

                DifficultyButton(
                    text = "Medium",
                    isSelected = difficulty == "Medium",
                    activeColor = MediumColor,
                    modifier = Modifier.weight(1f)
                ) { onDifficultyChange("Medium") }

                DifficultyButton(
                    text = "Hard",
                    isSelected = difficulty == "Hard",
                    activeColor = HardColor,
                    modifier = Modifier.weight(1f)
                ) { onDifficultyChange("Hard") }
            }

            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChange,
                label = { Text("Tags (Comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun DifficultyButton(
    text: String,
    isSelected: Boolean,
    activeColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) activeColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
