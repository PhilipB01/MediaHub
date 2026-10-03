package com.discflux.medialist.data.remote.openlibrary

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("fields") fields: String = SEARCH_FIELDS,
        @Query("limit") limit: Int = 20
    ): OpenLibrarySearchResponse

    /** `workId` is the bare id, e.g. `OL27448W`. */
    @GET("works/{workId}.json")
    suspend fun getWork(@Path("workId") workId: String): OpenLibraryWorkDto

    companion object {
        const val BASE_URL = "https://openlibrary.org/"
        const val COVER_BASE_URL = "https://covers.openlibrary.org/b/id/"
        const val SEARCH_FIELDS =
            "key,title,author_name,first_publish_year,cover_i,number_of_pages_median,publisher"
    }
}
