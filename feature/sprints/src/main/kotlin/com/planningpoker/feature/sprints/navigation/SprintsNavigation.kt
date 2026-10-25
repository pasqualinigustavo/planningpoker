package com.planningpoker.feature.sprints.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.planningpoker.feature.sprints.list.SprintsRoute
import kotlinx.serialization.Serializable

@Serializable
data object SprintsRoute

fun NavGraphBuilder.sprintsScreen(onSprintClick: (Long) -> Unit) {
    composable<SprintsRoute> {
        SprintsRoute(onSprintClick = onSprintClick)
    }
}
