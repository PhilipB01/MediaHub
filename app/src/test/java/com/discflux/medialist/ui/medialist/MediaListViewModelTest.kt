package com.discflux.medialist.ui.medialist

import app.cash.turbine.test
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MediaListViewModelTest {

    private val repository: MediaRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits filtered items by category`() = runTest {
        val items = listOf(
            MediaItem(id = 1, title = "Movie", type = MediaCategory.FILM),
            MediaItem(id = 2, title = "Book", type = MediaCategory.BOOK)
        )
        every { repository.getAllMedia() } returns flowOf(items)

        val viewModel = MediaListViewModel(repository)
        
        viewModel.uiState.test {
            // Initial state from stateIn
            assertEquals(0, awaitItem().items.size)
            
            // State after combine runs
            val stateAfterCollect = awaitItem()
            assertEquals(items.size, stateAfterCollect.items.size)
            
            viewModel.onCategorySelect(MediaCategory.FILM)
            val state = awaitItem()
            assertEquals(1, state.items.size)
            assertEquals("Movie", state.items[0].title)
        }
    }

    @Test
    fun `uiState emits filtered items by status`() = runTest {
        val items = listOf(
            MediaItem(id = 1, title = "Wishlist", type = MediaCategory.FILM, isWishlist = true, isOwned = false),
            MediaItem(id = 2, title = "Ongoing", type = MediaCategory.FILM, isOwned = true)
        )
        every { repository.getAllMedia() } returns flowOf(items)

        val viewModel = MediaListViewModel(repository)
        
        viewModel.uiState.test {
            awaitItem() // Initial
            awaitItem() // All items
            
            viewModel.onStatusFilterToggle(StatusFilter.WISHLIST)
            val state = awaitItem()
            assertEquals(1, state.items.size)
            assertEquals("Wishlist", state.items[0].title)
        }
    }
}
