package com.sheikhnaim2026.superpodcast.data

import com.google.gson.annotations.SerializedName

// PodcastResponse: Represents the top-level JSON object returned by the iTunes Search API.
// Gson uses the @SerializedName annotations to map the JSON keys to our Kotlin properties.
data class PodcastResponse(
    // The total number of items found by iTunes for our search query
    @SerializedName("resultCount")
    val resultCount: Int = 0,

    // The list of podcast objects parsed from the "results" JSON array
    @SerializedName("results")
    val results: List<Podcast> = emptyList()
)

// Podcast: Represents a single podcast item from the iTunes results list.
// Fields are nullable (String?) with default values to prevent app crashes if iTunes omits certain keys.
data class Podcast(
    // The main title/name of the podcast show
    @SerializedName("collectionName")
    val collectionName: String? = null,

    // The author, network, or creator of the podcast
    @SerializedName("artistName")
    val artistName: String? = null,

    // The URL link for the 100x100 pixel artwork image
    @SerializedName("artworkUrl100")
    val artworkUrl100: String? = null,

    // The RSS feed URL for the podcast episodes
    @SerializedName("feedUrl")
    val feedUrl: String? = null,

    // Unique numeric identifier for the track or collection on iTunes
    @SerializedName("trackId")
    val trackId: Long = 0L,

    // Secondary episode or track title
    @SerializedName("trackName")
    val trackName: String? = null
)
