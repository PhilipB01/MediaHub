package com.discflux.medialist.data.remote.openlibrary

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.model.MediaSearchResult
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** `/works/OL27448W` -> `OL27448W` */
fun String.toWorkId(): String = substringAfterLast('/')

fun OpenLibraryDocDto.toSearchResult(now: Long = System.currentTimeMillis()): MediaSearchResult {
    val workId = key.toWorkId()
    return MediaSearchResult(
        remoteId = workId,
        item = MediaItem(
            type = MediaCategory.BOOK,
            title = title,
            author = authorName.firstOrNull(),
            date = now,
            url = "https://openlibrary.org/works/$workId",
            yearOfRelease = firstPublishYear,
            totalPages = pagesMedian?.takeIf { it > 0 },
            publisher = publisher.firstOrNull(),
            imageUrl = coverId?.let { "${OpenLibraryApi.COVER_BASE_URL}$it-M.jpg" }
        )
    )
}

/** Adds the work's description (search results don't include one) to an existing hit. */
fun MediaSearchResult.withWork(work: OpenLibraryWorkDto): MediaSearchResult {
    val text = work.description.asText() ?: return this
    return copy(item = item.copy(description = text, synopsis = text))
}

private fun JsonElement?.asText(): String? = when (this) {
    is JsonPrimitive -> contentOrNull
    is JsonObject -> (this["value"] as? JsonPrimitive)?.contentOrNull
    else -> null
}?.trim()?.takeIf { it.isNotEmpty() }
