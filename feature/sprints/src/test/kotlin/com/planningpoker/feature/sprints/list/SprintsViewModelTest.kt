package com.planningpoker.feature.sprints.list

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.planningpoker.feature.sprints.testing.FakeSprintRepository
import com.planningpoker.feature.sprints.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SprintsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSprintRepository()

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) = SprintsViewModel(handle, repository)

    @Test
    fun emptyThenListAfterCreate() = runTest {
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertEquals(SprintsUiState.Empty, awaitItem())

            viewModel.onCreateSprintClick()
            viewModel.onNameChange("Sprint 1")
            viewModel.onSaveClick()

            val state = awaitItem() as SprintsUiState.Sprints
            assertEquals(listOf("Sprint 1"), state.sprints.map { it.name })
        }
    }

    @Test
    fun saveClosesDialog() = runTest {
        val viewModel = viewModel()

        viewModel.onCreateSprintClick()
        viewModel.onNameChange("Sprint 1")
        viewModel.onSaveClick()

        assertNull(viewModel.createDialogState)
    }

    @Test
    fun dialogInputIsWrittenToSavedStateHandle() = runTest {
        val handle = SavedStateHandle()
        val viewModel = viewModel(handle)

        viewModel.onCreateSprintClick()
        viewModel.onNameChange("Sprint 7")

        assertEquals(true, handle.get<Boolean>(SprintsViewModel.KEY_DIALOG_OPEN))
        assertEquals("Sprint 7", handle.get<String>(SprintsViewModel.KEY_NAME))
    }

    @Test
    fun blankNameCannotBeSaved() = runTest {
        val viewModel = viewModel()

        viewModel.onCreateSprintClick()
        viewModel.onNameChange("   ")

        assertFalse(viewModel.createDialogState!!.canSave)

        viewModel.onSaveClick()

        assertEquals(CreateSprintDialogState("   "), viewModel.createDialogState)
        assertEquals(emptyList<Any>(), repository.observeSprints().first())
    }

    @Test
    fun dismissDiscardsInput() = runTest {
        val viewModel = viewModel()

        viewModel.onCreateSprintClick()
        viewModel.onNameChange("Sprint 1")
        viewModel.onDismissDialog()

        assertNull(viewModel.createDialogState)
        assertEquals(emptyList<Any>(), repository.observeSprints().first())

        viewModel.onCreateSprintClick()
        assertEquals(CreateSprintDialogState(""), viewModel.createDialogState)
    }

    @Test
    fun dialogStateIsRestoredFromSavedStateHandle() = runTest {
        val handle = SavedStateHandle(
            mapOf(
                SprintsViewModel.KEY_DIALOG_OPEN to true,
                SprintsViewModel.KEY_NAME to "Sprint 4",
            ),
        )

        val viewModel = viewModel(handle)

        assertEquals(CreateSprintDialogState("Sprint 4"), viewModel.createDialogState)
    }
}
