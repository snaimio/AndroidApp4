package com.sheikhnaim2026.superpodcast.data

/**
 * Episode
 *
 * Data model representing an individual podcast episode parsed from an RSS feed.
 *
 * @property guid Unique identifier string for the episode, parsed from the <guid> tag in the RSS feed.
 * @property title The title of the episode, parsed from the <title> tag.
 * @property description HTML or plain text summary of the episode, extracted from <description> or <content:encoded>.
 * @property pubDate Release / publishing date string, parsed from the <pubDate> tag.
 * @property mediaUrl Direct URL to the audio/video media file, parsed from <enclosure url="..."> or <media:content url="...">.
 * @property mediaType MIME type string (e.g., "audio/mpeg", "video/mp4", "application/x-mpegURL").
 * @property isVideo True if the media stream is a video file; false if it is standard audio.
 */
data class Episode(
    val guid: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val mediaUrl: String,
    val mediaType: String,
    val isVideo: Boolean
)
