package com.sheikhnaim2026.superpodcast.data

// Model representing a mood category for filtering podcasts
data class Mood(
    val id: String,              // Short id for the mood (e.g. "cozy")
    val label: String,           // Text displayed on the UI chip
    val seedTerm: String,        // Search keyword sent to iTunes API
    val keywords: List<String>   // Words used to calculate the match percentage
)
