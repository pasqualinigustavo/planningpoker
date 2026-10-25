package com.planningpoker.feature.sprints.details

import com.planningpoker.core.model.Sprint

sealed interface SprintDetailsUiState {
    data object Loading : SprintDetailsUiState
    data class Success(val sprint: Sprint) : SprintDetailsUiState
}
