package com.discflux.medialist.data.repository

import com.discflux.medialist.data.remote.tmdb.TmdbApi
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
    private val repository = MediaSearchRepositoryImpl(api)

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
    fun `unsupported categories return no results without calling TMDB`() = runTest {
        assertFalse(repository.isSupported(MediaCategory.BOOK))
        assertTrue(repository.search("x", MediaCategory.BOOK).getOrThrow().isEmpty())
        coVerify(exactly = 0) { api.searchMovies(any()) }
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
