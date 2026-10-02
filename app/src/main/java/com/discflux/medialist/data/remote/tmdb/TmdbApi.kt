package com.discflux.medialist.data.remote.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovies(@Query("query") query: String): TmdbPage<TmdbMovieDto>

    @GET("search/tv")
    suspend fun searchTv(@Query("query") query: String): TmdbPage<TmdbTvDto>

    @GET("movie/{id}?append_to_response=credits")
    suspend fun getMovie(@Path("id") id: Long): TmdbMovieDto

    @GET("tv/{id}?append_to_response=credits")
    suspend fun getTv(@Path("id") id: Long): TmdbTvDto

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
        const val WEB_BASE_URL = "https://www.themoviedb.org"
    }
}
