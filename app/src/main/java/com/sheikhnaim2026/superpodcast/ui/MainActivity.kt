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

// Main activity for SuperPodcast app
class MainActivity : AppCompatActivity() {

    // UI View variables
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

    // Retrofit API instance
    private lateinit var api: ITunesApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle edge-to-edge system insets (status bar / navigation bar padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Connect UI elements from activity_main.xml
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

        // Setup RecyclerView with LinearLayoutManager and empty adapter
        adapter = PodcastAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup Retrofit with iTunes base URL and Gson converter
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ITunesApi::class.java)

        // Search button click listener
        buttonSearch.setOnClickListener {
            performSearchFromInput()
        }

        // Surprise Dice click listener (picks a random mood vibe)
        btnDice.setOnClickListener {
            val randomMood = MoodCatalog.getRandomMood()
            editTextSearch.setText(randomMood.label)
            // Playful vinyl rotation feedback
            imageVinyl.animate().rotationBy(360f).setDuration(500).start()
            searchPodcastsByMood(randomMood)
        }

        // Trigger search when pressing Enter on software keyboard
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
    }

    // Reads search input and triggers podcast search
    private fun performSearchFromInput() {
        val query = editTextSearch.text.toString().trim()
        if (query.isNotEmpty()) {
            val mood = MoodCatalog.findByLabelOrId(query) ?: MoodCatalog.fromFreeText(query)
            searchPodcastsByMood(mood)
        } else {
            Toast.makeText(this, "Enter a vibe or roll the dice 🎲", Toast.LENGTH_SHORT).show()
        }
    }

    // Set click listener on each preset chip
    private fun setupMoodChips() {
        bindChip(R.id.chipCozy, "cozy")
        bindChip(R.id.chipGym, "gym")
        bindChip(R.id.chipCrime, "crime")
        bindChip(R.id.chipFocus, "focus")
        bindChip(R.id.chipComedy, "comedy")
    }

    // Helper function to link a chip view with a mood id
    private fun bindChip(chipId: Int, moodId: String) {
        findViewById<Chip>(chipId)?.setOnClickListener {
            val mood = MoodCatalog.findByLabelOrId(moodId)
            if (mood != null) {
                editTextSearch.setText(mood.label)
                searchPodcastsByMood(mood)
            }
        }
    }

    // Searches iTunes API and scores results using Coroutines
    private fun searchPodcastsByMood(mood: Mood) {
        // Show progress bar and update status text
        progressBar.visibility = View.VISIBLE
        layoutEmptyState.visibility = View.GONE
        recyclerView.alpha = 0.2f
        textViewStatus.visibility = View.VISIBLE
        textViewStatus.text = "TUNING IN: ${mood.label.uppercase()}..."

        lifecycleScope.launch {
            try {
                // Step 1: Call iTunes API on background IO thread
                val response = withContext(Dispatchers.IO) {
                    api.searchPodcasts(term = mood.seedTerm)
                }

                // Step 2: Score and rank podcasts on background Default thread
                val rankedPodcasts = withContext(Dispatchers.Default) {
                    MoodScorer.rank(response.results, mood)
                }

                // Restore UI back to normal
                progressBar.visibility = View.GONE
                recyclerView.alpha = 1.0f

                // Step 3: Update adapter with results
                if (rankedPodcasts.isNotEmpty()) {
                    layoutEmptyState.visibility = View.GONE
                    textViewStatus.text = "● ${rankedPodcasts.size} SHOWS TUNED TO ${mood.label.uppercase()}"
                    adapter.updateList(rankedPodcasts)
                } else {
                    // Show empty state if no podcasts were found
                    textViewStatus.visibility = View.GONE
                    layoutEmptyState.visibility = View.VISIBLE
                    textViewEmptyTitle.text = "No frequency found"
                    textViewEmptySubtitle.text = "No podcasts matched '${mood.label}'.\nTry rolling the dice 🎲 or picking another vibe."
                    adapter.updateList(emptyList())
                }

            } catch (e: Exception) {
                // Catch network errors and show a toast
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
