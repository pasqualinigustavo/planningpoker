package com.planningpoker.feature.sprints.details

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.planningpoker.core.designsystem.theme.PlanningPokerTheme
import com.planningpoker.core.model.Sprint
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class SprintDetailsScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val loaded = SprintDetailsUiState.Success(
        Sprint(id = 1, name = "Sprint 42", createdAt = Instant.parse("2026-10-25T12:00:00Z")),
    )

    @Test
    fun noTasks() = snapshot(loaded, isDeleteDialogOpen = false)

    @Test
    fun noTasksDark() = snapshot(loaded, isDeleteDialogOpen = false, darkTheme = true)

    @Test
    fun deleteDialog() = snapshot(loaded, isDeleteDialogOpen = true)

    private fun snapshot(
        uiState: SprintDetailsUiState,
        isDeleteDialogOpen: Boolean,
        darkTheme: Boolean = false,
    ) = paparazzi.snapshot {
        PlanningPokerTheme(darkTheme = darkTheme, dynamicColor = false) {
            SprintDetailsScreen(
                uiState = uiState,
                isDeleteDialogOpen = isDeleteDialogOpen,
                onBack = {},
                onDeleteClick = {},
                onConfirmDelete = {},
                onDismissDeleteDialog = {},
            )
        }
    }
}
