package com.sheikhnaim2026.superpodcast.data

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sheikhnaim2026.superpodcast.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * PodcastUpdateWorker
 *
 * Background [CoroutineWorker] that iterates through all subscribed podcasts, fetches their RSS feeds,
 * compares the latest episode GUID against local SharedPreferences, and triggers alerts when a new episode is released.
 */
class PodcastUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val ACTION_NEW_EPISODE = "com.sheikhnaim2026.superpodcast.ACTION_NEW_EPISODE"
        const val EXTRA_PODCAST_TITLE = "com.sheikhnaim2026.superpodcast.EXTRA_PODCAST_TITLE"
        const val EXTRA_EPISODE_TITLE = "com.sheikhnaim2026.superpodcast.EXTRA_EPISODE_TITLE"
        private const val PREFS_NAME = "podcast_updates"
    }

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            // Retrieve list of subscribed podcasts from Room database
            val subscriptions = database.subscriptionDao().getAll().first()

            for (podcast in subscriptions) {
                checkPodcast(podcast)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Retry up to 3 times for transient failures, then fail permanently
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Downloads the RSS feed for a single subscribed podcast and checks if a new episode was published.
     */
    private suspend fun checkPodcast(podcast: SubscribedPodcast) {
        if (podcast.feedUrl.isBlank()) return

        // Parse RSS feed on IO dispatcher
        val episodes = withContext(Dispatchers.IO) {
            PodcastRssParser.parseFeed(podcast.feedUrl)
        }

        if (episodes.isEmpty()) return

        val newestEpisode = episodes.first()
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "latest_${podcast.trackId}"
        val previousGuid = prefs.getString(key, null)

        if (previousGuid == null) {
            // First time tracking this podcast: store current GUID as baseline without posting an alert
            prefs.edit().putString(key, newestEpisode.guid).apply()
        } else if (previousGuid != newestEpisode.guid) {
            // New episode detected: save new GUID and notify the user
            prefs.edit().putString(key, newestEpisode.guid).apply()
            notifyNewEpisode(podcast, newestEpisode)
        }
    }

    /**
     * Dispatches the update alert:
     * - If MainActivity is open in foreground: sends a broadcast for an in-app Toast.
     * - If the app is in background or closed: posts a system push notification.
     */
    private fun notifyNewEpisode(podcast: SubscribedPodcast, episode: Episode) {
        val intent = Intent(ACTION_NEW_EPISODE).apply {
            setPackage(applicationContext.packageName)
            putExtra(EXTRA_PODCAST_TITLE, podcast.collectionName)
            putExtra(EXTRA_EPISODE_TITLE, episode.title)
        }

        if (MainActivity.isInForeground) {
            applicationContext.sendBroadcast(intent)
        } else {
            NotificationHelper.showEpisodeNotification(applicationContext, podcast, episode)
        }
    }
}
