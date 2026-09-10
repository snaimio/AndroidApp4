package com.sheikhnaim2026.superpodcast.data

import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit interface to connect to the iTunes Search API
interface ITunesApi {

    // GET request to search for podcasts matching the term
    @GET("search")
    suspend fun searchPodcasts(
        @Query("term") term: String,
        @Query("media") media: String = "podcast"
    ): PodcastResponse
}
