package com.planningpoker.feature.sprints.list

import com.planningpoker.core.model.Sprint

sealed interface SprintsUiState {
    data object Loading : SprintsUiState
    data object Empty : SprintsUiState
    data class Sprints(val sprints: List<Sprint>) : SprintsUiState
}

/** State of the create-sprint dialog while it is open. */
data class CreateSprintDialogState(val name: String) {
    val canSave: Boolean get() = name.isNotBlank()
}
