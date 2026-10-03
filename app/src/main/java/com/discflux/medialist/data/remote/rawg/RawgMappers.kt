package com.discflux.medialist.data.remote.rawg

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.model.MediaSearchResult

private const val MAX_SCREENSHOTS = 6

fun RawgGameDto.toSearchResult(now: Long = System.currentTimeMillis()): MediaSearchResult =
    MediaSearchResult(
        remoteId = id.toString(),
        item = MediaItem(
            type = MediaCategory.GAME,
            title = name,
            author = developers.firstOrNull()?.name,
            date = now,
            url = slug?.let { RawgApi.WEB_BASE_URL + it }.orEmpty(),
            description = descriptionRaw.orEmpty(),
            yearOfRelease = released?.take(4)?.toIntOrNull(),
            imageUrl = backgroundImage,
            publisher = publishers.firstOrNull()?.name,
            genre = genres.joinToString(", ") { it.name }.ifBlank { null },
            // MediaEntity stores screenshots comma-separated, so URLs containing commas are dropped.
            screenshots = shortScreenshots.map { it.image }
                .filter { it.isNotBlank() && !it.contains(',') }
                .take(MAX_SCREENSHOTS)
        )
    )
