package com.planningpoker.feature.sprints.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.planningpoker.core.model.Sprint
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import com.planningpoker.feature.sprints.testing.FakeSprintRepository
import com.planningpoker.feature.sprints.testing.FakeTaskRepository
import com.planningpoker.feature.sprints.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class SprintDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sprint = Sprint(id = 7, name = "Sprint 42", createdAt = Instant.EPOCH)
    private val repository = FakeSprintRepository(listOf(sprint))
    private val taskRepository = FakeTaskRepository()

    private fun task(id: Long, sprintId: Long = sprint.id) = Task(
        id = id,
        sprintId = sprintId,
        title = "Task $id",
        description = "Description $id",
        userVote = Vote.THREE,
        aiVote = Vote.FIVE,
        risks = emptyList(),
        evaluatedAt = Instant.EPOCH,
    )

    private fun viewModel(
        sprintId: Long = sprint.id,
        extras: Map<String, Any> = emptyMap(),
    ) = SprintDetailsViewModel(
        SavedStateHandle(mapOf("sprintId" to sprintId) + extras),
        repository,
        taskRepository,
    )

    @Test
    fun showsSprint() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(SprintDetailsUiState.Success(sprint, emptyList()), awaitItem())
        }
    }

    @Test
    fun showsOnlyThisSprintsTasksAndUpdatesWhenOneIsSaved() = runTest {
        taskRepository.tasks.value = listOf(task(1), task(2, sprintId = 99))
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(SprintDetailsUiState.Success(sprint, listOf(task(1))), awaitItem())

            taskRepository.tasks.value = taskRepository.tasks.value + task(3)

            assertEquals(SprintDetailsUiState.Success(sprint, listOf(task(1), task(3))), awaitItem())
        }
    }

    @Test
    fun confirmDeletesSprintAndNavigatesBack() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.navigateBack.test {
                viewModel.onDeleteClick()
                assertTrue(viewModel.isDeleteDialogOpen.value)

                viewModel.onConfirmDelete()

                awaitItem()
                assertFalse(viewModel.isDeleteDialogOpen.value)
                assertEquals(listOf(sprint.id), repository.deletedIds)
                assertEquals(emptyList<Sprint>(), repository.observeSprints().first())
            }
        }
    }

    @Test
    fun cancelKeepsSprint() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onDeleteClick()
            viewModel.onDismissDeleteDialog()

            assertFalse(viewModel.isDeleteDialogOpen.value)
            assertEquals(emptyList<Long>(), repository.deletedIds)
            expectNoEvents()
        }
    }

    @Test
    fun missingSprintNavigatesBack() = runTest {
        val viewModel = viewModel(sprintId = 999)

        viewModel.uiState.test {
            assertEquals(SprintDetailsUiState.Loading, awaitItem())
            viewModel.navigateBack.test { awaitItem() }
        }
    }

    @Test
    fun deleteDialogIsRestoredFromSavedStateHandle() = runTest {
        val viewModel = viewModel(extras = mapOf(SprintDetailsViewModel.KEY_DELETE_DIALOG_OPEN to true))

        assertTrue(viewModel.isDeleteDialogOpen.value)
    }
}
