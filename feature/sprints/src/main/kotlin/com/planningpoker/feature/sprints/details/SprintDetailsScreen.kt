package com.planningpoker.feature.sprints.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.planningpoker.core.designsystem.component.EmptyState
import com.planningpoker.feature.sprints.R

@Composable
internal fun SprintDetailsRoute(
    onBack: () -> Unit,
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
    ) { padding ->
        when (uiState) {
            SprintDetailsUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is SprintDetailsUiState.Success -> EmptyState(
                icon = Icons.Default.CheckCircle,
                title = stringResource(R.string.feature_sprints_no_tasks_title),
                message = stringResource(R.string.feature_sprints_no_tasks_message),
                modifier = Modifier.padding(padding),
            )
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
