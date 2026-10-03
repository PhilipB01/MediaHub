package com.discflux.medialist.di

import com.discflux.medialist.BuildConfig
import com.discflux.medialist.data.remote.openlibrary.OpenLibraryApi
import com.discflux.medialist.data.remote.rawg.RawgApi
import com.discflux.medialist.data.remote.tmdb.TmdbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * One shared [OkHttpClient] (connection pool and cache) with a small per-API customisation:
 * TMDB sends a bearer token, RAWG a `key` query parameter, Open Library a descriptive User-Agent.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val USER_AGENT = "MediaHub/1.0 (Android; https://github.com/PhilipB01/MediaHub)"

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideTmdbApi(client: OkHttpClient, json: Json): TmdbApi {
        val tmdbClient = client.newBuilder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer ${BuildConfig.TMDB_API_TOKEN}")
                        .header("Accept", "application/json")
                        .build()
                )
            }
            .build()
        return retrofit(TmdbApi.BASE_URL, tmdbClient, json).create(TmdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenLibraryApi(client: OkHttpClient, json: Json): OpenLibraryApi {
        val openLibraryClient = client.newBuilder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("User-Agent", USER_AGENT).build())
            }
            .build()
        return retrofit(OpenLibraryApi.BASE_URL, openLibraryClient, json).create(OpenLibraryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRawgApi(client: OkHttpClient, json: Json): RawgApi {
        val rawgClient = client.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.newBuilder().addQueryParameter("key", BuildConfig.RAWG_API_KEY).build()
                chain.proceed(request.newBuilder().url(url).build())
            }
            .build()
        return retrofit(RawgApi.BASE_URL, rawgClient, json).create(RawgApi::class.java)
    }

    /** Games search is only offered when a RAWG key has been configured. */
    @Provides
    @Named("rawgEnabled")
    fun provideRawgEnabled(): Boolean = BuildConfig.RAWG_API_KEY.isNotBlank()

    private fun retrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
