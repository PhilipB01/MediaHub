package com.discflux.medialist.data.remote.rawg

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** The API key is added to every request by an interceptor in `NetworkModule`. */
interface RawgApi {

    @GET("games")
    suspend fun searchGames(
        @Query("search") query: String,
        @Query("page_size") pageSize: Int = 20
    ): RawgPage

    @GET("games/{id}")
    suspend fun getGame(@Path("id") id: Long): RawgGameDto

    companion object {
        const val BASE_URL = "https://api.rawg.io/api/"
        const val WEB_BASE_URL = "https://rawg.io/games/"
    }
}
