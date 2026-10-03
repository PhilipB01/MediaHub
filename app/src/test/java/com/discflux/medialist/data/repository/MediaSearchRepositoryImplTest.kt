package com.discflux.medialist.data.repository

import com.discflux.medialist.data.remote.openlibrary.OpenLibraryApi
import com.discflux.medialist.data.remote.openlibrary.OpenLibraryDocDto
import com.discflux.medialist.data.remote.openlibrary.OpenLibrarySearchResponse
import com.discflux.medialist.data.remote.openlibrary.OpenLibraryWorkDto
import com.discflux.medialist.data.remote.rawg.RawgApi
import com.discflux.medialist.data.remote.rawg.RawgGameDto
import com.discflux.medialist.data.remote.rawg.RawgNamedDto
import com.discflux.medialist.data.remote.rawg.RawgPage
import com.discflux.medialist.data.remote.rawg.RawgScreenshotDto
import com.discflux.medialist.data.remote.tmdb.TmdbApi
import kotlinx.serialization.json.JsonPrimitive
import com.discflux.medialist.data.remote.tmdb.TmdbMovieDto
import com.discflux.medialist.data.remote.tmdb.TmdbPage
import com.discflux.medialist.data.remote.tmdb.TmdbTvDto
import com.discflux.medialist.domain.model.MediaCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MediaSearchRepositoryImplTest {

    private val api: TmdbApi = mockk()
    private val openLibrary: OpenLibraryApi = mockk()
    private val rawg: RawgApi = mockk()
    private val repository = MediaSearchRepositoryImpl(api, openLibrary, rawg, rawgEnabled = true)

    @Test
    fun `film search uses the movie endpoint`() = runTest {
        coEvery { api.searchMovies("dune") } returns TmdbPage(listOf(TmdbMovieDto(id = 1, title = "Dune")))

        val results = repository.search("dune", MediaCategory.FILM).getOrThrow()

        assertEquals(listOf("Dune"), results.map { it.item.title })
        assertEquals(MediaCategory.FILM, results[0].item.type)
    }

    @Test
    fun `tv search uses the tv endpoint`() = runTest {
        coEvery { api.searchTv("dark") } returns TmdbPage(listOf(TmdbTvDto(id = 2, name = "Dark")))

        val results = repository.search("dark", MediaCategory.TV_SERIES).getOrThrow()

        assertEquals(MediaCategory.TV_SERIES, results[0].item.type)
    }

    @Test
    fun `music has no provider and makes no calls`() = runTest {
        assertFalse(repository.isSupported(MediaCategory.MUSIC))
        assertTrue(repository.search("x", MediaCategory.MUSIC).getOrThrow().isEmpty())
        coVerify(exactly = 0) { api.searchMovies(any()) }
    }

    @Test
    fun `book search uses Open Library`() = runTest {
        coEvery { openLibrary.searchBooks("hobbit") } returns OpenLibrarySearchResponse(
            listOf(OpenLibraryDocDto(key = "/works/OL1W", title = "The Hobbit", authorName = listOf("Tolkien")))
        )

        val results = repository.search("hobbit", MediaCategory.BOOK).getOrThrow()

        assertEquals("OL1W", results[0].remoteId)
        assertEquals(MediaCategory.BOOK, results[0].item.type)
        assertTrue(repository.isSupported(MediaCategory.BOOK))
    }

    @Test
    fun `book details add the work description`() = runTest {
        coEvery { openLibrary.searchBooks("hobbit") } returns OpenLibrarySearchResponse(
            listOf(OpenLibraryDocDto(key = "/works/OL1W", title = "The Hobbit"))
        )
        coEvery { openLibrary.getWork("OL1W") } returns OpenLibraryWorkDto(JsonPrimitive("A hobbit goes there."))
        val hit = repository.search("hobbit", MediaCategory.BOOK).getOrThrow().first()

        val detailed = repository.getDetails(hit).getOrThrow()

        assertEquals("A hobbit goes there.", detailed.item.synopsis)
        assertEquals("The Hobbit", detailed.item.title)
    }

    @Test
    fun `game search uses RAWG when a key is configured`() = runTest {
        coEvery { rawg.searchGames("elden") } returns RawgPage(listOf(RawgGameDto(id = 9, name = "Elden Ring")))

        val results = repository.search("elden", MediaCategory.GAME).getOrThrow()

        assertEquals("9", results[0].remoteId)
        assertTrue(repository.isSupported(MediaCategory.GAME))
    }

    @Test
    fun `game search is disabled without a RAWG key`() = runTest {
        val noKey = MediaSearchRepositoryImpl(api, openLibrary, rawg, rawgEnabled = false)

        assertFalse(noKey.isSupported(MediaCategory.GAME))
        assertTrue(noKey.search("elden", MediaCategory.GAME).getOrThrow().isEmpty())
        coVerify(exactly = 0) { rawg.searchGames(any()) }
    }

    @Test
    fun `game details keep screenshots from the search hit`() = runTest {
        coEvery { rawg.searchGames("elden") } returns RawgPage(
            listOf(RawgGameDto(id = 9, name = "Elden Ring", shortScreenshots = listOf(RawgScreenshotDto("https://x/1.jpg"))))
        )
        coEvery { rawg.getGame(9) } returns RawgGameDto(
            id = 9, name = "Elden Ring", descriptionRaw = "Rise.", developers = listOf(RawgNamedDto("FromSoftware"))
        )
        val hit = repository.search("elden", MediaCategory.GAME).getOrThrow().first()

        val detailed = repository.getDetails(hit).getOrThrow()

        assertEquals("Rise.", detailed.item.description)
        assertEquals("FromSoftware", detailed.item.author)
        assertEquals(listOf("https://x/1.jpg"), detailed.item.screenshots)
    }

    @Test
    fun `provider failures become failures for books and games too`() = runTest {
        coEvery { openLibrary.searchBooks(any()) } throws IOException()
        coEvery { rawg.searchGames(any()) } throws IOException()

        assertTrue(repository.search("x", MediaCategory.BOOK).isFailure)
        assertTrue(repository.search("x", MediaCategory.GAME).isFailure)
    }

    @Test
    fun `network errors become failures`() = runTest {
        coEvery { api.searchMovies(any()) } throws IOException("offline")

        assertTrue(repository.search("dune", MediaCategory.FILM).isFailure)
    }

    @Test
    fun `details keep the date from the search hit`() = runTest {
        coEvery { api.searchMovies("dune") } returns TmdbPage(listOf(TmdbMovieDto(id = 1, title = "Dune")))
        coEvery { api.getMovie(1) } returns TmdbMovieDto(id = 1, title = "Dune", runtime = 155)
        val hit = repository.search("dune", MediaCategory.FILM).getOrThrow().first()

        val detailed = repository.getDetails(hit).getOrThrow()

        assertEquals(155, detailed.item.runtime)
        assertEquals(hit.item.date, detailed.item.date)
    }
}
