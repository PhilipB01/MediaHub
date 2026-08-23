package com.discflux.medialist.data.repository

import com.discflux.medialist.data.local.dao.MediaDao
import com.discflux.medialist.data.local.entity.toDomain
import com.discflux.medialist.data.local.entity.toEntity
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val mediaDao: MediaDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : MediaRepository {

    private val userId: String? get() = auth.currentUser?.uid
    private val mediaCollection get() = userId?.let { firestore.collection("users").document(it).collection("media") }

    override fun getAllMedia(): Flow<List<MediaItem>> {
        return mediaDao.getAllMedia().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMediaByCategory(category: MediaCategory): Flow<List<MediaItem>> {
        return mediaDao.getMediaByType(category.code).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getMediaById(id: Long): MediaItem? {
        return mediaDao.getMediaById(id)?.toDomain()
    }

    override suspend fun upsertMedia(media: MediaItem) {
        val entity = media.toEntity()
        mediaDao.insertMedia(entity)
        
        // Sync to Firestore
        mediaCollection?.document(media.id.toString())?.set(entity)?.await()
    }

    override suspend fun updatePositions(items: List<MediaItem>) {
        val entities = items.map { it.toEntity() }
        mediaDao.insertMediaList(entities)
        
        // Batch sync to Firestore
        val batch = firestore.batch()
        entities.forEach { entity ->
            mediaCollection?.document(entity.id.toString())?.let { doc ->
                batch.set(doc, entity)
            }
        }
        batch.commit().await()
    }

    override suspend fun deleteMedia(media: MediaItem) {
        mediaDao.deleteMedia(media.toEntity())
        
        // Delete from Firestore
        mediaCollection?.document(media.id.toString())?.delete()?.await()
    }
}
