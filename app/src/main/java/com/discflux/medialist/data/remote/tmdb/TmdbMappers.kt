package com.discflux.medialist.data.remote.tmdb

import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.domain.model.MediaItem
import com.discflux.medialist.domain.model.MediaSearchResult

private const val MAX_CAST = 8

private fun String?.toYear(): Int? = this?.take(4)?.toIntOrNull()

private fun String?.toImageUrl(): String? =
    this?.takeIf { it.isNotBlank() }?.let { TmdbApi.IMAGE_BASE_URL + it }

private fun TmdbCreditsDto?.castNames(): List<String> =
    // MediaEntity stores cast comma separated, so names must not contain commas.
    this?.cast.orEmpty().take(MAX_CAST).map { it.name.replace(",", "") }.filter { it.isNotBlank() }

fun TmdbMovieDto.toSearchResult(now: Long = System.currentTimeMillis()): MediaSearchResult {
    val director = credits?.crew?.firstOrNull { it.job == "Director" }?.name
    return MediaSearchResult(
        remoteId = id.toString(),
        item = MediaItem(
            type = MediaCategory.FILM,
            title = title,
            author = director,
            date = now,
            url = "${TmdbApi.WEB_BASE_URL}/movie/$id",
            description = overview,
            director = director,
            yearOfRelease = releaseDate.toYear(),
            cast = credits.castNames(),
            imageUrl = posterPath.toImageUrl(),
            genre = genres.joinToString(", ") { it.name }.ifBlank { null },
            runtime = runtime?.takeIf { it > 0 }
        )
    )
}

fun TmdbTvDto.toSearchResult(now: Long = System.currentTimeMillis()): MediaSearchResult {
    val creator = createdBy.firstOrNull()?.name
    return MediaSearchResult(
        remoteId = id.toString(),
        item = MediaItem(
            type = MediaCategory.TV_SERIES,
            title = name,
            author = creator,
            date = now,
            url = "${TmdbApi.WEB_BASE_URL}/tv/$id",
            description = overview,
            director = creator,
            yearOfRelease = firstAirDate.toYear(),
            cast = credits.castNames(),
            imageUrl = posterPath.toImageUrl(),
            genre = genres.joinToString(", ") { it.name }.ifBlank { null },
            totalSeasons = numberOfSeasons,
            totalEpisodes = numberOfEpisodes
        )
    )
}
