package com.sheikhnaim2026.superpodcast.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.sheikhnaim2026.superpodcast.R
import com.sheikhnaim2026.superpodcast.data.ITunesApi
import com.sheikhnaim2026.superpodcast.data.Mood
import com.sheikhnaim2026.superpodcast.data.MoodCatalog
import com.sheikhnaim2026.superpodcast.data.NotificationHelper
import com.sheikhnaim2026.superpodcast.data.Podcast
import com.sheikhnaim2026.superpodcast.data.PodcastUpdateScheduler
import com.sheikhnaim2026.superpodcast.data.PodcastUpdateWorker
import com.sheikhnaim2026.superpodcast.logic.MoodScorer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * MainActivity
 *
 * The primary discovery and search activity of SuperPodcast.
 * Coordinates:
 * - iTunes API podcast search with mood scoring.
 * - Runtime notification permission requests (Android 13+).
 * - WorkManager update check scheduling.
 * - In-app broadcast receiver for new episode alerts.
 * - Navigation to [PodcastDetailActivity] and [SubscriptionsActivity].
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE_NOTIFICATIONS = 1001

        /**
         * Volatile flag tracking whether MainActivity is currently active in the foreground.
         * Used by [PodcastUpdateWorker] to decide between sending an in-app broadcast or a push notification.
         */
        @Volatile
        var isInForeground = false
    }

    // UI View References
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
    private lateinit var btnSubscriptions: MaterialButton
    private lateinit var btnDice: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewStatus: TextView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var imageVinyl: ImageView
    private lateinit var textViewEmptyTitle: TextView
    private lateinit var textViewEmptySubtitle: TextView
    private lateinit var adapter: PodcastAdapter

    // Retrofit API Service instance
    private lateinit var api: ITunesApi

    // Coroutine Job tracking active search query to prevent race conditions
    private var searchJob: Job? = null

    /**
     * BroadcastReceiver that listens for new episode alerts when the app is active in foreground.
     */
    private val episodeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PodcastUpdateWorker.ACTION_NEW_EPISODE) {
                val podcastTitle = intent.getStringExtra(PodcastUpdateWorker.EXTRA_PODCAST_TITLE) ?: "Podcast"
                val episodeTitle = intent.getStringExtra(PodcastUpdateWorker.EXTRA_EPISODE_TITLE) ?: "New Episode"
                Toast.makeText(this@MainActivity, "✨ New episode in $podcastTitle:\n$episodeTitle", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle edge-to-edge system insets (status bar & navigation bar padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components from XML layout
        editTextSearch = findViewById(R.id.editTextSearch)
        buttonSearch = findViewById(R.id.buttonSearch)
        btnSubscriptions = findViewById(R.id.btnSubscriptions)
        btnDice = findViewById(R.id.btnDice)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        textViewStatus = findViewById(R.id.textViewStatus)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        imageVinyl = findViewById(R.id.imageVinyl)
        textViewEmptyTitle = findViewById(R.id.textViewEmptyTitle)
        textViewEmptySubtitle = findViewById(R.id.textViewEmptySubtitle)

        // Configure RecyclerView with vertical layout manager and podcast click navigation
        adapter = PodcastAdapter(emptyList()) { podcast ->
            openPodcastDetail(podcast)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Build Retrofit client targeting iTunes Search API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ITunesApi::class.java)

        // Search button click listener
        buttonSearch.setOnClickListener {
            performSearchFromInput()
        }

        // Subscriptions library button
        btnSubscriptions.setOnClickListener {
            startActivity(Intent(this, SubscriptionsActivity::class.java))
        }

        // Surprise Dice button: picks a random mood with a 360-degree vinyl spin animation
        btnDice.setOnClickListener {
            val randomMood = MoodCatalog.getRandomMood()
            editTextSearch.setText(randomMood.label)
            imageVinyl.animate().rotationBy(360f).setDuration(500).start()
            searchPodcastsByMood(randomMood)
        }

        // Trigger search on keyboard "Search" action
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchFromInput()
                true
            } else {
                false
            }
        }

        // Setup click listeners for preset mood chips
        setupMoodChips()

        // Initialize notification channel and background WorkManager tasks
        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermission()
        PodcastUpdateScheduler.schedule(this)
    }

    /**
     * Checks and requests runtime notification permission on Android 13+ (API 33).
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE_NOTIFICATIONS
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isInForeground = true
        // Register in-app broadcast receiver for new episode alerts
        val filter = IntentFilter(PodcastUpdateWorker.ACTION_NEW_EPISODE)
        ContextCompat.registerReceiver(
            this,
            episodeBroadcastReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        isInForeground = false
        try {
            unregisterReceiver(episodeBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
    }

    /**
     * Reads search text from input and starts podcast discovery.
     */
    private fun performSearchFromInput() {
        val query = editTextSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            val mood = MoodCatalog.findByLabelOrId(query) ?: MoodCatalog.fromFreeText(query)
            searchPodcastsByMood(mood)
        } else {
            Toast.makeText(this, "Enter a vibe or roll the dice 🎲", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Sets click listeners on preset mood chips.
     */
    private fun setupMoodChips() {
        bindChip(R.id.chipCozy, "cozy")
        bindChip(R.id.chipGym, "gym")
        bindChip(R.id.chipCrime, "crime")
        bindChip(R.id.chipFocus, "focus")
        bindChip(R.id.chipComedy, "comedy")
    }

    private fun bindChip(chipId: Int, moodId: String) {
        findViewById<Chip>(chipId)?.setOnClickListener {
            val mood = MoodCatalog.findByLabelOrId(moodId)
            if (mood != null) {
                editTextSearch.setText(mood.label)
                searchPodcastsByMood(mood)
            }
        }
    }

    /**
     * Executes the search pipeline, cancelling any ongoing search job to prevent race conditions.
     */
    private fun searchPodcastsByMood(mood: Mood) {
        // Cancel any pending search to prevent out-of-order UI updates
        searchJob?.cancel()

        progressBar.visibility = View.VISIBLE
        layoutEmptyState.visibility = View.GONE
        recyclerView.alpha = 0.2f
        textViewStatus.visibility = View.VISIBLE
        textViewStatus.text = "TUNING IN: ${mood.label.uppercase()}..."

        searchJob = lifecycleScope.launch {
            try {
                // Step 1: Call iTunes Search API on IO dispatcher
                val response = withContext(Dispatchers.IO) {
                    api.searchPodcasts(term = mood.seedTerm)
                }

                // Step 2: Calculate mood match percentage on Default dispatcher
                val rankedPodcasts = withContext(Dispatchers.Default) {
                    MoodScorer.rank(response.results, mood)
                }

                progressBar.visibility = View.GONE
                recyclerView.alpha = 1.0f

                // Step 3: Populate list
                if (rankedPodcasts.isNotEmpty()) {
                    layoutEmptyState.visibility = View.GONE
                    textViewStatus.text = "● ${rankedPodcasts.size} SHOWS TUNED TO ${mood.label.uppercase()}"
                    adapter.updateList(rankedPodcasts)
                } else {
                    textViewStatus.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE
                    textViewEmptyTitle.text = getString(R.string.empty_title_not_found)
                    textViewEmptySubtitle.text = "No podcasts matched '${mood.label}'.\nTry rolling the dice 🎲 or picking another vibe."
                    adapter.updateList(emptyList())
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                recyclerView.alpha = 1.0f
                textViewStatus.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
                textViewEmptyTitle.text = getString(R.string.empty_title_error)
                textViewEmptySubtitle.text = getString(R.string.empty_subtitle_error)
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Network error. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Navigates to [PodcastDetailActivity] passing all 5 extras.
     */
    private fun openPodcastDetail(podcast: Podcast) {
        val intent = Intent(this, PodcastDetailActivity::class.java).apply {
            putExtra(PodcastDetailActivity.EXTRA_TRACK_ID, podcast.trackId)
            putExtra(PodcastDetailActivity.EXTRA_TITLE, podcast.collectionName ?: podcast.trackName ?: "Podcast")
            putExtra(PodcastDetailActivity.EXTRA_ARTIST, podcast.artistName ?: "Unknown Creator")
            putExtra(PodcastDetailActivity.EXTRA_ARTWORK, podcast.artworkUrl100 ?: "")
            putExtra(PodcastDetailActivity.EXTRA_FEED_URL, podcast.feedUrl ?: "")
        }
        startActivity(intent)
    }
}
