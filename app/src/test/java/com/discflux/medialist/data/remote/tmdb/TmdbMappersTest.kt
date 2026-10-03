package com.discflux.medialist.data.remote.tmdb

import com.discflux.medialist.domain.model.MediaCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `movie search page parses and maps to film items`() {
        val page = json.decodeFromString<TmdbPage<TmdbMovieDto>>(
            """
            {"page":1,"results":[
              {"id":27205,"title":"Inception","overview":"Dreams.","release_date":"2010-07-15",
               "poster_path":"/abc.jpg","vote_average":8.4,"adult":false},
              {"id":2,"title":"No Poster","overview":"","release_date":"","poster_path":null}
            ],"total_pages":1}
            """
        )

        val results = page.results.map { it.toSearchResult(now = 123L) }

        with(results[0]) {
            assertEquals("27205", remoteId)
            assertEquals(MediaCategory.FILM, item.type)
            assertEquals("Inception", item.title)
            assertEquals(2010, item.yearOfRelease)
            assertEquals("https://image.tmdb.org/t/p/w500/abc.jpg", item.imageUrl)
            assertEquals("https://www.themoviedb.org/movie/27205", item.url)
            assertEquals(123L, item.date)
            assertEquals(0L, item.id)
            assertEquals(0f, item.rating)
        }
        assertNull(results[1].item.imageUrl)
        assertNull(results[1].item.yearOfRelease)
    }

    @Test
    fun `movie details map director, cast, runtime and genres`() {
        val movie = json.decodeFromString<TmdbMovieDto>(
            """
            {"id":27205,"title":"Inception","overview":"Dreams.","release_date":"2010-07-15","runtime":148,
             "genres":[{"id":28,"name":"Action"},{"id":878,"name":"Science Fiction"}],
             "credits":{"cast":[{"name":"Leonardo DiCaprio"},{"name":"Sammy Davis, Jr."}],
                        "crew":[{"name":"Hans Zimmer","job":"Original Music Composer"},
                                {"name":"Christopher Nolan","job":"Director"}]}}
            """
        )

        val item = movie.toSearchResult().item

        assertEquals("Christopher Nolan", item.director)
        assertEquals("Christopher Nolan", item.author)
        assertEquals(listOf("Leonardo DiCaprio", "Sammy Davis Jr."), item.cast)
        assertEquals(148, item.runtime)
        assertEquals("Action, Science Fiction", item.genre)
    }

    @Test
    fun `tv details map seasons, episodes and creator`() {
        val tv = json.decodeFromString<TmdbTvDto>(
            """
            {"id":76479,"name":"The Boys","overview":"Supes.","first_air_date":"2019-07-25",
             "number_of_seasons":4,"number_of_episodes":32,"created_by":[{"name":"Eric Kripke"}],
             "poster_path":"/p.jpg"}
            """
        )

        val result = tv.toSearchResult()

        assertEquals(MediaCategory.TV_SERIES, result.item.type)
        assertEquals("The Boys", result.item.title)
        assertEquals(2019, result.item.yearOfRelease)
        assertEquals(4, result.item.totalSeasons)
        assertEquals(32, result.item.totalEpisodes)
        assertEquals("Eric Kripke", result.item.author)
        assertEquals("https://www.themoviedb.org/tv/76479", result.item.url)
    }
}
