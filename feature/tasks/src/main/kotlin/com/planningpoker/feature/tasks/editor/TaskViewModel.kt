package com.planningpoker.feature.tasks.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.planningpoker.core.data.repository.TaskRepository
import com.planningpoker.core.data.repository.TaskSaveOutcome
import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import com.planningpoker.feature.tasks.navigation.TaskRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TaskEvent {
    data class TaskCreated(val sprintId: Long, val taskId: Long) : TaskEvent
    data class EvaluationFailed(val error: EvaluationError) : TaskEvent
    data object TaskMissing : TaskEvent
}

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TaskRoute>()
    val isNewTask: Boolean = route.taskId == null

    // Draft as Compose state so the text field updates synchronously, mirrored into
    // SavedStateHandle so it survives rotation and process death.
    var description by mutableStateOf(savedStateHandle[KEY_DESCRIPTION] ?: "")
        private set
    var selectedVote by mutableStateOf(savedStateHandle.get<String>(KEY_VOTE)?.let(Vote::valueOf))
        private set

    /** In memory only: after process death the request is gone and the draft is editable again. */
    var isEvaluating by mutableStateOf(false)
        private set

    /** False while a saved task's values are being loaded into the draft. */
    var isLoaded by mutableStateOf(isNewTask || savedStateHandle.contains(KEY_DESCRIPTION))
        private set

    val canEvaluate: Boolean
        get() = isLoaded && !isEvaluating && description.isNotBlank() && selectedVote != null

    /** The stored evaluation of a saved task; always null for a new task. */
    val savedTask: StateFlow<Task?> = route.taskId
        ?.let { id ->
            taskRepository.observeTask(id)
                .stateIn(viewModelScope, SharingStarted.Eagerly, null)
        }
        ?: MutableStateFlow(null)

    private val eventChannel = Channel<TaskEvent>(Channel.BUFFERED)
    val events: Flow<TaskEvent> = eventChannel.receiveAsFlow()

    init {
        val taskId = route.taskId
        // Load a saved task into the draft once; a draft restored from SavedStateHandle wins.
        if (taskId != null && !isLoaded) {
            viewModelScope.launch {
                val task = taskRepository.observeTask(taskId).first()
                if (task == null) {
                    eventChannel.send(TaskEvent.TaskMissing)
                } else {
                    updateDescription(task.description)
                    updateVote(task.userVote)
                    isLoaded = true
                }
            }
        }
    }

    fun onDescriptionChange(text: String) {
        if (!isEvaluating) updateDescription(text)
    }

    fun onVoteSelect(vote: Vote) {
        if (!isEvaluating) updateVote(vote)
    }

    fun onDictationResult(text: String) {
        if (!isEvaluating) updateDescription(appendDictation(description, text))
    }

    fun onEvaluateClick() {
        val vote = selectedVote
        if (!canEvaluate || vote == null) return
        isEvaluating = true
        viewModelScope.launch {
            val taskId = route.taskId
            val outcome = if (taskId == null) {
                taskRepository.evaluateAndCreate(route.sprintId, description, vote)
            } else {
                taskRepository.reevaluate(taskId, description, vote)
            }
            isEvaluating = false
            when (outcome) {
                is TaskSaveOutcome.Saved ->
                    if (isNewTask) eventChannel.send(TaskEvent.TaskCreated(route.sprintId, outcome.taskId))
                is TaskSaveOutcome.Failed -> eventChannel.send(TaskEvent.EvaluationFailed(outcome.error))
            }
        }
    }

    private fun updateDescription(text: String) {
        description = text
        savedStateHandle[KEY_DESCRIPTION] = text
    }

    private fun updateVote(vote: Vote) {
        selectedVote = vote
        savedStateHandle[KEY_VOTE] = vote.name
    }

    internal companion object {
        const val KEY_DESCRIPTION = "description"
        const val KEY_VOTE = "vote"
    }
}

/** Adds dictated text to the description, separated by a single space. */
internal fun appendDictation(current: String, dictated: String): String {
    val addition = dictated.trim()
    return when {
        addition.isEmpty() -> current
        current.isBlank() -> addition
        else -> current.trimEnd() + " " + addition
    }
}
