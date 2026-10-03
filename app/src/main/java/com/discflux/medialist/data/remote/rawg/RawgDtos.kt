package com.discflux.medialist.data.remote.rawg

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawgPage(
    val results: List<RawgGameDto> = emptyList()
)

// Search results and the detail response share this DTO; detail-only fields default to empty.
@Serializable
data class RawgGameDto(
    val id: Long,
    val name: String = "",
    val slug: String? = null,
    val released: String? = null,
    @SerialName("background_image") val backgroundImage: String? = null,
    val genres: List<RawgNamedDto> = emptyList(),
    @SerialName("short_screenshots") val shortScreenshots: List<RawgScreenshotDto> = emptyList(),
    @SerialName("description_raw") val descriptionRaw: String? = null,
    val developers: List<RawgNamedDto> = emptyList(),
    val publishers: List<RawgNamedDto> = emptyList()
)

@Serializable
data class RawgNamedDto(val name: String = "")

@Serializable
data class RawgScreenshotDto(val image: String = "")
