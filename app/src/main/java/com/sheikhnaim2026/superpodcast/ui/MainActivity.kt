package com.sheikhnaim2026.superpodcast.ui

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
import com.sheikhnaim2026.superpodcast.logic.MoodScorer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// MainActivity: The main screen of SuperPodcast.
// Handles user interactions (search bar, mood chips, dice shuffle), triggers asynchronous API
// network requests using Kotlin Coroutines, and updates the RecyclerView with scored results.
class MainActivity : AppCompatActivity() {

    // UI View References
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge rendering for modern full-screen display
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Apply system window insets so content is not obscured by the status bar or navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components from XML layout
        editTextSearch = findViewById(R.id.editTextSearch)
        buttonSearch = findViewById(R.id.buttonSearch)
        btnDice = findViewById(R.id.btnDice)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        textViewStatus = findViewById(R.id.textViewStatus)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        imageVinyl = findViewById(R.id.imageVinyl)
        textViewEmptyTitle = findViewById(R.id.textViewEmptyTitle)
        textViewEmptySubtitle = findViewById(R.id.textViewEmptySubtitle)

        // Configure RecyclerView with vertical layout manager and an initially empty adapter
        adapter = PodcastAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Build Retrofit client targeting the iTunes Search API base URL with Gson JSON parser
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ITunesApi::class.java)

        // Set click listener for the search button
        buttonSearch.setOnClickListener {
            performSearchFromInput()
        }

        // Set click listener for the Surprise Dice button:
        // Picks a random mood from MoodCatalog, triggers a 360-degree vinyl spin animation, and searches
        btnDice.setOnClickListener {
            val randomMood = MoodCatalog.getRandomMood()
            editTextSearch.setText(randomMood.label)
            imageVinyl.animate().rotationBy(360f).setDuration(500).start()
            searchPodcastsByMood(randomMood)
        }

        // Handle the software keyboard "Search" action key on IME enter
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchFromInput()
                true
            } else {
                false
            }
        }

        // Set up click listeners on all predefined mood preset chips
        setupMoodChips()
    }

    // Reads the search query from the EditText and resolves it to a preset or free-text Mood
    private fun performSearchFromInput() {
        val query = editTextSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            // Find existing preset mood or dynamically synthesize one from custom text
            val mood = MoodCatalog.findByLabelOrId(query) ?: MoodCatalog.fromFreeText(query)
            searchPodcastsByMood(mood)
        } else {
            Toast.makeText(this, "Enter a vibe or roll the dice 🎲", Toast.LENGTH_SHORT).show()
        }
    }

    // Configures listeners for the horizontal mood chips
    private fun setupMoodChips() {
        bindChip(R.id.chipCozy, "cozy")
        bindChip(R.id.chipGym, "gym")
        bindChip(R.id.chipCrime, "crime")
        bindChip(R.id.chipFocus, "focus")
        bindChip(R.id.chipComedy, "comedy")
    }

    // Helper to connect a Chip view to a specific Mood ID from MoodCatalog
    private fun bindChip(chipId: Int, moodId: String) {
        findViewById<Chip>(chipId)?.setOnClickListener {
            val mood = MoodCatalog.findByLabelOrId(moodId)
            if (mood != null) {
                editTextSearch.setText(mood.label)
                searchPodcastsByMood(mood)
            }
        }
    }

    // Executes the complete search, score, and UI update pipeline using Coroutines
    private fun searchPodcastsByMood(mood: Mood) {
        // Show loading progress spinner, hide empty state, and dim list slightly
        progressBar.visibility = View.VISIBLE
        layoutEmptyState.visibility = View.GONE
        recyclerView.alpha = 0.2f
        textViewStatus.visibility = View.VISIBLE
        textViewStatus.text = "TUNING IN: ${mood.label.uppercase()}..."

        // Launch coroutine on lifecycleScope to automatically cancel if Activity is destroyed
        lifecycleScope.launch {
            try {
                // Step 1: Query iTunes API on the IO Dispatcher (background thread for networking)
                val response = withContext(Dispatchers.IO) {
                    api.searchPodcasts(term = mood.seedTerm)
                }

                // Step 2: Calculate mood match percentage on the Default Dispatcher (CPU-bound scoring)
                val rankedPodcasts = withContext(Dispatchers.Default) {
                    MoodScorer.rank(response.results, mood)
                }

                // Restore UI controls on the Main thread
                progressBar.visibility = View.GONE
                recyclerView.alpha = 1.0f

                // Step 3: Handle results and update adapter
                if (rankedPodcasts.isNotEmpty()) {
                    layoutEmptyState.visibility = View.GONE
                    textViewStatus.text = "● ${rankedPodcasts.size} SHOWS TUNED TO ${mood.label.uppercase()}"
                    adapter.updateList(rankedPodcasts)
                } else {
                    // Show empty state if no podcasts matched the query
                    textViewStatus.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE
                    textViewEmptyTitle.text = "No frequency found"
                    textViewEmptySubtitle.text = "No podcasts matched '${mood.label}'.\nTry rolling the dice 🎲 or picking another vibe."
                    adapter.updateList(emptyList())
                }

            } catch (e: Exception) {
                // Catch any network timeouts, DNS failures, or parsing errors gracefully
                progressBar.visibility = View.GONE
                recyclerView.alpha = 1.0f
                textViewStatus.visibility = View.GONE
                layoutEmptyState.visibility = View.VISIBLE
                textViewEmptyTitle.text = "Signal Lost"
                textViewEmptySubtitle.text = "Unable to connect to iTunes.\nPlease check your network connection."
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Network error. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
