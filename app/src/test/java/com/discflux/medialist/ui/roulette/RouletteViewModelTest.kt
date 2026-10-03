package com.discflux.medialist.ui.roulette

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.usecase.GetMediaSuggestionsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouletteViewModelTest {

    private val useCase: GetMediaSuggestionsUseCase = mockk()
    private val dispatcher = StandardTestDispatcher()
    private val film = MediaItem(id = 1, type = MediaCategory.FILM, title = "Film")

    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `spin shows rolling, then the pick`() = runTest(dispatcher) {
        coEvery { useCase(any()) } returns film
        val viewModel = RouletteViewModel(useCase)

        viewModel.roll()
        advanceTimeBy(RouletteViewModel.SPIN_DURATION_MS / 2)
        assertTrue(viewModel.uiState.value.isRolling)

        advanceUntilIdle()
        with(viewModel.uiState.value) {
            assertFalse(isRolling)
            assertEquals(film, suggestion)
            assertFalse(isNothingToPick)
        }
    }

    @Test
    fun `empty library stops spinning and reports nothing to pick`() = runTest(dispatcher) {
        coEvery { useCase(any()) } returns null
        val viewModel = RouletteViewModel(useCase)

        viewModel.roll()
        advanceUntilIdle()

        with(viewModel.uiState.value) {
            assertFalse(isRolling)
            assertNull(suggestion)
            assertTrue(isNothingToPick)
        }
    }

    @Test
    fun `not spun yet is not nothing-to-pick`() {
        assertFalse(RouletteViewModel(useCase).uiState.value.isNothingToPick)
    }

    @Test
    fun `spin again excludes the previous pick`() = runTest(dispatcher) {
        coEvery { useCase(null) } returns film
        coEvery { useCase(1L) } returns MediaItem(id = 2, type = MediaCategory.BOOK, title = "Book")
        val viewModel = RouletteViewModel(useCase)

        viewModel.roll(); advanceUntilIdle()
        viewModel.roll(); advanceUntilIdle()

        assertEquals(2L, viewModel.uiState.value.suggestion?.id)
    }

    @Test
    fun `a second tap while rolling is ignored`() = runTest(dispatcher) {
        var calls = 0
        coEvery { useCase(any()) } answers { calls++; film }
        val viewModel = RouletteViewModel(useCase)

        viewModel.roll(); viewModel.roll(); advanceUntilIdle()

        assertEquals(1, calls)
    }
}
