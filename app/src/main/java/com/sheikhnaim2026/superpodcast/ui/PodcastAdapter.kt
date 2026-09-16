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
import com.sheikhnaim2026.superpodcast.data.Podcast
import com.sheikhnaim2026.superpodcast.data.ScoredPodcast

/**
 * PodcastAdapter
 *
 * RecyclerView Adapter that displays scored podcast cards and handles click events to open podcast details.
 *
 * @param items Initial dataset of scored podcasts.
 * @param onPodcastClick Callback invoked when a podcast card is tapped.
 */
class PodcastAdapter(
    private var items: List<ScoredPodcast>,
    private val onPodcastClick: (Podcast) -> Unit
) : RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder>() {

    fun updateList(newItems: List<ScoredPodcast>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_podcast, parent, false)
        return PodcastViewHolder(view)
    }

    override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
        holder.bind(items[position], onPodcastClick)
    }

    override fun getItemCount(): Int = items.size

    class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageArtwork: ImageView = itemView.findViewById(R.id.imageViewArtwork)
        private val textTitle: TextView = itemView.findViewById(R.id.textViewPodcastTitle)
        private val textArtist: TextView = itemView.findViewById(R.id.textViewArtistName)
        private val textMoodScore: TextView = itemView.findViewById(R.id.textViewMoodScore)

        fun bind(scoredItem: ScoredPodcast, onPodcastClick: (Podcast) -> Unit) {
            val podcast = scoredItem.podcast

            val title = podcast.collectionName?.takeIf { it.isNotBlank() }
                ?: podcast.trackName ?: "Untitled Podcast"
            textTitle.text = title
            textArtist.text = podcast.artistName ?: "Unknown Creator"

            val score = scoredItem.matchPercentage
            val (badgeText, bgColor, textColor) = when {
                score >= 80 -> Triple("● $score% Vibe", Color.parseColor("#142921"), Color.parseColor("#00F5A0"))
                score >= 60 -> Triple("● $score% Vibe", Color.parseColor("#2E2214"), Color.parseColor("#FFD166"))
                else -> Triple("● $score% Vibe", Color.parseColor("#26192E"), Color.parseColor("#FF6B6B"))
            }

            textMoodScore.text = badgeText
            ViewCompat.setBackgroundTintList(textMoodScore, ColorStateList.valueOf(bgColor))
            textMoodScore.setTextColor(textColor)

            Glide.with(itemView.context)
                .load(podcast.artworkUrl100)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(imageArtwork)

            // Make the entire podcast card clickable to navigate to detail screen
            itemView.setOnClickListener {
                onPodcastClick(podcast)
            }
        }
    }
}
