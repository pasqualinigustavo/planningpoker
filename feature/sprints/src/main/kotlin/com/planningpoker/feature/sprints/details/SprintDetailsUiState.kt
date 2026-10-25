package com.planningpoker.feature.sprints.details

import com.planningpoker.core.model.Sprint
import com.planningpoker.core.model.Task

sealed interface SprintDetailsUiState {
    data object Loading : SprintDetailsUiState
    data class Success(val sprint: Sprint, val tasks: List<Task>) : SprintDetailsUiState
}
