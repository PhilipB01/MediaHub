package com.discflux.medialist.data.remote.rawg

import com.discflux.medialist.domain.model.MediaCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RawgMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `search page maps to game items`() {
        val page = json.decodeFromString<RawgPage>(
            """
            {"count":2,"results":[
              {"id":3498,"slug":"grand-theft-auto-v","name":"Grand Theft Auto V","released":"2013-09-17",
               "background_image":"https://media.rawg.io/a.jpg","rating":4.47,
               "genres":[{"id":4,"name":"Action"},{"id":3,"name":"Adventure"}],
               "short_screenshots":[{"id":-1,"image":"https://media.rawg.io/b.jpg"},
                                    {"id":1,"image":"https://media.rawg.io/c,d.jpg"}]},
              {"id":2,"name":"Bare","released":null,"background_image":null}
            ]}
            """
        )

        val results = page.results.map { it.toSearchResult(now = 7L) }

        with(results[0]) {
            assertEquals("3498", remoteId)
            assertEquals(MediaCategory.GAME, item.type)
            assertEquals(2013, item.yearOfRelease)
            assertEquals("Action, Adventure", item.genre)
            assertEquals("https://rawg.io/games/grand-theft-auto-v", item.url)
            assertEquals(7L, item.date)
            // The URL containing a comma is dropped (screenshots are stored comma-joined).
            assertEquals(listOf("https://media.rawg.io/b.jpg"), item.screenshots)
        }
        assertNull(results[1].item.imageUrl)
        assertNull(results[1].item.yearOfRelease)
        assertEquals("", results[1].item.url)
    }

    @Test
    fun `details map description, developer and publisher`() {
        val game = json.decodeFromString<RawgGameDto>(
            """
            {"id":1,"name":"G","description_raw":"Plain description.",
             "developers":[{"name":"Dev Studio"}],"publishers":[{"name":"Big Publisher"},{"name":"Other"}]}
            """
        )

        val item = game.toSearchResult().item

        assertEquals("Plain description.", item.description)
        assertEquals("Dev Studio", item.author)
        assertEquals("Big Publisher", item.publisher)
    }
}
