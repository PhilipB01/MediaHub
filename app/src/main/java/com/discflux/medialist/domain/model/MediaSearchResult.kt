package com.discflux.medialist.domain.model

/** A hit from an external metadata provider; [remoteId] is that provider's id for the title, as a string because providers use numbers, work keys and UUIDs. */
data class MediaSearchResult(
    val remoteId: String,
    val item: MediaItem
)
