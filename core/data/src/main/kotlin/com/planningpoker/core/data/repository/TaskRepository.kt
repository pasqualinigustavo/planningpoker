package com.planningpoker.core.data.repository

import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    /** The sprint's tasks, most recently evaluated first. */
    fun observeTasks(sprintId: Long): Flow<List<Task>>

    /** Emits `null` once the task no longer exists. */
    fun observeTask(id: Long): Flow<Task?>

    /**
     * Asks the Tech Lead to evaluate [description] and, only if that succeeds, stores a new task
     * in [sprintId]. [userVote] is stored but never sent to the AI.
     */
    suspend fun evaluateAndCreate(sprintId: Long, description: String, userVote: Vote): TaskSaveOutcome

    /**
     * Re-evaluates [description] and, only if that succeeds, overwrites every field of the task
     * with the edits and the new result. On failure the stored task is left unchanged.
     */
    suspend fun reevaluate(taskId: Long, description: String, userVote: Vote): TaskSaveOutcome
}

sealed interface TaskSaveOutcome {
    data class Saved(val taskId: Long) : TaskSaveOutcome
    data class Failed(val error: EvaluationError) : TaskSaveOutcome
}
