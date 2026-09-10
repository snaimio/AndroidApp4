package com.sheikhnaim2026.superpodcast.data

import retrofit2.http.GET
import retrofit2.http.Query

// ITunesApi: Defines the HTTP network endpoints for communicating with the Apple iTunes Search API.
// Retrofit automatically generates the network implementation based on these annotations.
interface ITunesApi {

    // Sends an HTTP GET request to the "search" endpoint on iTunes.
    // - term: The search keyword (for example, "relaxing" or "true crime").
    // - media: Defaults to "podcast" so iTunes only returns podcasts rather than music or movies.
    // - suspend: Marks this function as a Kotlin Coroutine so it can run asynchronously without blocking the UI thread.
    @GET("search")
    suspend fun searchPodcasts(
        @Query("term") term: String,
        @Query("media") media: String = "podcast"
    ): PodcastResponse
}
