package com.sheikhnaim2026.superpodcast.data

import com.google.gson.annotations.SerializedName

// Top-level response from iTunes API
data class PodcastResponse(
    @SerializedName("resultCount")
    val resultCount: Int = 0,

    @SerializedName("results")
    val results: List<Podcast> = emptyList()
)

// Data class for each podcast returned in the results list
data class Podcast(
    @SerializedName("collectionName")
    val collectionName: String? = null,

    @SerializedName("artistName")
    val artistName: String? = null,

    @SerializedName("artworkUrl100")
    val artworkUrl100: String? = null,

    @SerializedName("feedUrl")
    val feedUrl: String? = null,

    @SerializedName("trackId")
    val trackId: Long = 0L,

    @SerializedName("trackName")
    val trackName: String? = null
)
