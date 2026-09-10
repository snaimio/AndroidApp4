package com.sheikhnaim2026.superpodcast.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sheikhnaim2026.superpodcast.R
import com.sheikhnaim2026.superpodcast.data.ScoredPodcast

// PodcastAdapter: Custom RecyclerView Adapter to display scored podcast cards.
// Manages inflating item_podcast.xml layouts and binding podcast metadata and images.
class PodcastAdapter(
    private var items: List<ScoredPodcast>
) : RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder>() {

    // Swaps the current list with a new dataset and refreshes the RecyclerView UI
    fun updateList(newItems: List<ScoredPodcast>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    // Inflates the custom card layout (item_podcast.xml) when new rows are needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_podcast, parent, false)
        return PodcastViewHolder(view)
    }

    // Connects data from the ScoredPodcast item at the given position to the ViewHolder
    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // Returns total number of podcasts currently in the list
    override fun getItemCount(): Int = items.size

    // PodcastViewHolder: Caches references to the views inside item_podcast.xml for fast scrolling
    class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageArtwork: ImageView = itemView.findViewById(R.id.imageViewArtwork)
        private val textTitle: TextView = itemView.findViewById(R.id.textViewPodcastTitle)
        private val textArtist: TextView = itemView.findViewById(R.id.textViewArtistName)
        private val textMoodScore: TextView = itemView.findViewById(R.id.textViewMoodScore)

        // Binds all podcast properties and applies dynamic badge color coding
        fun bind(scoredItem: ScoredPodcast) {
            val podcast = scoredItem.podcast

            // Display collection name or fallback to trackName if collectionName is missing
            val title = podcast.collectionName?.takeIf { it.isNotBlank() }
                ?: podcast.trackName ?: "Untitled Podcast"
            textTitle.text = title

            // Display creator / artist name
            textArtist.text = podcast.artistName ?: "Unknown Creator"

            // Set mood match percentage badge text and color-code based on strength:
            // - 80%+: Electric Mint (High resonance)
            // - 60%-79%: Golden Ochre (Medium resonance)
            // - Below 60%: Neon Peach (Baseline match)
            val score = scoredItem.matchPercentage
            val (badgeText, bgColor, textColor) = when {
                score >= 80 -> Triple("● $score% Vibe", Color.parseColor("#142921"), Color.parseColor("#00F5A0"))
                score >= 60 -> Triple("● $score% Vibe", Color.parseColor("#2E2214"), Color.parseColor("#FFD166"))
                else -> Triple("● $score% Vibe", Color.parseColor("#26192E"), Color.parseColor("#FF6B6B"))
            }

            textMoodScore.text = badgeText
            // Apply background tint to the rounded shape drawable while preserving rounded pill corners
            ViewCompat.setBackgroundTintList(textMoodScore, ColorStateList.valueOf(bgColor))
            textMoodScore.setTextColor(textColor)

            // Asynchronously download and cache podcast artwork image using Glide
            Glide.with(itemView.context)
                .load(podcast.artworkUrl100)
                .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder while loading
                .error(android.R.drawable.ic_dialog_alert)       // Fallback icon on network error
                .into(imageArtwork)
        }
    }
}
