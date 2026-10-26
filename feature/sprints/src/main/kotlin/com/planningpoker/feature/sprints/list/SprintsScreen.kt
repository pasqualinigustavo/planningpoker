package com.planningpoker.feature.sprints.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.planningpoker.core.designsystem.component.EmptyState
import com.planningpoker.core.designsystem.theme.PlanningPokerTheme
import com.planningpoker.core.model.Sprint
import com.planningpoker.feature.sprints.R
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun SprintsRoute(onSprintClick: (Long) -> Unit, viewModel: SprintsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SprintsScreen(
        uiState = uiState,
        dialogState = viewModel.createDialogState,
        onSprintClick = onSprintClick,
        onCreateSprintClick = viewModel::onCreateSprintClick,
        onNameChange = viewModel::onNameChange,
        onSaveClick = viewModel::onSaveClick,
        onDismissDialog = viewModel::onDismissDialog,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SprintsScreen(
    uiState: SprintsUiState,
    dialogState: CreateSprintDialogState?,
    onSprintClick: (Long) -> Unit,
    onCreateSprintClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismissDialog: () -> Unit,
    zoneId: ZoneId = remember { ZoneId.systemDefault() },
    requestDialogFocus: Boolean = true,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.feature_sprints_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateSprintClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.feature_sprints_create))
            }
        },
    ) { padding ->
        when (uiState) {
            SprintsUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            SprintsUiState.Empty -> EmptyState(
                icon = Icons.AutoMirrored.Filled.List,
                title = stringResource(R.string.feature_sprints_empty_title),
                message = stringResource(R.string.feature_sprints_empty_message),
                modifier = Modifier.padding(padding),
            )

            is SprintsUiState.Sprints -> SprintList(
                sprints = uiState.sprints,
                onSprintClick = onSprintClick,
                zoneId = zoneId,
                contentPadding = padding,
            )
        }
    }

    if (dialogState != null) {
        CreateSprintDialog(
            state = dialogState,
            onNameChange = onNameChange,
            onSave = onSaveClick,
            onDismiss = onDismissDialog,
            requestFocus = requestDialogFocus,
        )
    }
}

@Composable
private fun SprintList(
    sprints: List<Sprint>,
    onSprintClick: (Long) -> Unit,
    zoneId: ZoneId,
    contentPadding: PaddingValues,
) {
    val formatter = remember(zoneId) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(zoneId)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            // Room for the FAB so the last sprint is never hidden behind it.
            bottom = contentPadding.calculateBottomPadding() + 88.dp,
        ),
    ) {
        items(sprints, key = Sprint::id) { sprint ->
            ListItem(
                headlineContent = { Text(sprint.name) },
                supportingContent = {
                    Text(stringResource(R.string.feature_sprints_created_on, formatter.format(sprint.createdAt)))
                },
                modifier = Modifier.clickable { onSprintClick(sprint.id) },
            )
            HorizontalDivider()
        }
    }
}

private class SprintsUiStatePreviewProvider : PreviewParameterProvider<SprintsUiState> {
    override val values = sequenceOf(
        SprintsUiState.Sprints(
            listOf(
                Sprint(id = 3, name = "Sprint 42", createdAt = Instant.parse("2026-10-25T12:00:00Z")),
                Sprint(id = 2, name = "Sprint 41", createdAt = Instant.parse("2026-10-11T12:00:00Z")),
                Sprint(id = 1, name = "Sprint 40", createdAt = Instant.parse("2026-09-27T12:00:00Z")),
            ),
        ),
        SprintsUiState.Empty,
        SprintsUiState.Loading,
    )
}

@PreviewLightDark
@Composable
private fun SprintsScreenPreview(@PreviewParameter(SprintsUiStatePreviewProvider::class) uiState: SprintsUiState) {
    PlanningPokerTheme(dynamicColor = false) {
        SprintsScreen(
            uiState = uiState,
            dialogState = null,
            onSprintClick = {},
            onCreateSprintClick = {},
            onNameChange = {},
            onSaveClick = {},
            onDismissDialog = {},
            zoneId = ZoneOffset.UTC,
        )
    }
}

@Preview
@Composable
private fun SprintsScreenCreateDialogPreview() {
    PlanningPokerTheme(dynamicColor = false) {
        SprintsScreen(
            uiState = SprintsUiState.Empty,
            dialogState = CreateSprintDialogState(name = "Sprint 43"),
            onSprintClick = {},
            onCreateSprintClick = {},
            onNameChange = {},
            onSaveClick = {},
            onDismissDialog = {},
            zoneId = ZoneOffset.UTC,
            requestDialogFocus = false,
        )
    }
}
