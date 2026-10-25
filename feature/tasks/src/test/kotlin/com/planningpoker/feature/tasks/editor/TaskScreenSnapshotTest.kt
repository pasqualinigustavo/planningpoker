package com.planningpoker.feature.tasks.editor

import androidx.compose.material3.SnackbarHostState
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.planningpoker.core.designsystem.theme.PlanningPokerTheme
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class TaskScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    private val empty = TaskUiState(
        isNewTask = true,
        isLoaded = true,
        description = "",
        selectedVote = null,
        isEvaluating = false,
        canEvaluate = false,
        savedTask = null,
    )
    private val filled = empty.copy(
        description = "Add fingerprint login to the sign-in screen with a PIN fallback",
        selectedVote = Vote.FIVE,
        canEvaluate = true,
    )
    private val savedTask = Task(
        id = 1,
        sprintId = 1,
        title = "Fingerprint login with PIN fallback",
        description = filled.description,
        userVote = Vote.FIVE,
        aiVote = Vote.EIGHT,
        risks = listOf(
            "Biometric APIs behave differently across device manufacturers.",
            "The PIN fallback needs its own secure storage and lockout rules.",
        ),
        evaluatedAt = Instant.parse("2026-10-25T14:30:00Z"),
    )
    private val saved = filled.copy(isNewTask = false, savedTask = savedTask)

    @Test
    fun newEmpty() = snapshot(empty)

    @Test
    fun newFilled() = snapshot(filled)

    @Test
    fun evaluating() = snapshot(filled.copy(isEvaluating = true, canEvaluate = false))

    @Test
    fun savedTask() = snapshot(saved)

    @Test
    fun savedTaskDark() = snapshot(saved, darkTheme = true)

    @Test
    fun savedTaskWithoutRisks() = snapshot(saved.copy(savedTask = savedTask.copy(risks = emptyList())))

    @Test
    fun noVoiceInput() = snapshot(filled, isVoiceInputAvailable = false)

    private fun snapshot(
        uiState: TaskUiState,
        darkTheme: Boolean = false,
        isVoiceInputAvailable: Boolean = true,
    ) = paparazzi.snapshot {
        PlanningPokerTheme(darkTheme = darkTheme, dynamicColor = false) {
            TaskScreen(
                uiState = uiState,
                isVoiceInputAvailable = isVoiceInputAvailable,
                snackbarHostState = SnackbarHostState(),
                onBack = {},
                onDescriptionChange = {},
                onVoteSelect = {},
                onDictateClick = {},
                onEvaluateClick = {},
                zoneId = ZoneOffset.UTC,
            )
        }
    }
}
