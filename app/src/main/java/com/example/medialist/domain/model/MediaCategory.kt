package com.example.medialist.domain.model

enum class MediaCategory(val code: Int) {
    FILM(100),
    TV_SERIES(200),
    BOOK(300),
    GAME(400),
    MUSIC(500);

    val displayName: String
        get() = when (this) {
            FILM -> "Film"
            TV_SERIES -> "TV Series"
            BOOK -> "Book"
            GAME -> "Game"
            MUSIC -> "Music"
        }

    companion object {
        fun fromCode(code: Int): MediaCategory {
            return entries.find { it.code == code } ?: FILM
        }
    }
}
