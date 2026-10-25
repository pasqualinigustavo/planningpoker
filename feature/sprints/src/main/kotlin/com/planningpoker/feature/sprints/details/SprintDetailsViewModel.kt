package com.planningpoker.feature.sprints.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.planningpoker.core.data.repository.SprintRepository
import com.planningpoker.core.data.repository.TaskRepository
import com.planningpoker.feature.sprints.navigation.SprintDetailsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SprintDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sprintRepository: SprintRepository,
    taskRepository: TaskRepository,
) : ViewModel() {

    val sprintId = savedStateHandle.toRoute<SprintDetailsRoute>().sprintId

    private val navigateBackChannel = Channel<Unit>(Channel.CONFLATED)

    /** Fires once when the sprint no longer exists: after a confirmed delete, or a stale entry. */
    val navigateBack: Flow<Unit> = navigateBackChannel.receiveAsFlow()

    val uiState: StateFlow<SprintDetailsUiState> = sprintRepository.observeSprint(sprintId)
        .onEach { sprint -> if (sprint == null) navigateBackChannel.trySend(Unit) }
        .filterNotNull()
        .combine(taskRepository.observeTasks(sprintId)) { sprint, tasks ->
            SprintDetailsUiState.Success(sprint, tasks) as SprintDetailsUiState
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SprintDetailsUiState.Loading,
        )

    val isDeleteDialogOpen: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_DELETE_DIALOG_OPEN, false)

    fun onDeleteClick() {
        savedStateHandle[KEY_DELETE_DIALOG_OPEN] = true
    }

    fun onDismissDeleteDialog() {
        savedStateHandle[KEY_DELETE_DIALOG_OPEN] = false
    }

    fun onConfirmDelete() {
        savedStateHandle[KEY_DELETE_DIALOG_OPEN] = false
        // Deleting makes observeSprint emit null, which triggers navigateBack.
        viewModelScope.launch { sprintRepository.delete(sprintId) }
    }

    internal companion object {
        const val KEY_DELETE_DIALOG_OPEN = "deleteDialogOpen"
    }
}
