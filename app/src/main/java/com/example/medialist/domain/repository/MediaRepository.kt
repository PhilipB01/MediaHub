package com.example.medialist.domain.repository

import com.example.medialist.domain.model.MediaCategory
import com.example.medialist.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAllMedia(): Flow<List<MediaItem>>
    fun getMediaByCategory(category: MediaCategory): Flow<List<MediaItem>>
    suspend fun getMediaById(id: Long): MediaItem?
    suspend fun upsertMedia(media: MediaItem)
    suspend fun updatePositions(items: List<MediaItem>)
    suspend fun deleteMedia(media: MediaItem)
}
