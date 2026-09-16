package com.sheikhnaim2026.superpodcast.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.sheikhnaim2026.superpodcast.R
import com.sheikhnaim2026.superpodcast.data.AppDatabase
import com.sheikhnaim2026.superpodcast.data.Episode
import com.sheikhnaim2026.superpodcast.data.PodcastRssParser
import com.sheikhnaim2026.superpodcast.data.SubscribedPodcast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PodcastDetailActivity
 *
 * Detailed view for an individual podcast.
 * Handles:
 * - Parsing and displaying the RSS feed episodes.
 * - Audio, Video, and HLS streaming playback via Media3 ExoPlayer.
 * - Subscribing/unsubscribing to the podcast with Room database persistence.
 */
class PodcastDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TRACK_ID = "com.sheikhnaim2026.superpodcast.EXTRA_TRACK_ID"
        const val EXTRA_TITLE = "com.sheikhnaim2026.superpodcast.EXTRA_TITLE"
        const val EXTRA_ARTIST = "com.sheikhnaim2026.superpodcast.EXTRA_ARTIST"
        const val EXTRA_ARTWORK = "com.sheikhnaim2026.superpodcast.EXTRA_ARTWORK"
        const val EXTRA_FEED_URL = "com.sheikhnaim2026.superpodcast.EXTRA_FEED_URL"
    }

    // UI View References
    private lateinit var imageViewArtwork: ImageView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewArtist: TextView
    private lateinit var textViewDescription: TextView
    private lateinit var buttonSubscribe: MaterialButton
    private lateinit var playerView: PlayerView
    private lateinit var recyclerViewEpisodes: RecyclerView
    private lateinit var progressBarFeed: ProgressBar

    // Data & Media player
    private lateinit var episodeAdapter: EpisodeAdapter
    private lateinit var database: AppDatabase
    private var player: ExoPlayer? = null
    private var isSubscribed = false

    // Podcast Intent Extras
    private var podcastTrackId: Long = 0L
    private var podcastTitle: String = ""
    private var podcastArtist: String = ""
    private var podcastArtwork: String = ""
    private var podcastFeedUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_podcast_detail)

        // Set up MaterialToolbar with back navigation
        val toolbar: MaterialToolbar = findViewById(R.id.toolbarDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Wire top My Subscriptions navigation button
        val btnSubscriptions: MaterialButton = findViewById(R.id.btnSubscriptions)
        btnSubscriptions.setOnClickListener {
            startActivity(Intent(this, SubscriptionsActivity::class.java))
        }

        // Bind view references
        imageViewArtwork = findViewById(R.id.imageViewPodcastArtwork)
        textViewTitle = findViewById(R.id.textViewPodcastTitle)
        textViewArtist = findViewById(R.id.textViewPodcastArtist)
        textViewDescription = findViewById(R.id.textViewDescription)
        buttonSubscribe = findViewById(R.id.buttonSubscribe)
        playerView = findViewById(R.id.playerView)
        recyclerViewEpisodes = findViewById(R.id.recyclerViewEpisodes)
        progressBarFeed = findViewById(R.id.progressBarFeed)

        // Read intent extras passed from MainActivity or SubscriptionsActivity
        podcastTrackId = intent.getLongExtra(EXTRA_TRACK_ID, 0L)
        podcastTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Podcast"
        toolbar.title = podcastTitle
        supportActionBar?.title = podcastTitle
        invalidateOptionsMenu()
        podcastArtist = intent.getStringExtra(EXTRA_ARTIST) ?: "Unknown Artist"
        podcastArtwork = intent.getStringExtra(EXTRA_ARTWORK) ?: ""
        podcastFeedUrl = intent.getStringExtra(EXTRA_FEED_URL) ?: ""

        // Populate header views
        textViewTitle.text = podcastTitle
        textViewArtist.text = podcastArtist
        Glide.with(this)
            .load(podcastArtwork)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .into(imageViewArtwork)

        // Initialize Room database
        database = AppDatabase.getDatabase(this)

        // Wire Subscribe button
        buttonSubscribe.setOnClickListener {
            toggleSubscription()
        }

        // Configure Episode Adapter with click-to-play listener
        episodeAdapter = EpisodeAdapter(emptyList()) { selectedEpisode ->
            textViewDescription.text = cleanDescription(selectedEpisode.description)
            playEpisode(selectedEpisode)
        }
        recyclerViewEpisodes.layoutManager = LinearLayoutManager(this)
        recyclerViewEpisodes.adapter = episodeAdapter

        // Initialize ExoPlayer and check subscription state
        initializePlayer()
        checkSubscriptionStatus()

        // Load RSS feed episodes
        if (podcastFeedUrl.isNotBlank()) {
            loadFeed(podcastFeedUrl)
        } else {
            progressBarFeed.visibility = View.GONE
            textViewDescription.text = getString(R.string.error_no_feed_url)
        }
    }

    /**
     * Checks if this podcast is currently stored in the Room subscriptions table.
     */
    private fun checkSubscriptionStatus() {
        lifecycleScope.launch {
            val exists = withContext(Dispatchers.IO) {
                database.subscriptionDao().isSubscribed(podcastTrackId)
            }
            isSubscribed = exists
            updateSubscriptionButton()
        }
    }

    /**
     * Updates the Subscribe button appearance depending on whether the podcast is subscribed.
     */
    private fun updateSubscriptionButton() {
        if (isSubscribed) {
            buttonSubscribe.text = getString(R.string.btn_unsubscribe)
            buttonSubscribe.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2C3040"))
            buttonSubscribe.setTextColor(Color.parseColor("#FF6B6B"))
        } else {
            buttonSubscribe.text = getString(R.string.btn_subscribe)
            buttonSubscribe.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF6B6B"))
            buttonSubscribe.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    /**
     * Inserts or deletes the podcast from the Room subscriptions database.
     */
    private fun toggleSubscription() {
        if (!isSubscribed && podcastFeedUrl.isBlank()) {
            Toast.makeText(this, getString(R.string.error_cannot_subscribe_no_feed), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val entity = SubscribedPodcast(
                trackId = podcastTrackId,
                collectionName = podcastTitle,
                artistName = podcastArtist,
                artworkUrl100 = podcastArtwork,
                feedUrl = podcastFeedUrl
            )

            withContext(Dispatchers.IO) {
                if (isSubscribed) {
                    database.subscriptionDao().delete(entity)
                } else {
                    database.subscriptionDao().insert(entity)
                }
            }

            isSubscribed = !isSubscribed
            updateSubscriptionButton()
            val msg = if (isSubscribed) getString(R.string.msg_subscribed_to, podcastTitle) else getString(R.string.msg_unsubscribed)
            Toast.makeText(this@PodcastDetailActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Initializes Media3 ExoPlayer and connects it to the PlayerView.
     */
    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(this).build()
            playerView.player = player
        }
    }

    /**
     * Parses the podcast RSS feed on a background thread and populates the episode list.
     */
    private fun loadFeed(feedUrl: String) {
        progressBarFeed.visibility = View.VISIBLE

        lifecycleScope.launch {
            val episodes = withContext(Dispatchers.IO) {
                PodcastRssParser.parseFeed(feedUrl)
            }

            progressBarFeed.visibility = View.GONE

            if (episodes.isNotEmpty()) {
                episodeAdapter.updateList(episodes)
                // Show show-notes description of the newest episode
                textViewDescription.text = cleanDescription(episodes.first().description)
            } else {
                textViewDescription.text = getString(R.string.error_no_episodes)
            }
        }
    }

    /**
     * Prepares and starts playback of the selected episode stream in ExoPlayer.
     * Supports standard MP3/MP4 media as well as HLS (.m3u8) adaptive streams.
     */
    private fun playEpisode(episode: Episode) {
        initializePlayer()

        val mediaItemBuilder = MediaItem.Builder()
            .setUri(episode.mediaUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(episode.title)
                    .setArtist(podcastArtist)
                    .build()
            )

        // Check if stream is HLS (.m3u8) so ExoPlayer can instantiate the correct HLS MediaSource factory
        if (episode.mediaUrl.contains(".m3u8", ignoreCase = true) ||
            episode.mediaType.contains("mpegurl", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        player?.setMediaItem(mediaItemBuilder.build())
        player?.prepare()
        player?.play()
        playerView.visibility = View.VISIBLE
    }

    /**
     * Strips HTML formatting from RSS description text to render clean human-readable text.
     */
    private fun cleanDescription(rawHtml: String): String {
        return try {
            Html.fromHtml(rawHtml, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } catch (e: Exception) {
            rawHtml.trim()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStart() {
        super.onStart()
        if (player == null) {
            initializePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release player only in onDestroy (not onStop) to allow audio playback to continue during configuration changes and screen off
        player?.release()
        player = null
    }
}
