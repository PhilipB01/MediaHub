package com.discflux.medialist.domain.usecase

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GetMediaSuggestionsUseCaseTest {

    private val repository: MediaRepository = mockk()
    private val useCase = GetMediaSuggestionsUseCase(repository)

    @Test
    fun `invoke returns random available item`() = runTest {
        val item = MediaItem(id = 1, title = "Ongoing", type = MediaCategory.FILM, isComplete = false, isWishlist = false)
        every { repository.getAllMedia() } returns flowOf(listOf(item))

        val result = useCase()
        
        assertNotNull(result)
        assertEquals(1L, result?.id)
    }

    @Test
    fun `invoke returns null when no items available`() = runTest {
        val item = MediaItem(id = 1, title = "Completed", type = MediaCategory.FILM, isComplete = true)
        every { repository.getAllMedia() } returns flowOf(listOf(item))

        val result = useCase()
        
        assertNull(result)
    }

    @Test
    fun `excludes wishlist items`() = runTest {
        every { repository.getAllMedia() } returns flowOf(
            listOf(MediaItem(id = 1, title = "Wish", type = MediaCategory.FILM, isWishlist = true))
        )

        assertNull(useCase())
    }

    @Test
    fun `avoids the excluded id when another candidate exists`() = runTest {
        every { repository.getAllMedia() } returns flowOf(
            listOf(
                MediaItem(id = 1, title = "A", type = MediaCategory.FILM),
                MediaItem(id = 2, title = "B", type = MediaCategory.FILM)
            )
        )

        repeat(20) { assertEquals(2L, useCase(excludeId = 1)?.id) }
    }

    @Test
    fun `falls back to the excluded item when it is the only candidate`() = runTest {
        every { repository.getAllMedia() } returns flowOf(
            listOf(MediaItem(id = 1, title = "Only", type = MediaCategory.FILM))
        )

        assertEquals(1L, useCase(excludeId = 1)?.id)
    }
}
