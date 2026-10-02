package com.discflux.medialist.data.repository

import com.discflux.medialist.data.remote.tmdb.TmdbApi
import com.discflux.medialist.data.remote.tmdb.toSearchResult
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaSearchResult
import com.discflux.medialist.domain.repository.MediaSearchRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class MediaSearchRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi
) : MediaSearchRepository {

    override fun isSupported(category: MediaCategory): Boolean =
        category == MediaCategory.FILM || category == MediaCategory.TV_SERIES

    override suspend fun search(
        query: String,
        category: MediaCategory
    ): Result<List<MediaSearchResult>> = remoteCall {
        when (category) {
            MediaCategory.FILM -> tmdbApi.searchMovies(query).results.map { it.toSearchResult() }
            MediaCategory.TV_SERIES -> tmdbApi.searchTv(query).results.map { it.toSearchResult() }
            else -> emptyList()
        }
    }

    override suspend fun getDetails(result: MediaSearchResult): Result<MediaSearchResult> = remoteCall {
        when (result.item.type) {
            MediaCategory.FILM -> tmdbApi.getMovie(result.remoteId).toSearchResult(result.item.date)
            MediaCategory.TV_SERIES -> tmdbApi.getTv(result.remoteId).toSearchResult(result.item.date)
            else -> result
        }
    }

    private inline fun <T> remoteCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
