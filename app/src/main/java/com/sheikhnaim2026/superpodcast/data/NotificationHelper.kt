package com.sheikhnaim2026.superpodcast.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sheikhnaim2026.superpodcast.R
import com.sheikhnaim2026.superpodcast.ui.PodcastDetailActivity

/**
 * NotificationHelper
 *
 * Utility object for creating system notification channels and posting episode update notifications.
 */
object NotificationHelper {

    const val CHANNEL_ID = "podcast_updates"
    private const val CHANNEL_NAME = "Podcast Updates"
    private const val CHANNEL_DESCRIPTION = "Alerts when new episodes are released for your subscribed podcasts"

    /**
     * Registers the notification channel with the Android OS on API 26 (Android O) and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Builds and displays a push notification alerting the user to a newly discovered podcast episode.
     * Tapping the notification opens [PodcastDetailActivity] pre-loaded with the podcast's data.
     *
     * @param context Application or service context.
     * @param podcast The subscribed podcast that released the episode.
     * @param episode The newly discovered episode.
     */
    fun showEpisodeNotification(context: Context, podcast: SubscribedPodcast, episode: Episode) {
        // Guard against posting without runtime permission on Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return
        }

        // Ensure channel exists before posting
        createNotificationChannel(context)

        // Build navigation intent to launch PodcastDetailActivity
        val intent = Intent(context, PodcastDetailActivity::class.java).apply {
            putExtra(PodcastDetailActivity.EXTRA_TRACK_ID, podcast.trackId)
            putExtra(PodcastDetailActivity.EXTRA_TITLE, podcast.collectionName)
            putExtra(PodcastDetailActivity.EXTRA_ARTIST, podcast.artistName)
            putExtra(PodcastDetailActivity.EXTRA_ARTWORK, podcast.artworkUrl100)
            putExtra(PodcastDetailActivity.EXTRA_FEED_URL, podcast.feedUrl)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        // Use hashCode of the trackId as the request code and notification ID
        val notificationId = podcast.trackId.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build rich notification with BigTextStyle expander
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(podcast.collectionName)
            .setContentText("New episode: ${episode.title}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("New episode: ${episode.title}\n\n${episode.description.take(160)}")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
