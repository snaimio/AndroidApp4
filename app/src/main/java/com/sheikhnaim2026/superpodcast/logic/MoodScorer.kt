package com.sheikhnaim2026.superpodcast.logic

import com.sheikhnaim2026.superpodcast.data.Mood
import com.sheikhnaim2026.superpodcast.data.Podcast
import com.sheikhnaim2026.superpodcast.data.ScoredPodcast

// Helper object to score and sort podcasts based on mood keywords
object MoodScorer {

    // Calculate match percentage for one podcast against the mood's keywords
    fun score(podcast: Podcast, mood: Mood): Int {
        if (mood.keywords.isEmpty()) return 50

        // Combine title, track name, and artist into one lowercase string to search in
        val combinedText = buildString {
            append(podcast.collectionName.orEmpty())
            append(" ")
            append(podcast.trackName.orEmpty())
            append(" ")
            append(podcast.artistName.orEmpty())
        }.lowercase()

        // Count how many keywords appear in the podcast info
        var hits = 0
        for (word in mood.keywords) {
            if (combinedText.contains(word)) {
                hits++
            }
        }

        // Base score of 50% + bonus based on how many keywords matched
        val ratio = hits.toDouble() / mood.keywords.size.coerceAtLeast(1)
        val finalScore = (50 + (ratio * 50)).toInt().coerceIn(50, 100)
        return finalScore
    }

    // Scores all podcasts in the list and sorts them from highest to lowest score
    fun rank(podcasts: List<Podcast>, mood: Mood): List<ScoredPodcast> {
        return podcasts
            .map { podcast ->
                ScoredPodcast(
                    podcast = podcast,
                    matchPercentage = score(podcast, mood)
                )
            }
            .sortedByDescending { it.matchPercentage }
    }
}
