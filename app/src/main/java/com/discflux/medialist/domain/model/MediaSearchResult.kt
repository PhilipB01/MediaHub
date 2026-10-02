package com.discflux.medialist.domain.model

/** A hit from an external metadata provider; [remoteId] is that provider's id for the title. */
data class MediaSearchResult(
    val remoteId: Long,
    val item: MediaItem
)
