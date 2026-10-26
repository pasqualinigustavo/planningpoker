package com.planningpoker.feature.tasks.editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.planningpoker.core.data.repository.TaskSaveOutcome
import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import com.planningpoker.feature.tasks.testing.FakeTaskRepository
import com.planningpoker.feature.tasks.testing.MainDispatcherRule
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedTask = Task(
        id = 42,
        sprintId = 7,
        title = "Biometric login",
        description = "Add biometric login",
        userVote = Vote.FIVE,
        aiVote = Vote.EIGHT,
        risks = listOf("Device support"),
        evaluatedAt = Instant.EPOCH,
    )
    private val repository = FakeTaskRepository(listOf(savedTask))

    private fun newTaskViewModel(extras: Map<String, Any?> = emptyMap()) =
        TaskViewModel(SavedStateHandle(mapOf<String, Any?>("sprintId" to 7L, "taskId" to null) + extras), repository)

    private fun savedTaskViewModel(extras: Map<String, Any?> = emptyMap()) =
        TaskViewModel(SavedStateHandle(mapOf<String, Any?>("sprintId" to 7L, "taskId" to 42L) + extras), repository)

    @Test
    fun evaluateRequiresDescriptionAndVote() {
        val viewModel = newTaskViewModel()
        assertFalse(viewModel.canEvaluate)

        viewModel.onDescriptionChange("Login screen")
        assertFalse("vote missing", viewModel.canEvaluate)

        viewModel.onDescriptionChange("   ")
        viewModel.onVoteSelect(Vote.FIVE)
        assertFalse("description blank", viewModel.canEvaluate)

        viewModel.onDescriptionChange("Login screen")
        assertTrue(viewModel.canEvaluate)
    }

    @Test
    fun unknownVoteCanBeSelected() {
        val viewModel = newTaskViewModel()

        viewModel.onVoteSelect(Vote.THREE)
        viewModel.onVoteSelect(Vote.UNKNOWN)

        assertEquals(Vote.UNKNOWN, viewModel.selectedVote)
    }

    @Test
    fun successfulFirstEvaluationNavigatesToSavedTask() = runTest {
        val viewModel = newTaskViewModel()
        viewModel.onDescriptionChange("Login screen")
        viewModel.onVoteSelect(Vote.THREE)

        viewModel.events.test {
            viewModel.onEvaluateClick()
            assertTrue(viewModel.isEvaluating)

            repository.pendingOutcome.complete(TaskSaveOutcome.Saved(99))

            assertEquals(TaskEvent.TaskCreated(sprintId = 7, taskId = 99), awaitItem())
            assertFalse(viewModel.isEvaluating)
        }
        assertEquals(listOf(FakeTaskRepository.Call("create", 7, "Login screen", Vote.THREE)), repository.calls)
    }

    @Test
    fun failureShowsErrorAndKeepsDraft() = runTest {
        val viewModel = newTaskViewModel()
        viewModel.onDescriptionChange("Login screen")
        viewModel.onVoteSelect(Vote.THREE)

        viewModel.events.test {
            viewModel.onEvaluateClick()
            repository.pendingOutcome.complete(TaskSaveOutcome.Failed(EvaluationError.Network))

            assertEquals(TaskEvent.EvaluationFailed(EvaluationError.Network), awaitItem())
        }
        assertFalse(viewModel.isEvaluating)
        assertEquals("Login screen", viewModel.description)
        assertEquals(Vote.THREE, viewModel.selectedVote)
        assertTrue("can retry", viewModel.canEvaluate)
    }

    @Test
    fun inputsAndEvaluateAreLockedWhileEvaluating() {
        val viewModel = newTaskViewModel()
        viewModel.onDescriptionChange("Login screen")
        viewModel.onVoteSelect(Vote.THREE)

        viewModel.onEvaluateClick()
        viewModel.onEvaluateClick()
        viewModel.onDescriptionChange("Changed")
        viewModel.onVoteSelect(Vote.EIGHT)
        viewModel.onDictationResult("more")

        assertEquals("only one request", 1, repository.calls.size)
        assertFalse(viewModel.canEvaluate)
        assertEquals("Login screen", viewModel.description)
        assertEquals(Vote.THREE, viewModel.selectedVote)
    }

    @Test
    fun draftIsWrittenToSavedStateHandle() {
        val handle = SavedStateHandle(mapOf("sprintId" to 7L, "taskId" to null))
        val viewModel = TaskViewModel(handle, repository)

        viewModel.onDescriptionChange("Login screen")
        viewModel.onVoteSelect(Vote.UNKNOWN)

        assertEquals("Login screen", handle.get<String>(TaskViewModel.KEY_DESCRIPTION))
        assertEquals(Vote.UNKNOWN.name, handle.get<String>(TaskViewModel.KEY_VOTE))
    }

    @Test
    fun draftIsRestoredFromSavedStateHandle() {
        val viewModel = newTaskViewModel(
            mapOf(TaskViewModel.KEY_DESCRIPTION to "Restored", TaskViewModel.KEY_VOTE to Vote.EIGHT.name),
        )

        assertEquals("Restored", viewModel.description)
        assertEquals(Vote.EIGHT, viewModel.selectedVote)
        assertFalse("evaluation never resumes after process death", viewModel.isEvaluating)
    }

    @Test
    fun savedTaskLoadsItsValuesIntoTheDraft() {
        val viewModel = savedTaskViewModel()

        assertTrue(viewModel.isLoaded)
        assertEquals("Add biometric login", viewModel.description)
        assertEquals(Vote.FIVE, viewModel.selectedVote)
        assertEquals(savedTask, viewModel.savedTask.value)
    }

    @Test
    fun restoredEditsWinOverDatabaseValues() {
        val viewModel = savedTaskViewModel(
            mapOf(
                TaskViewModel.KEY_DESCRIPTION to "Edited before process death",
                TaskViewModel.KEY_VOTE to Vote.TWENTY.name,
            ),
        )

        assertEquals("Edited before process death", viewModel.description)
        assertEquals(Vote.TWENTY, viewModel.selectedVote)
    }

    @Test
    fun reevaluationSendsEditsAndDoesNotNavigate() = runTest {
        val viewModel = savedTaskViewModel()
        viewModel.onDescriptionChange("Add biometric login with PIN fallback")
        viewModel.onVoteSelect(Vote.THIRTEEN)

        viewModel.events.test {
            viewModel.onEvaluateClick()
            repository.pendingOutcome.complete(TaskSaveOutcome.Saved(42))
            expectNoEvents()
        }
        assertEquals(
            listOf(FakeTaskRepository.Call("reevaluate", 42, "Add biometric login with PIN fallback", Vote.THIRTEEN)),
            repository.calls,
        )
        assertFalse(viewModel.isEvaluating)
    }

    @Test
    fun missingSavedTaskNavigatesBack() = runTest {
        val viewModel = TaskViewModel(SavedStateHandle(mapOf("sprintId" to 7L, "taskId" to 999L)), repository)

        viewModel.events.test {
            assertEquals(TaskEvent.TaskMissing, awaitItem())
        }
    }

    @Test
    fun dictationIsAppendedWithASingleSpace() {
        assertEquals("Login screen with biometric support", appendDictation("Login screen", "with biometric support"))
        assertEquals(
            "Login screen with biometric support",
            appendDictation("Login screen  ", " with biometric support "),
        )
        assertEquals("Fresh text", appendDictation("", "Fresh text"))
        assertEquals("Fresh text", appendDictation("   ", "Fresh text"))
        assertEquals("Unchanged", appendDictation("Unchanged", "   "))
    }
}
