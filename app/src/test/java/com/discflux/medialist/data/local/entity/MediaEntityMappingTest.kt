package com.discflux.medialist.data.local.entity

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaEntityMappingTest {

    @Test
    fun `round trip preserves every TV field`() {
        val item = MediaItem(
            id = 3, type = MediaCategory.TV_SERIES, title = "Show",
            currentSeason = 2, currentEpisode = 4, totalSeasons = 5, totalEpisodes = 50,
            cast = listOf("A", "B"), position = 9
        )
        assertEquals(item, item.toEntity().toDomain())
    }

    @Test
    fun `round trip preserves book and game fields`() {
        val book = MediaItem(
            id = 1, type = MediaCategory.BOOK, title = "B", currentPage = 10, totalPages = 300,
            publisher = "P", currentChapter = "3"
        )
        val game = MediaItem(
            id = 2, type = MediaCategory.GAME, title = "G", hoursPlayed = 1.5f,
            screenshots = listOf("a", "b"), genre = "RPG"
        )
        assertEquals(book, book.toEntity().toDomain())
        assertEquals(game, game.toEntity().toDomain())
    }

    @Test
    fun `unknown type code falls back to film`() {
        assertEquals(MediaCategory.FILM, MediaEntity(type = 999).toDomain().type)
    }
}
