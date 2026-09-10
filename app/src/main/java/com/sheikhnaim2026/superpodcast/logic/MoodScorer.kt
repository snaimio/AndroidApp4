package com.sheikhnaim2026.superpodcast.logic

import com.sheikhnaim2026.superpodcast.data.Mood
import com.sheikhnaim2026.superpodcast.data.Podcast
import com.sheikhnaim2026.superpodcast.data.ScoredPodcast

// MoodScorer: Custom logic engine for calculating how well a podcast matches a user's selected mood.
// Implements the assignment's "advanced/unusual search criteria" requirement by analyzing
// keyword occurrences across podcast titles, episode names, and artist descriptions.
object MoodScorer {

    // Calculates a 50% to 100% match score for an individual podcast.
    // - podcast: The raw podcast data returned by the iTunes API.
    // - mood: The active mood containing the list of related keywords.
    fun score(podcast: Podcast, mood: Mood): Int {
        // Default baseline score if no keywords are available
        if (mood.keywords.isEmpty()) return 50

        // Step 1: Concatenate all available text fields into a single lowercase string for scanning
        val combinedText = buildString {
            append(podcast.collectionName.orEmpty())
            append(" ")
            append(podcast.trackName.orEmpty())
            append(" ")
            append(podcast.artistName.orEmpty())
        }.lowercase()

        // Step 2: Count how many mood-related keywords appear in the podcast metadata
        var hits = 0
        for (word in mood.keywords) {
            if (combinedText.contains(word)) {
                hits++
            }
        }

        // Step 3: Compute the match percentage.
        // Since iTunes already returned this podcast based on our seed search, we give it a
        // 50% baseline score, and add up to 50% bonus points based on keyword hit density.
        val ratio = hits.toDouble() / mood.keywords.size.coerceAtLeast(1)
        val finalScore = (50 + (ratio * 50)).toInt().coerceIn(50, 100)
        return finalScore
    }

    // Takes a list of raw iTunes podcasts, scores each one against the mood,
    // and returns a list of ScoredPodcast objects sorted from highest match to lowest.
    fun rank(podcasts: List<Podcast>, mood: Mood): List<ScoredPodcast> {
        return podcasts
            .map { podcast ->
                // Wrap each podcast with its computed match percentage
                ScoredPodcast(
                    podcast = podcast,
                    matchPercentage = score(podcast, mood)
                )
            }
            // Sort descending so the strongest matches appear at the top of the RecyclerView
            .sortedByDescending { it.matchPercentage }
    }
}
