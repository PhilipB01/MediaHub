package com.discflux.medialist.data.repository

import android.util.Log
import com.discflux.medialist.data.local.dao.MediaDao
import com.discflux.medialist.data.local.entity.MediaEntity
import com.discflux.medialist.data.local.entity.toDomain
import com.discflux.medialist.data.local.entity.toEntity
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.repository.MediaRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
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
        // Room generates the id for new items (id == 0); use it so local and remote keys match.
        val rowId = mediaDao.insertMedia(media.toEntity())
        val entity = media.toEntity().let { if (it.id == 0L) it.copy(id = rowId) else it }

        // Firestore queues writes while offline, so don't block on the server ack.
        mediaCollection?.document(entity.id.toString())?.set(entity)?.logFailure("upsert")
    }

    override suspend fun updatePositions(items: List<MediaItem>) {
        val entities = items.map { it.toEntity() }
        mediaDao.insertMediaList(entities)

        val collection = mediaCollection ?: return
        val batch = firestore.batch()
        entities.forEach { batch.set(collection.document(it.id.toString()), it) }
        batch.commit().logFailure("updatePositions")
    }

    override suspend fun deleteMedia(media: MediaItem) {
        mediaDao.deleteMedia(media.toEntity())
        mediaCollection?.document(media.id.toString())?.delete()?.logFailure("delete")
    }

    override suspend fun fetchFromRemote() {
        val collection = mediaCollection ?: return
        try {
            val snapshot = collection.get().await()
            // Key by document id and skip anything without a valid id: inserting id 0
            // would make Room auto-generate a new row (a duplicate) on every fetch.
            val entities = snapshot.documents.mapNotNull { doc ->
                val id = doc.id.toLongOrNull()?.takeIf { it != 0L } ?: return@mapNotNull null
                doc.toObject(MediaEntity::class.java)?.copy(id = id)
            }
            if (entities.isNotEmpty()) {
                mediaDao.insertMediaList(entities)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "fetchFromRemote failed", e)
        }
    }

    private fun Task<*>.logFailure(operation: String) {
        addOnFailureListener { Log.w(TAG, "Firestore $operation failed", it) }
    }

    private companion object {
        const val TAG = "MediaRepository"
    }
}
