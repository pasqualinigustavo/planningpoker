package com.planningpoker.feature.sprints.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.planningpoker.feature.sprints.details.SprintDetailsRoute
import kotlinx.serialization.Serializable

@Serializable
data class SprintDetailsRoute(val sprintId: Long)

fun NavController.navigateToSprintDetails(sprintId: Long) = navigate(SprintDetailsRoute(sprintId))

fun NavGraphBuilder.sprintDetailsScreen(onBack: () -> Unit) {
    composable<SprintDetailsRoute> {
        SprintDetailsRoute(onBack = onBack)
    }
}
