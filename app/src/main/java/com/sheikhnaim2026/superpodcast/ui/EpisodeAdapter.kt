package com.sheikhnaim2026.superpodcast.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.sheikhnaim2026.superpodcast.R
import com.sheikhnaim2026.superpodcast.data.Episode

/**
 * EpisodeAdapter
 *
 * RecyclerView Adapter to display parsed RSS episodes in the podcast detail screen.
 *
 * @param episodes List of [Episode] items to render.
 * @param onEpisodeClick Callback invoked when an episode row is clicked.
 */
class EpisodeAdapter(
    private var episodes: List<Episode>,
    private val onEpisodeClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    /**
     * Replaces the current dataset with [newEpisodes] and refreshes the list.
     */
    fun updateList(newEpisodes: List<Episode>) {
        this.episodes = newEpisodes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position], onEpisodeClick)
    }

    override fun getItemCount(): Int = episodes.size

    class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textTitle: TextView = itemView.findViewById(R.id.textViewEpisodeTitle)
        private val textType: TextView = itemView.findViewById(R.id.textViewEpisodeType)
        private val textDate: TextView = itemView.findViewById(R.id.textViewEpisodeDate)

        fun bind(episode: Episode, onEpisodeClick: (Episode) -> Unit) {
            textTitle.text = episode.title
            textDate.text = episode.pubDate.ifBlank { "Recent Release" }

            if (episode.isVideo) {
                textType.text = "VIDEO"
                ViewCompat.setBackgroundTintList(textType, ColorStateList.valueOf(Color.parseColor("#2E1928")))
                textType.setTextColor(Color.parseColor("#FF6B6B"))
            } else {
                textType.text = "AUDIO"
                ViewCompat.setBackgroundTintList(textType, ColorStateList.valueOf(Color.parseColor("#142921")))
                textType.setTextColor(Color.parseColor("#00F5A0"))
            }

            itemView.setOnClickListener {
                onEpisodeClick(episode)
            }
        }
    }
}
