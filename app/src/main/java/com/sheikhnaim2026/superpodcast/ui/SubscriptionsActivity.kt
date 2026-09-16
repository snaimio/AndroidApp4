package com.sheikhnaim2026.superpodcast.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.sheikhnaim2026.superpodcast.R
import com.sheikhnaim2026.superpodcast.data.AppDatabase
import com.sheikhnaim2026.superpodcast.data.SubscribedPodcast
import kotlinx.coroutines.launch

/**
 * SubscriptionsActivity
 *
 * Screen listing all podcasts saved to the local Room database by the user.
 * Observes database changes via Kotlin Coroutines [kotlinx.coroutines.flow.Flow].
 */
class SubscriptionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var adapter: SubscriptionAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        // Set up MaterialToolbar with back navigation
        val toolbar: MaterialToolbar = findViewById(R.id.toolbarSubscriptions)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewSubscriptions)
        layoutEmpty = findViewById(R.id.layoutEmptySubscriptions)

        database = AppDatabase.getDatabase(this)

        // Setup adapter with click navigation to detail screen
        adapter = SubscriptionAdapter(emptyList()) { podcast ->
            openPodcastDetail(podcast)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        observeSubscriptions()
    }

    /**
     * Reactively collects subscribed podcasts from Room database and toggles empty state.
     */
    private fun observeSubscriptions() {
        lifecycleScope.launch {
            database.subscriptionDao().getAll().collect { subscriptions ->
                adapter.updateList(subscriptions)

                if (subscriptions.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Navigates to [PodcastDetailActivity] passing all required extras.
     */
    private fun openPodcastDetail(podcast: SubscribedPodcast) {
        val intent = Intent(this, PodcastDetailActivity::class.java).apply {
            putExtra(PodcastDetailActivity.EXTRA_TRACK_ID, podcast.trackId)
            putExtra(PodcastDetailActivity.EXTRA_TITLE, podcast.collectionName)
            putExtra(PodcastDetailActivity.EXTRA_ARTIST, podcast.artistName)
            putExtra(PodcastDetailActivity.EXTRA_ARTWORK, podcast.artworkUrl100)
            putExtra(PodcastDetailActivity.EXTRA_FEED_URL, podcast.feedUrl)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
