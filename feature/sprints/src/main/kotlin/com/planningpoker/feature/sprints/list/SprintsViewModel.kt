package com.planningpoker.feature.sprints.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planningpoker.core.data.repository.SprintRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SprintsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sprintRepository: SprintRepository,
) : ViewModel() {

    val uiState: StateFlow<SprintsUiState> = sprintRepository.observeSprints()
        .map { sprints ->
            if (sprints.isEmpty()) SprintsUiState.Empty else SprintsUiState.Sprints(sprints)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SprintsUiState.Loading,
        )

    // Compose state so the text field updates synchronously (an async StateFlow round trip
    // scrambles fast typing), mirrored into SavedStateHandle to survive process death.
    private var isDialogOpen by mutableStateOf(savedStateHandle[KEY_DIALOG_OPEN] ?: false)
    private var dialogName by mutableStateOf(savedStateHandle[KEY_NAME] ?: "")

    /** The open create-sprint dialog, or `null` when it is closed. */
    val createDialogState: CreateSprintDialogState?
        get() = if (isDialogOpen) CreateSprintDialogState(dialogName) else null

    fun onCreateSprintClick() {
        updateName("")
        updateDialogOpen(true)
    }

    fun onNameChange(name: String) = updateName(name)

    fun onDismissDialog() = closeDialog()

    fun onSaveClick() {
        val name = dialogName
        if (name.isBlank()) return
        // Close first so a second tap cannot create a duplicate.
        closeDialog()
        viewModelScope.launch { sprintRepository.create(name) }
    }

    private fun closeDialog() {
        updateDialogOpen(false)
        updateName("")
    }

    private fun updateDialogOpen(open: Boolean) {
        isDialogOpen = open
        savedStateHandle[KEY_DIALOG_OPEN] = open
    }

    private fun updateName(name: String) {
        dialogName = name
        savedStateHandle[KEY_NAME] = name
    }

    internal companion object {
        const val KEY_DIALOG_OPEN = "createDialogOpen"
        const val KEY_NAME = "createDialogName"
    }
}
