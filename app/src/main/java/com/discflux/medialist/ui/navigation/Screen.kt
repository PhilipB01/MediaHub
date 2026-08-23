package com.discflux.medialist.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object MediaList : Screen("media_list?category={category}") {
        fun createRoute(category: String? = null) = if (category != null) "media_list?category=$category" else "media_list"
    }
    data object Detail : Screen("detail/{mediaId}") {
        fun createRoute(mediaId: Long) = "detail/$mediaId"
    }
    data object Roulette : Screen("roulette")
    data object AddMedia : Screen("add_media/{category}") {
        fun createRoute(category: String) = "add_media/$category"
    }
}
