package com.example.medialist.domain.usecase

import com.example.medialist.domain.model.MediaItem
import com.example.medialist.domain.repository.MediaRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetMediaSuggestionsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(): MediaItem? {
        val allMedia = repository.getAllMedia().first()
        val availableMedia = allMedia.filter { !it.isComplete && !it.isWishlist }
        return if (availableMedia.isNotEmpty()) {
            availableMedia.random()
        } else {
            null
        }
    }
}
