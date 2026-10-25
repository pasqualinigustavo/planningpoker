package com.planningpoker.feature.sprints.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.planningpoker.core.designsystem.component.EmptyState
import com.planningpoker.core.model.Task
import com.planningpoker.feature.sprints.R

@Composable
internal fun SprintDetailsRoute(
    onBack: () -> Unit,
    onCreateTask: (sprintId: Long) -> Unit,
    onTaskClick: (sprintId: Long, taskId: Long) -> Unit,
    viewModel: SprintDetailsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDeleteDialogOpen by viewModel.isDeleteDialogOpen.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)

    LaunchedEffect(viewModel) {
        viewModel.navigateBack.collect { currentOnBack() }
    }

    SprintDetailsScreen(
        uiState = uiState,
        isDeleteDialogOpen = isDeleteDialogOpen,
        onBack = onBack,
        onCreateTask = { onCreateTask(viewModel.sprintId) },
        onTaskClick = { taskId -> onTaskClick(viewModel.sprintId, taskId) },
        onDeleteClick = viewModel::onDeleteClick,
        onConfirmDelete = viewModel::onConfirmDelete,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SprintDetailsScreen(
    uiState: SprintDetailsUiState,
    isDeleteDialogOpen: Boolean,
    onBack: () -> Unit,
    onCreateTask: () -> Unit,
    onTaskClick: (taskId: Long) -> Unit,
    onDeleteClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
) {
    val sprint = (uiState as? SprintDetailsUiState.Success)?.sprint
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(sprint?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.feature_sprints_back),
                        )
                    }
                },
                actions = {
                    if (sprint != null) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.feature_sprints_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (sprint != null) {
                ExtendedFloatingActionButton(
                    onClick = onCreateTask,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.feature_sprints_create_task)) },
                )
            }
        },
    ) { padding ->
        when (uiState) {
            SprintDetailsUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is SprintDetailsUiState.Success -> if (uiState.tasks.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CheckCircle,
                    title = stringResource(R.string.feature_sprints_no_tasks_title),
                    message = stringResource(R.string.feature_sprints_no_tasks_message),
                    modifier = Modifier.padding(padding),
                )
            } else {
                TaskList(tasks = uiState.tasks, onTaskClick = onTaskClick, contentPadding = padding)
            }
        }
    }

    if (isDeleteDialogOpen && sprint != null) {
        DeleteSprintDialog(
            sprintName = sprint.name,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDeleteDialog,
        )
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onTaskClick: (taskId: Long) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            // Room for the FAB so the last task is never hidden behind it.
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        items(tasks, key = Task::id) { task ->
            ListItem(
                headlineContent = { Text(task.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                supportingContent = {
                    Text(stringResource(R.string.feature_sprints_task_votes, task.userVote.label, task.aiVote.label))
                },
                modifier = Modifier.clickable { onTaskClick(task.id) },
            )
            HorizontalDivider()
        }
    }
}
