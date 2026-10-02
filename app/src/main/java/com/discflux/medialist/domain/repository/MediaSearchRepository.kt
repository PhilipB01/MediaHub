package com.discflux.medialist.domain.repository

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaSearchResult

interface MediaSearchRepository {
    /** Whether an online metadata provider is available for [category]. */
    fun isSupported(category: MediaCategory): Boolean

    suspend fun search(query: String, category: MediaCategory): Result<List<MediaSearchResult>>

    /** Fetches the full record (cast, runtime, season counts...) for a search hit. */
    suspend fun getDetails(result: MediaSearchResult): Result<MediaSearchResult>
}
