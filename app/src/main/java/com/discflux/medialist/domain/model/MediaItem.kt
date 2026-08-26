package com.discflux.medialist.domain.model

data class MediaItem(
    val id: Long = 0,
    val type: MediaCategory,
    val title: String = "",
    val author: String? = null,
    val date: Long = 0, 
    val url: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val isComplete: Boolean = false,
    val isWishlist: Boolean = false,
    val isOwned: Boolean = true,
    
    // Type-specific details
    val director: String? = null,
    val yearOfRelease: Int? = null,
    val cast: List<String> = emptyList(),
    val imageUrl: String? = null,
    
    // TV Specific
    val currentSeason: Int? = null,
    val currentEpisode: Int? = null,
    val totalEpisodes: Int? = null,
    val totalSeasons: Int? = null,
    
    // Book Specific
    val publisher: String? = null,
    val synopsis: String? = null,
    val currentPage: Int? = null,
    val totalPages: Int? = null,
    val currentChapter: String? = null,
    
    // Game Specific
    val genre: String? = null,
    val hoursPlayed: Float? = null,
    val screenshots: List<String> = emptyList(),
    
    // Film/TV Specific
    val runtime: Int? = null, // in minutes
    
    // User data
    val userNotes: String? = null,
    
    // List ordering
    val position: Int = 0
) {
    val isInProgress: Boolean
        get() = !isComplete && (
            (currentSeason != null && currentSeason > 1) ||
            (currentEpisode != null && currentEpisode > 0) ||
            (currentPage != null && currentPage > 0) ||
            (hoursPlayed != null && hoursPlayed > 0)
        )
}
