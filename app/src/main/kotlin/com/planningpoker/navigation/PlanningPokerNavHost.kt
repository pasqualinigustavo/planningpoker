package com.planningpoker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.planningpoker.feature.sprints.navigation.SprintsRoute
import com.planningpoker.feature.sprints.navigation.navigateToSprintDetails
import com.planningpoker.feature.sprints.navigation.sprintDetailsScreen
import com.planningpoker.feature.sprints.navigation.sprintsScreen

@Composable
fun PlanningPokerNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SprintsRoute,
        modifier = modifier,
    ) {
        sprintsScreen(onSprintClick = navController::navigateToSprintDetails)
        sprintDetailsScreen(onBack = navController::popBackStack)
    }
}
