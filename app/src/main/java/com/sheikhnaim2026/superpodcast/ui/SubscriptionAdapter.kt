package com.sheikhnaim2026.superpodcast.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sheikhnaim2026.superpodcast.R
import com.sheikhnaim2026.superpodcast.data.SubscribedPodcast

/**
 * SubscriptionAdapter
 *
 * RecyclerView Adapter for displaying subscribed podcasts retrieved from Room database.
 *
 * @param items List of [SubscribedPodcast] entities.
 * @param onPodcastClick Callback invoked when a subscription card is tapped.
 */
class SubscriptionAdapter(
    private var items: List<SubscribedPodcast>,
    private val onPodcastClick: (SubscribedPodcast) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder>() {

    fun updateList(newItems: List<SubscribedPodcast>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscription, parent, false)
        return SubscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(items[position], onPodcastClick)
    }

    override fun getItemCount(): Int = items.size

    class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageArtwork: ImageView = itemView.findViewById(R.id.imageViewArtwork)
        private val textTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textArtist: TextView = itemView.findViewById(R.id.textViewArtist)

        fun bind(podcast: SubscribedPodcast, onPodcastClick: (SubscribedPodcast) -> Unit) {
            textTitle.text = podcast.collectionName
            textArtist.text = podcast.artistName

            Glide.with(itemView.context)
                .load(podcast.artworkUrl100)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .into(imageArtwork)

            itemView.setOnClickListener {
                onPodcastClick(podcast)
            }
        }
    }
}
