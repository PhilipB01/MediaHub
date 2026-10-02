package com.discflux.medialist.data.repository

import com.discflux.medialist.data.local.dao.MediaDao
import com.discflux.medialist.data.local.entity.MediaEntity
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaRepositoryImplTest {

    private val mediaDao: MediaDao = mockk(relaxUnitFun = true)
    private val firestore: FirebaseFirestore = mockk()
    private val auth: FirebaseAuth = mockk()
    private val repository = MediaRepositoryImpl(mediaDao, firestore, auth)

    private val collection: CollectionReference = mockk()
    private val document: DocumentReference = mockk(relaxed = true)

    // Task.forResult/forException need a Looper, which plain JVM tests don't have.
    private fun <T> completedTask(value: T?): Task<T> = mockk(relaxed = true) {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { exception } returns null
        every { result } returns value
    }

    private fun <T> failedTask(error: Exception): Task<T> = mockk(relaxed = true) {
        every { isComplete } returns true
        every { isCanceled } returns false
        every { exception } returns error
    }

    private fun signedInAs(uid: String?) {
        val user = uid?.let { mockk<FirebaseUser> { every { this@mockk.uid } returns it } }
        every { auth.currentUser } returns user
        if (uid != null) {
            every { firestore.collection("users").document(uid).collection("media") } returns collection
        }
    }

    @Test
    fun `getMediaById returns domain item`() = runTest {
        coEvery { mediaDao.getMediaById(1) } returns
            MediaEntity(id = 1, type = MediaCategory.FILM.code, title = "Test")

        val result = repository.getMediaById(1)

        assertEquals(1L, result?.id)
        assertEquals("Test", result?.title)
    }

    @Test
    fun `getMediaById returns null when missing`() = runTest {
        coEvery { mediaDao.getMediaById(9) } returns null
        assertNull(repository.getMediaById(9))
    }

    @Test
    fun `upsert of new item syncs under the Room generated id`() = runTest {
        signedInAs("uid")
        coEvery { mediaDao.insertMedia(any()) } returns 42L
        every { collection.document("42") } returns document
        val synced = slot<MediaEntity>()
        every { document.set(capture(synced)) } returns completedTask(null)

        repository.upsertMedia(MediaItem(id = 0, type = MediaCategory.BOOK, title = "New"))

        verify { collection.document("42") }
        assertEquals(42L, synced.captured.id)
        assertEquals("New", synced.captured.title)
    }

    @Test
    fun `upsert of existing item keeps its id`() = runTest {
        signedInAs("uid")
        coEvery { mediaDao.insertMedia(any()) } returns 7L
        every { collection.document("7") } returns document
        every { document.set(any()) } returns completedTask(null)

        repository.upsertMedia(MediaItem(id = 7, type = MediaCategory.GAME, title = "Old"))

        verify { collection.document("7") }
    }

    @Test
    fun `upsert is local only when signed out`() = runTest {
        signedInAs(null)
        coEvery { mediaDao.insertMedia(any()) } returns 1L

        repository.upsertMedia(MediaItem(type = MediaCategory.FILM, title = "Local"))

        coVerify(exactly = 1) { mediaDao.insertMedia(any()) }
        verify(exactly = 0) { firestore.collection(any()) }
    }

    @Test
    fun `upsert does not wait for the server (offline safe)`() = runTest {
        signedInAs("uid")
        coEvery { mediaDao.insertMedia(any()) } returns 1L
        every { collection.document("1") } returns document
        // A task that never completes, like a Firestore write while offline.
        every { document.set(any()) } returns mockk<Task<Void>>(relaxed = true) // never completes

        repository.upsertMedia(MediaItem(type = MediaCategory.FILM, title = "Offline"))
        // Reaching here means upsertMedia returned instead of hanging.
    }

    @Test
    fun `updatePositions skips remote when signed out`() = runTest {
        signedInAs(null)

        repository.updatePositions(listOf(MediaItem(id = 1, type = MediaCategory.FILM, position = 0)))

        coVerify { mediaDao.insertMediaList(any()) }
        verify(exactly = 0) { firestore.batch() }
    }

    @Test
    fun `fetchFromRemote skips documents without a valid id`() = runTest {
        signedInAs("uid")
        fun doc(docId: String, entity: MediaEntity?) = mockk<DocumentSnapshot> {
            every { id } returns docId
            every { toObject(MediaEntity::class.java) } returns entity
        }
        val snapshot = mockk<QuerySnapshot> {
            every { documents } returns listOf(
                doc("0", MediaEntity(title = "legacy bad doc")),
                doc("abc", MediaEntity(title = "non numeric")),
                doc("5", MediaEntity(id = 0, title = "good"))
            )
        }
        every { collection.get() } returns completedTask(snapshot)
        val inserted = slot<List<MediaEntity>>()
        coEvery { mediaDao.insertMediaList(capture(inserted)) } returns Unit

        repository.fetchFromRemote()

        assertEquals(listOf(5L), inserted.captured.map { it.id })
    }

    @Test
    fun `fetchFromRemote swallows network failures`() = runTest {
        signedInAs("uid")
        every { collection.get() } returns failedTask(RuntimeException("offline"))

        repository.fetchFromRemote() // must not throw
    }

    @Test
    fun `fetchFromRemote does nothing when signed out`() = runTest {
        signedInAs(null)
        repository.fetchFromRemote()
        coVerify(exactly = 0) { mediaDao.insertMediaList(any()) }
    }
}
