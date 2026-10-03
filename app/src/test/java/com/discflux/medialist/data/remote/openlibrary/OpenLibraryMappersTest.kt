package com.discflux.medialist.data.remote.openlibrary

import com.discflux.medialist.domain.model.MediaCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenLibraryMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `search response maps to book items`() {
        val response = json.decodeFromString<OpenLibrarySearchResponse>(
            """
            {"numFound":2,"start":0,"docs":[
              {"key":"/works/OL27448W","title":"The Lord of the Rings","author_name":["J. R. R. Tolkien","Other"],
               "first_publish_year":1954,"cover_i":258027,"number_of_pages_median":1193,
               "publisher":["Allen & Unwin","Houghton"],"isbn":["x"]},
              {"key":"/works/OL2W","title":"No Cover"}
            ]}
            """
        )

        val results = response.docs.map { it.toSearchResult(now = 5L) }

        with(results[0]) {
            assertEquals("OL27448W", remoteId)
            assertEquals(MediaCategory.BOOK, item.type)
            assertEquals("J. R. R. Tolkien", item.author)
            assertEquals(1954, item.yearOfRelease)
            assertEquals(1193, item.totalPages)
            assertEquals("Allen & Unwin", item.publisher)
            assertEquals("https://covers.openlibrary.org/b/id/258027-M.jpg", item.imageUrl)
            assertEquals("https://openlibrary.org/works/OL27448W", item.url)
            assertEquals(5L, item.date)
        }
        assertNull(results[1].item.imageUrl)
        assertNull(results[1].item.author)
    }

    @Test
    fun `description may be a plain string`() {
        val work = json.decodeFromString<OpenLibraryWorkDto>("""{"description":"Plain text."}""")
        val hit = json.decodeFromString<OpenLibraryDocDto>("""{"key":"/works/OL1W","title":"T"}""").toSearchResult()

        assertEquals("Plain text.", hit.withWork(work).item.description)
    }

    @Test
    fun `description may be an object with a value`() {
        val work = json.decodeFromString<OpenLibraryWorkDto>(
            """{"description":{"type":"/type/text","value":"  Wrapped.  "}}"""
        )
        val hit = json.decodeFromString<OpenLibraryDocDto>("""{"key":"/works/OL1W","title":"T"}""").toSearchResult()

        val item = hit.withWork(work).item

        assertEquals("Wrapped.", item.description)
        assertEquals("Wrapped.", item.synopsis)
    }

    @Test
    fun `missing description leaves the hit unchanged`() {
        val work = json.decodeFromString<OpenLibraryWorkDto>("""{"title":"x"}""")
        val hit = json.decodeFromString<OpenLibraryDocDto>("""{"key":"/works/OL1W","title":"T"}""").toSearchResult()

        assertEquals(hit, hit.withWork(work))
    }
}
