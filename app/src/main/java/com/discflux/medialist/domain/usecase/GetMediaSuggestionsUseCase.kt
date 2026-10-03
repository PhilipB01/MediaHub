package com.discflux.medialist.domain.usecase

import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetMediaSuggestionsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    /**
     * Picks a random item that is neither completed nor on the wishlist. When [excludeId] is given
     * (the previous pick), a different item is chosen unless it is the only candidate.
     */
    suspend operator fun invoke(excludeId: Long? = null): MediaItem? {
        val allMedia = repository.getAllMedia().first()
        val available = allMedia.filter { !it.isComplete && !it.isWishlist }
        return available.filter { it.id != excludeId }.ifEmpty { available }.randomOrNull()
    }
}
