package com.sheikhnaim2026.superpodcast.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * PodcastUpdateScheduler
 *
 * Configures and registers background WorkManager jobs to periodically check for new podcast episodes.
 */
object PodcastUpdateScheduler {

    private const val WORK_PERIODIC = "podcast_update_check"
    private const val WORK_INITIAL = "podcast_initial_update_check"

    /**
     * Schedules periodic and one-time update checks using WorkManager with network constraints.
     *
     * @param context Application context used to access [WorkManager].
     */
    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Require an active network connection so we don't waste battery attempting offline fetches
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 1. Periodic background check every 15 minutes (minimum interval supported by Android WorkManager)
        val periodicWork = PeriodicWorkRequestBuilder<PodcastUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )

        // 2. One-time initial check executed shortly after startup to sync immediately
        val initialWork = OneTimeWorkRequestBuilder<PodcastUpdateWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            WORK_INITIAL,
            ExistingWorkPolicy.KEEP,
            initialWork
        )
    }
}
