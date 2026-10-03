package com.discflux.medialist.data.remote.openlibrary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class OpenLibrarySearchResponse(
    val docs: List<OpenLibraryDocDto> = emptyList()
)

@Serializable
data class OpenLibraryDocDto(
    /** Work key, e.g. `/works/OL27448W`. */
    val key: String,
    val title: String = "",
    @SerialName("author_name") val authorName: List<String> = emptyList(),
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("cover_i") val coverId: Long? = null,
    @SerialName("number_of_pages_median") val pagesMedian: Int? = null,
    val publisher: List<String> = emptyList()
)

@Serializable
data class OpenLibraryWorkDto(
    /** Either a plain string or an object like `{"type": "/type/text", "value": "..."}`. */
    val description: JsonElement? = null
)
