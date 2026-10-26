package com.planningpoker.feature.sprints.details

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.planningpoker.core.designsystem.theme.PlanningPokerTheme
import com.planningpoker.core.model.Sprint
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import java.time.Instant
import org.junit.Rule
import org.junit.Test

class SprintDetailsScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val sprint = Sprint(id = 1, name = "Sprint 42", createdAt = Instant.parse("2026-10-25T12:00:00Z"))
    private val loaded = SprintDetailsUiState.Success(sprint, emptyList())
    private val withTasks = SprintDetailsUiState.Success(
        sprint,
        listOf(
            task(3, "Fingerprint login with PIN fallback", Vote.FIVE, Vote.EIGHT),
            task(2, "Export sprint summary as PDF", Vote.UNKNOWN, Vote.THIRTEEN),
            task(1, "Rename sprint", Vote.ONE, Vote.ONE),
        ),
    )

    private fun task(id: Long, title: String, userVote: Vote, aiVote: Vote) = Task(
        id = id,
        sprintId = sprint.id,
        title = title,
        description = title,
        userVote = userVote,
        aiVote = aiVote,
        risks = emptyList(),
        evaluatedAt = Instant.parse("2026-10-25T12:00:00Z"),
    )

    @Test
    fun noTasks() = snapshot(loaded, isDeleteDialogOpen = false)

    @Test
    fun noTasksDark() = snapshot(loaded, isDeleteDialogOpen = false, darkTheme = true)

    @Test
    fun withTasks() = snapshot(withTasks, isDeleteDialogOpen = false)

    @Test
    fun withTasksDark() = snapshot(withTasks, isDeleteDialogOpen = false, darkTheme = true)

    @Test
    fun deleteDialog() = snapshot(loaded, isDeleteDialogOpen = true)

    private fun snapshot(uiState: SprintDetailsUiState, isDeleteDialogOpen: Boolean, darkTheme: Boolean = false) =
        paparazzi.snapshot {
            PlanningPokerTheme(darkTheme = darkTheme, dynamicColor = false) {
                SprintDetailsScreen(
                    uiState = uiState,
                    isDeleteDialogOpen = isDeleteDialogOpen,
                    onBack = {},
                    onCreateTask = {},
                    onTaskClick = {},
                    onDeleteClick = {},
                    onConfirmDelete = {},
                    onDismissDeleteDialog = {},
                )
            }
        }
}
