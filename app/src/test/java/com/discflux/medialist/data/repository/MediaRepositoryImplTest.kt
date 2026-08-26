package com.discflux.medialist.data.repository

import com.discflux.medialist.data.local.dao.MediaDao
import com.discflux.medialist.data.local.entity.MediaEntity
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MediaRepositoryImplTest {

    private val mediaDao: MediaDao = mockk()
    private val firestore: FirebaseFirestore = mockk()
    private val auth: FirebaseAuth = mockk()
    private val repository = MediaRepositoryImpl(mediaDao, firestore, auth)

    @Test
    fun `getMediaById returns domain item`() = runTest {
        val entity = MediaEntity(id = 1, type = MediaCategory.FILM.code, title = "Test")
        coEvery { mediaDao.getMediaById(1) } returns entity
        
        val result = repository.getMediaById(1)
        
        assertEquals(1L, result?.id)
        assertEquals("Test", result?.title)
    }
}
