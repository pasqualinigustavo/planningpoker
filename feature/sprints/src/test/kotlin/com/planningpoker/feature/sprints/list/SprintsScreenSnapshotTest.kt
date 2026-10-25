package com.planningpoker.feature.sprints.list

import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.planningpoker.core.designsystem.theme.PlanningPokerTheme
import com.planningpoker.core.model.Sprint
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class SprintsScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val sprints = listOf(
        Sprint(3, "Sprint 42", Instant.parse("2026-10-25T12:00:00Z")),
        Sprint(2, "Sprint 41", Instant.parse("2026-10-11T12:00:00Z")),
        Sprint(1, "Onboarding redesign", Instant.parse("2026-09-27T12:00:00Z")),
    )

    @Test
    fun empty() = snapshot(SprintsUiState.Empty)

    @Test
    fun populated() = snapshot(SprintsUiState.Sprints(sprints))

    @Test
    fun populatedDark() = snapshot(SprintsUiState.Sprints(sprints), darkTheme = true)

    @Test
    fun createDialogEmpty() = snapshot(SprintsUiState.Empty, CreateSprintDialogState(""))

    @Test
    fun createDialogFilled() =
        snapshot(SprintsUiState.Sprints(sprints), CreateSprintDialogState("Sprint 43"))

    private fun snapshot(
        uiState: SprintsUiState,
        dialogState: CreateSprintDialogState? = null,
        darkTheme: Boolean = false,
    ) = paparazzi.snapshot {
        PlanningPokerTheme(darkTheme = darkTheme, dynamicColor = false) {
            Screen(uiState, dialogState)
        }
    }

    @Composable
    private fun Screen(uiState: SprintsUiState, dialogState: CreateSprintDialogState?) {
        SprintsScreen(
            uiState = uiState,
            dialogState = dialogState,
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
