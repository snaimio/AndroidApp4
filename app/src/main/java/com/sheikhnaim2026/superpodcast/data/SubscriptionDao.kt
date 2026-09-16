package com.sheikhnaim2026.superpodcast.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * SubscriptionDao
 *
 * Data Access Object (DAO) providing database operations for the [SubscribedPodcast] table.
 */
@Dao
interface SubscriptionDao {

    /**
     * Inserts a podcast into local database subscriptions, replacing any existing entry with the same trackId.
     *
     * @param podcast The subscribed podcast entity to save.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(podcast: SubscribedPodcast)

    /**
     * Removes a subscribed podcast from the database.
     *
     * @param podcast The subscribed podcast entity to delete.
     */
    @Delete
    suspend fun delete(podcast: SubscribedPodcast)

    /**
     * Retrieves all subscribed podcasts ordered alphabetically by collection name (case-insensitive).
     * Returns a reactive [Flow] that automatically emits fresh lists whenever the database updates.
     */
    @Query("SELECT * FROM subscribed_podcasts ORDER BY collectionName COLLATE NOCASE ASC")
    fun getAll(): Flow<List<SubscribedPodcast>>

    /**
     * Finds a single subscribed podcast by its iTunes trackId.
     *
     * @param trackId The iTunes numeric identifier.
     * @return The [SubscribedPodcast] if subscribed, or null if not found.
     */
    @Query("SELECT * FROM subscribed_podcasts WHERE trackId = :trackId LIMIT 1")
    suspend fun getByTrackId(trackId: Long): SubscribedPodcast?

    /**
     * Checks if a podcast with the given trackId is currently subscribed.
     *
     * @param trackId The iTunes numeric identifier.
     * @return True if the subscription exists in Room, false otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM subscribed_podcasts WHERE trackId = :trackId)")
    suspend fun isSubscribed(trackId: Long): Boolean
}
