package com.planningpoker.feature.tasks.editor

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.planningpoker.core.designsystem.theme.PlanningPokerTheme
import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import com.planningpoker.feature.tasks.R
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Everything [TaskScreen] renders, so it can be previewed and snapshot-tested without a ViewModel. */
internal data class TaskUiState(
    val isNewTask: Boolean,
    val isLoaded: Boolean,
    val description: String,
    val selectedVote: Vote?,
    val isEvaluating: Boolean,
    val canEvaluate: Boolean,
    val savedTask: Task?,
)

@Composable
internal fun TaskRoute(
    onBack: () -> Unit,
    onTaskCreated: (sprintId: Long, taskId: Long) -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
) {
    val savedTask by viewModel.savedTask.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnTaskCreated by rememberUpdatedState(onTaskCreated)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is TaskEvent.TaskCreated -> currentOnTaskCreated(event.sprintId, event.taskId)

                is TaskEvent.EvaluationFailed ->
                    snackbarHostState.showSnackbar(context.getString(event.error.messageRes()))

                TaskEvent.TaskMissing -> currentOnBack()
            }
        }
    }

    val isVoiceInputAvailable = remember { context.isSpeechRecognitionAvailable() }
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.let(viewModel::onDictationResult)
        }
    }
    val dictationPrompt = stringResource(R.string.feature_tasks_dictation_prompt)

    TaskScreen(
        uiState = TaskUiState(
            isNewTask = viewModel.isNewTask,
            isLoaded = viewModel.isLoaded,
            description = viewModel.description,
            selectedVote = viewModel.selectedVote,
            isEvaluating = viewModel.isEvaluating,
            canEvaluate = viewModel.canEvaluate,
            savedTask = savedTask,
        ),
        isVoiceInputAvailable = isVoiceInputAvailable,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onDescriptionChange = viewModel::onDescriptionChange,
        onVoteSelect = viewModel::onVoteSelect,
        onDictateClick = {
            try {
                speechLauncher.launch(speechRecognitionIntent(dictationPrompt))
            } catch (_: ActivityNotFoundException) {
                // The recognizer disappeared after the availability check; nothing to dictate with.
            }
        },
        onEvaluateClick = viewModel::onEvaluateClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskScreen(
    uiState: TaskUiState,
    isVoiceInputAvailable: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onVoteSelect: (Vote) -> Unit,
    onDictateClick: () -> Unit,
    onEvaluateClick: () -> Unit,
    zoneId: ZoneId = remember { ZoneId.systemDefault() },
) {
    val inputsEnabled = uiState.isLoaded && !uiState.isEvaluating
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.savedTask?.title ?: stringResource(R.string.feature_tasks_new_task),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_tasks_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = onEvaluateClick,
                    enabled = uiState.canEvaluate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(16.dp),
                ) {
                    Text(
                        stringResource(
                            if (uiState.isNewTask) {
                                R.string.feature_tasks_evaluate
                            } else {
                                R.string.feature_tasks_reevaluate
                            },
                        ),
                    )
                }
            }
        },
    ) { padding ->
        if (!uiState.isLoaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(modifier = Modifier.padding(padding)) {
            val evaluatingDescription = stringResource(R.string.feature_tasks_evaluating)
            if (uiState.isEvaluating) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = evaluatingDescription },
                )
            } else {
                // Same height as the indicator so the content does not jump.
                Spacer(Modifier.height(4.dp))
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    enabled = inputsEnabled,
                    label = { Text(stringResource(R.string.feature_tasks_description)) },
                    minLines = 4,
                    trailingIcon = if (isVoiceInputAvailable) {
                        {
                            IconButton(onClick = onDictateClick, enabled = inputsEnabled) {
                                Icon(
                                    painterResource(R.drawable.ic_mic),
                                    contentDescription = stringResource(R.string.feature_tasks_dictate),
                                )
                            }
                        }
                    } else {
                        null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
                VoteScale(
                    selectedVote = uiState.selectedVote,
                    enabled = inputsEnabled,
                    onVoteSelect = onVoteSelect,
                )
                uiState.savedTask?.let { task ->
                    HorizontalDivider()
                    TechLeadEvaluation(task = task, zoneId = zoneId)
                }
            }
        }
    }
}

@Composable
private fun VoteScale(selectedVote: Vote?, enabled: Boolean, onVoteSelect: (Vote) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.feature_tasks_your_vote), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.feature_tasks_vote_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(Vote.entries) { vote ->
                FilterChip(
                    selected = vote == selectedVote,
                    onClick = { onVoteSelect(vote) },
                    enabled = enabled,
                    label = { Text(vote.label, style = MaterialTheme.typography.titleMedium) },
                )
            }
        }
    }
}

@Composable
private fun TechLeadEvaluation(task: Task, zoneId: ZoneId) {
    val formatter = remember(zoneId) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(zoneId)
    }
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.feature_tasks_tech_lead), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VoteCard(
                label = stringResource(R.string.feature_tasks_you),
                vote = task.userVote,
                modifier = Modifier.weight(1f),
            )
            VoteCard(
                label = stringResource(R.string.feature_tasks_ai),
                vote = task.aiVote,
                highlighted = true,
                modifier = Modifier.weight(1f),
            )
        }
        Text(stringResource(R.string.feature_tasks_risks), style = MaterialTheme.typography.titleSmall)
        if (task.risks.isEmpty()) {
            Text(stringResource(R.string.feature_tasks_no_risks), style = MaterialTheme.typography.bodyMedium)
        } else {
            task.risks.forEach { risk ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", style = MaterialTheme.typography.bodyMedium)
                    Text(risk, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Text(
            stringResource(R.string.feature_tasks_evaluated_at, formatter.format(task.evaluatedAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VoteCard(label: String, vote: Vote, modifier: Modifier = Modifier, highlighted: Boolean = false) {
    Card(
        modifier = modifier,
        colors = if (highlighted) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(vote.label, style = MaterialTheme.typography.displaySmall)
        }
    }
}

private fun EvaluationError.messageRes(): Int = when (this) {
    EvaluationError.Network -> R.string.feature_tasks_error_network
    EvaluationError.Timeout -> R.string.feature_tasks_error_timeout
    EvaluationError.InvalidResponse -> R.string.feature_tasks_error_invalid
    EvaluationError.Rejected -> R.string.feature_tasks_error_rejected
    EvaluationError.Unknown -> R.string.feature_tasks_error_unknown
}

private fun speechRecognitionIntent(prompt: String) = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    // Device language, so dictation matches the language the Tech Lead answers in.
    .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
    .putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)

private fun Context.isSpeechRecognitionAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(this) &&
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).resolveActivity(packageManager) != null

private val previewEmpty = TaskUiState(
    isNewTask = true,
    isLoaded = true,
    description = "",
    selectedVote = null,
    isEvaluating = false,
    canEvaluate = false,
    savedTask = null,
)

private val previewFilled = previewEmpty.copy(
    description = "Add fingerprint login to the sign-in screen with a PIN fallback",
    selectedVote = Vote.FIVE,
    canEvaluate = true,
)

private val previewSaved = previewFilled.copy(
    isNewTask = false,
    savedTask = Task(
        id = 1,
        sprintId = 1,
        title = "Fingerprint login with PIN fallback",
        description = previewFilled.description,
        userVote = Vote.FIVE,
        aiVote = Vote.EIGHT,
        risks = listOf(
            "Biometric APIs behave differently across device manufacturers.",
            "The PIN fallback needs its own secure storage and lockout rules.",
        ),
        evaluatedAt = Instant.parse("2026-10-25T14:30:00Z"),
    ),
)

private class TaskUiStatePreviewProvider : PreviewParameterProvider<TaskUiState> {
    override val values = sequenceOf(
        previewEmpty,
        previewFilled,
        previewFilled.copy(isEvaluating = true, canEvaluate = false),
        previewSaved,
        previewSaved.copy(savedTask = previewSaved.savedTask?.copy(risks = emptyList())),
        previewEmpty.copy(isLoaded = false),
    )
}

@Preview
@Composable
private fun TaskScreenPreview(@PreviewParameter(TaskUiStatePreviewProvider::class) uiState: TaskUiState) {
    TaskScreenPreviewContent(uiState)
}

@PreviewLightDark
@Composable
private fun TaskScreenSavedPreview() {
    TaskScreenPreviewContent(previewSaved)
}

@Composable
private fun TaskScreenPreviewContent(uiState: TaskUiState) {
    PlanningPokerTheme(dynamicColor = false) {
        TaskScreen(
            uiState = uiState,
            isVoiceInputAvailable = true,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onDescriptionChange = {},
            onVoteSelect = {},
            onDictateClick = {},
            onEvaluateClick = {},
            zoneId = ZoneOffset.UTC,
        )
    }
}
