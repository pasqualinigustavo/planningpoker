package com.planningpoker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.planningpoker.feature.sprints.navigation.SprintsRoute
import com.planningpoker.feature.sprints.navigation.navigateToSprintDetails
import com.planningpoker.feature.sprints.navigation.sprintDetailsScreen
import com.planningpoker.feature.sprints.navigation.sprintsScreen
import com.planningpoker.feature.tasks.navigation.navigateToCreatedTask
import com.planningpoker.feature.tasks.navigation.navigateToNewTask
import com.planningpoker.feature.tasks.navigation.navigateToTask
import com.planningpoker.feature.tasks.navigation.taskScreen

@Composable
fun PlanningPokerNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SprintsRoute,
        modifier = modifier,
    ) {
        sprintsScreen(onSprintClick = navController::navigateToSprintDetails)
        // The app wires the two features together; they never depend on each other.
        sprintDetailsScreen(
            onBack = navController::popBackStack,
            onCreateTask = navController::navigateToNewTask,
            onTaskClick = navController::navigateToTask,
        )
        taskScreen(
            onBack = navController::popBackStack,
            onTaskCreated = navController::navigateToCreatedTask,
        )
    }
}
