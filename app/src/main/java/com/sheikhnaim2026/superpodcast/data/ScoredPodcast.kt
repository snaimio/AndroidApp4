package com.sheikhnaim2026.superpodcast.data

// Wrapper class that pairs a Podcast with its computed mood match score
data class ScoredPodcast(
    val podcast: Podcast,
    val matchPercentage: Int
)
