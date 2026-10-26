package com.planningpoker.feature.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.planningpoker.feature.tasks.editor.TaskRoute
import kotlinx.serialization.Serializable

/** A new task in [sprintId] when [taskId] is null, otherwise the saved task [taskId]. */
@Serializable
data class TaskRoute(val sprintId: Long, val taskId: Long? = null)

fun NavController.navigateToNewTask(sprintId: Long) = navigate(TaskRoute(sprintId))

fun NavController.navigateToTask(sprintId: Long, taskId: Long) = navigate(TaskRoute(sprintId, taskId))

/** Replaces the new-task screen with the saved task, so back returns to sprint details. */
fun NavController.navigateToCreatedTask(sprintId: Long, taskId: Long) = navigate(TaskRoute(sprintId, taskId)) {
    popUpTo<TaskRoute> { inclusive = true }
}

fun NavGraphBuilder.taskScreen(onBack: () -> Unit, onTaskCreated: (sprintId: Long, taskId: Long) -> Unit) {
    composable<TaskRoute> {
        TaskRoute(onBack = onBack, onTaskCreated = onTaskCreated)
    }
}
