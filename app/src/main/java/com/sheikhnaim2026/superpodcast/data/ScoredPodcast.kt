package com.sheikhnaim2026.superpodcast.data

// ScoredPodcast: Wrapper data class that pairs a raw iTunes Podcast with its calculated mood match score.
// Keeps our client-side scoring logic separate from the raw API data model.
data class ScoredPodcast(
    // The original podcast object containing title, artist, artwork URL, etc.
    val podcast: Podcast,

    // The calculated match percentage (between 50% and 100%)
    val matchPercentage: Int
)
