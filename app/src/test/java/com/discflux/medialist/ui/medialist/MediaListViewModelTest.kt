package com.discflux.medialist.ui.medialist

import app.cash.turbine.test
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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

    @Test
    fun `manual sort orders by position within a status group`() = runTest {
        val items = listOf(
            MediaItem(id = 1, title = "C", type = MediaCategory.FILM, position = 2, date = 3),
            MediaItem(id = 2, title = "A", type = MediaCategory.FILM, position = 0, date = 1),
            MediaItem(id = 3, title = "B", type = MediaCategory.FILM, position = 1, date = 2)
        )
        every { repository.getAllMedia() } returns flowOf(items)

        val viewModel = MediaListViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            assertEquals(listOf("A", "B", "C"), awaitItem().items.map { it.title })
        }
    }

    @Test
    fun `date sort is newest first even for close epoch timestamps`() = runTest {
        val base = 1_700_000_000_000L
        val items = listOf(
            MediaItem(id = 1, title = "older", type = MediaCategory.FILM, date = base),
            MediaItem(id = 2, title = "newer", type = MediaCategory.FILM, date = base + 1)
        )
        every { repository.getAllMedia() } returns flowOf(items)

        val viewModel = MediaListViewModel(repository)
        viewModel.onSortByChange(SortOption.DATE_ADDED)

        viewModel.uiState.test {
            awaitItem()
            assertEquals(listOf("newer", "older"), awaitItem().items.map { it.title })
        }
    }

    @Test
    fun `moveItem persists renumbered positions`() = runTest {
        val items = listOf(
            MediaItem(id = 1, title = "A", type = MediaCategory.FILM, position = 0),
            MediaItem(id = 2, title = "B", type = MediaCategory.FILM, position = 1)
        )
        every { repository.getAllMedia() } returns flowOf(items)
        val saved = slot<List<MediaItem>>()
        coEvery { repository.updatePositions(capture(saved)) } returns Unit

        val viewModel = MediaListViewModel(repository)
        viewModel.uiState.test {
            awaitItem(); awaitItem()
            viewModel.moveItem(0, 1)
            testDispatcher.scheduler.advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(listOf(2L, 1L), saved.captured.map { it.id })
        assertEquals(listOf(0, 1), saved.captured.map { it.position })
    }

    @Test
    fun `dropping a film on ongoing keeps it where it was`() = runTest {
        val film = MediaItem(id = 1, title = "F", type = MediaCategory.FILM)
        every { repository.getAllMedia() } returns flowOf(listOf(film))
        coEvery { repository.getMediaById(1) } returns film
        val saved = slot<MediaItem>()
        coEvery { repository.upsertMedia(capture(saved)) } returns Unit

        MediaListViewModel(repository).updateItemStatus(1, StatusFilter.ONGOING)
        testDispatcher.scheduler.advanceUntilIdle()

        // Documents current behaviour: films have no progress fields, so ONGOING is a no-op.
        assertEquals(false, saved.captured.isInProgress)
    }
}
