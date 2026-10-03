package com.discflux.medialist.data.repository

import com.discflux.medialist.data.remote.openlibrary.OpenLibraryApi
import com.discflux.medialist.data.remote.openlibrary.toSearchResult
import com.discflux.medialist.data.remote.openlibrary.withWork
import com.discflux.medialist.data.remote.rawg.RawgApi
import com.discflux.medialist.data.remote.rawg.toSearchResult
import com.discflux.medialist.data.remote.tmdb.TmdbApi
import com.discflux.medialist.data.remote.tmdb.toSearchResult
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaSearchResult
import com.discflux.medialist.domain.repository.MediaSearchRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Named

/**
 * Routes searches to the provider for each category:
 * films and TV to TMDB, books to Open Library, games to RAWG. Music has no provider yet.
 */
class MediaSearchRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val openLibraryApi: OpenLibraryApi,
    private val rawgApi: RawgApi,
    @Named("rawgEnabled") private val rawgEnabled: Boolean
) : MediaSearchRepository {

    override fun isSupported(category: MediaCategory): Boolean = when (category) {
        MediaCategory.FILM, MediaCategory.TV_SERIES, MediaCategory.BOOK -> true
        MediaCategory.GAME -> rawgEnabled // RAWG needs a key; without one, manual add only
        MediaCategory.MUSIC -> false
    }

    override suspend fun search(
        query: String,
        category: MediaCategory
    ): Result<List<MediaSearchResult>> = remoteCall {
        when (category) {
            MediaCategory.FILM -> tmdbApi.searchMovies(query).results.map { it.toSearchResult() }
            MediaCategory.TV_SERIES -> tmdbApi.searchTv(query).results.map { it.toSearchResult() }
            MediaCategory.BOOK -> openLibraryApi.searchBooks(query).docs.map { it.toSearchResult() }
            MediaCategory.GAME ->
                if (rawgEnabled) rawgApi.searchGames(query).results.map { it.toSearchResult() } else emptyList()
            MediaCategory.MUSIC -> emptyList()
        }
    }

    override suspend fun getDetails(result: MediaSearchResult): Result<MediaSearchResult> = remoteCall {
        when (result.item.type) {
            MediaCategory.FILM -> tmdbApi.getMovie(result.remoteId.toLong()).toSearchResult(result.item.date)
            MediaCategory.TV_SERIES -> tmdbApi.getTv(result.remoteId.toLong()).toSearchResult(result.item.date)
            MediaCategory.BOOK -> result.withWork(openLibraryApi.getWork(result.remoteId))
            MediaCategory.GAME -> {
                val detailed = rawgApi.getGame(result.remoteId.toLong()).toSearchResult(result.item.date)
                // The detail response has no screenshot list, so keep the ones from the search hit.
                detailed.copy(
                    item = detailed.item.copy(
                        screenshots = detailed.item.screenshots.ifEmpty { result.item.screenshots }
                    )
                )
            }
            MediaCategory.MUSIC -> result
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
