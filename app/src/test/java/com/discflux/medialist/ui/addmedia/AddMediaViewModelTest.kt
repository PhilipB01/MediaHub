package com.discflux.medialist.ui.addmedia

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.model.MediaSearchResult
import com.discflux.medialist.domain.repository.MediaRepository
import com.discflux.medialist.domain.repository.MediaSearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AddMediaViewModelTest {

    private val repository: MediaRepository = mockk(relaxUnitFun = true)
    private val searchRepository: MediaSearchRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val hit = MediaSearchResult("1", MediaItem(type = MediaCategory.FILM, title = "Dune"))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { searchRepository.isSupported(any()) } answers {
            firstArg<MediaCategory>() == MediaCategory.FILM
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `typing debounces to a single search for the latest query`() = runTest(testDispatcher) {
        coEvery { searchRepository.search(any(), any()) } returns Result.success(listOf(hit))
        val viewModel = AddMediaViewModel(repository, searchRepository)

        viewModel.onSearchQueryChange("du", MediaCategory.FILM)
        viewModel.onSearchQueryChange("dun", MediaCategory.FILM)
        viewModel.onSearchQueryChange("dune", MediaCategory.FILM)
        advanceUntilIdle()

        coVerify(exactly = 1) { searchRepository.search("dune", MediaCategory.FILM) }
        coVerify(exactly = 0) { searchRepository.search("du", any()) }
        assertEquals(listOf(hit), viewModel.uiState.value.searchResults)
    }

    @Test
    fun `short queries and unsupported categories do not hit the network`() = runTest(testDispatcher) {
        val viewModel = AddMediaViewModel(repository, searchRepository)

        viewModel.onSearchQueryChange("d", MediaCategory.FILM)
        viewModel.onSearchQueryChange("the hobbit", MediaCategory.BOOK)
        advanceUntilIdle()

        coVerify(exactly = 0) { searchRepository.search(any(), any()) }
    }

    @Test
    fun `search failure shows an error`() = runTest(testDispatcher) {
        coEvery { searchRepository.search(any(), any()) } returns Result.failure(IOException())
        val viewModel = AddMediaViewModel(repository, searchRepository)

        viewModel.onSearchQueryChange("dune", MediaCategory.FILM)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.searchResults.isEmpty())
    }

    @Test
    fun `selecting a result loads its details`() = runTest(testDispatcher) {
        val detailed = hit.copy(item = hit.item.copy(runtime = 155))
        coEvery { searchRepository.getDetails(hit) } returns Result.success(detailed)
        val viewModel = AddMediaViewModel(repository, searchRepository)

        viewModel.onResultSelected(hit)
        assertEquals(hit.item, viewModel.uiState.value.selectedItem)
        advanceUntilIdle()

        assertEquals(155, viewModel.uiState.value.selectedItem?.runtime)
    }

    @Test
    fun `selection survives a details failure`() = runTest(testDispatcher) {
        coEvery { searchRepository.getDetails(hit) } returns Result.failure(IOException())
        val viewModel = AddMediaViewModel(repository, searchRepository)

        viewModel.onResultSelected(hit)
        advanceUntilIdle()

        assertEquals(hit.item, viewModel.uiState.value.selectedItem)
    }

    @Test
    fun `manual add uses the typed title and saves it`() = runTest(testDispatcher) {
        val viewModel = AddMediaViewModel(repository, searchRepository)
        viewModel.onSearchQueryChange("  The Hobbit ", MediaCategory.BOOK)

        viewModel.onAddManually(MediaCategory.BOOK)
        var completed = false
        viewModel.saveMedia { completed = true }
        advanceUntilIdle()

        assertEquals("The Hobbit", viewModel.uiState.value.selectedItem?.title)
        coVerify { repository.upsertMedia(match { it.title == "The Hobbit" && it.type == MediaCategory.BOOK }) }
        assertTrue(completed)
    }
}
