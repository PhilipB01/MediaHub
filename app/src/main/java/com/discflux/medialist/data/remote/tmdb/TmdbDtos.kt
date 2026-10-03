package com.discflux.medialist.data.remote.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbPage<T>(
    val results: List<T> = emptyList()
)

// Search results and detail responses share these DTOs; detail-only fields default to null.
@Serializable
data class TmdbMovieDto(
    val id: Long,
    val title: String = "",
    val overview: String = "",
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    val runtime: Int? = null,
    val genres: List<TmdbGenreDto> = emptyList(),
    val credits: TmdbCreditsDto? = null
)

@Serializable
data class TmdbTvDto(
    val id: Long,
    val name: String = "",
    val overview: String = "",
    @SerialName("first_air_date") val firstAirDate: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int? = null,
    @SerialName("number_of_episodes") val numberOfEpisodes: Int? = null,
    @SerialName("created_by") val createdBy: List<TmdbPersonDto> = emptyList(),
    val genres: List<TmdbGenreDto> = emptyList(),
    val credits: TmdbCreditsDto? = null
)

@Serializable
data class TmdbGenreDto(val name: String = "")

@Serializable
data class TmdbPersonDto(val name: String = "")

@Serializable
data class TmdbCreditsDto(
    val cast: List<TmdbPersonDto> = emptyList(),
    val crew: List<TmdbCrewDto> = emptyList()
)

@Serializable
data class TmdbCrewDto(
    val name: String = "",
    val job: String = ""
)
