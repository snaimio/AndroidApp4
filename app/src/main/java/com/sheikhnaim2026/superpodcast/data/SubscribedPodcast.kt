package com.sheikhnaim2026.superpodcast.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SubscribedPodcast
 *
 * Room database entity representing a podcast that the user has subscribed to.
 * Saved locally in SQLite so subscriptions persist across app restarts.
 *
 * @property trackId Unique numeric ID from the iTunes Search API, serving as the primary key.
 * @property collectionName The show title / album name of the podcast.
 * @property artistName Creator, host, or publishing network name.
 * @property artworkUrl100 URL pointing to the podcast's 100x100 thumbnail artwork.
 * @property feedUrl The direct RSS feed XML link used to fetch episodes and check for updates.
 */
@Entity(tableName = "subscribed_podcasts")
data class SubscribedPodcast(
    @PrimaryKey
    val trackId: Long,
    val collectionName: String,
    val artistName: String,
    val artworkUrl100: String,
    val feedUrl: String
)
