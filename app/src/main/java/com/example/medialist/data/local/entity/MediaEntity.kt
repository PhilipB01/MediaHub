package com.example.medialist.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medialist.domain.model.MediaCategory
import com.example.medialist.domain.model.MediaItem

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: Int,
    val title: String,
    val author: String?,
    val date: Long,
    val url: String,
    val description: String,
    val rating: Float,
    val isComplete: Boolean,
    val isWishlist: Boolean = false,
    val isOwned: Boolean = true,
    
    // Additional fields
    val director: String? = null,
    val yearOfRelease: Int? = null,
    val cast: String? = null, // Comma separated
    val imageUrl: String? = null,
    val currentSeason: Int? = null,
    val currentEpisode: Int? = null,
    val totalEpisodes: Int? = null,
    val totalSeasons: Int? = null,
    val publisher: String? = null,
    val synopsis: String? = null,
    val currentPage: Int? = null,
    val totalPages: Int? = null,
    val currentChapter: String? = null,
    val genre: String? = null,
    val hoursPlayed: Float? = null,
    val screenshots: String? = null, // Comma separated
    val runtime: Int? = null,
    val userNotes: String? = null,
    val position: Int = 0
)

fun MediaEntity.toDomain() = MediaItem(
    id = id,
    type = MediaCategory.fromCode(type),
    title = title,
    author = author,
    date = date,
    url = url,
    description = description,
    rating = rating,
    isComplete = isComplete,
    isWishlist = isWishlist,
    isOwned = isOwned,
    director = director,
    yearOfRelease = yearOfRelease,
    cast = cast?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
    imageUrl = imageUrl,
    currentSeason = currentSeason,
    currentEpisode = currentEpisode,
    totalEpisodes = totalEpisodes,
    totalSeasons = totalSeasons,
    publisher = publisher,
    synopsis = synopsis,
    currentPage = currentPage,
    totalPages = totalPages,
    currentChapter = currentChapter,
    genre = genre,
    hoursPlayed = hoursPlayed,
    screenshots = screenshots?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
    runtime = runtime,
    userNotes = userNotes,
    position = position
)

fun MediaItem.toEntity() = MediaEntity(
    id = id,
    type = type.code,
    title = title,
    author = author,
    date = date,
    url = url,
    description = description,
    rating = rating,
    isComplete = isComplete,
    isWishlist = isWishlist,
    isOwned = isOwned,
    director = director,
    yearOfRelease = yearOfRelease,
    cast = cast.joinToString(","),
    imageUrl = imageUrl,
    currentSeason = currentSeason,
    currentEpisode = currentEpisode,
    totalEpisodes = totalEpisodes,
    publisher = publisher,
    synopsis = synopsis,
    currentPage = currentPage,
    totalPages = totalPages,
    currentChapter = currentChapter,
    genre = genre,
    hoursPlayed = hoursPlayed,
    screenshots = screenshots.joinToString(","),
    runtime = runtime,
    userNotes = userNotes,
    position = position
)
