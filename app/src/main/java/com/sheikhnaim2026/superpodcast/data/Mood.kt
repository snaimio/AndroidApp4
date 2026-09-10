package com.sheikhnaim2026.superpodcast.data

// Mood: Represents a podcast mood vibe used to filter and score search results.
// Each mood contains a search keyword for iTunes plus related terms for client-side scoring.
data class Mood(
    // Unique identifier for the mood (e.g. "cozy", "gym", "crime")
    val id: String,

    // User-friendly title displayed on the UI chip (e.g. "☕ Rainy Loft")
    val label: String,

    // The primary search term sent to the iTunes Search API (e.g. "relaxing")
    val seedTerm: String,

    // Related keywords used by MoodScorer to calculate the match percentage
    val keywords: List<String>
)
