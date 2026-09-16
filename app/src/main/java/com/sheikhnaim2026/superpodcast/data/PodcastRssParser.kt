package com.sheikhnaim2026.superpodcast.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * PodcastRssParser
 *
 * Utility object for downloading and parsing XML RSS podcast feeds.
 * Uses Android's native [XmlPullParser] with namespace support to accurately extract
 * episode details, enclosure media streams, and namespaced content tags.
 */
object PodcastRssParser {

    private const val CONTENT_NAMESPACE = "http://purl.org/rss/1.0/modules/content/"
    private const val MEDIA_NAMESPACE = "http://search.yahoo.com/mrss/"

    /**
     * Connects to the given RSS feed URL and parses all <item> tags into a list of [Episode] objects.
     *
     * @param feedUrl The HTTP/HTTPS RSS feed link.
     * @return List of parsed [Episode] instances, or an empty list on failure.
     */
    fun parseFeed(feedUrl: String): List<Episode> {
        if (feedUrl.isBlank()) return emptyList()

        val episodes = mutableListOf<Episode>()
        var connection: HttpURLConnection? = null

        try {
            val url = URL(feedUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "SuperPodcast/1.0 (Android)")
                instanceFollowRedirects = true
            }

            if (connection.responseCode !in 200..299) {
                return emptyList()
            }

            connection.inputStream.use { inputStream ->
                val parser = Xml.newPullParser().apply {
                    setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
                    setInput(inputStream, null)
                }

                var eventType = parser.eventType
                var insideItem = false

                // Episode field buffers
                var guid = ""
                var title = ""
                var description = ""
                var pubDate = ""
                var mediaUrl = ""
                var mediaType = ""

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    val tagName = parser.name ?: ""
                    val namespace = parser.namespace ?: ""

                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (tagName.equals("item", ignoreCase = true)) {
                                insideItem = true
                                // Reset buffers for new episode item
                                guid = ""
                                title = ""
                                description = ""
                                pubDate = ""
                                mediaUrl = ""
                                mediaType = ""
                            } else if (insideItem) {
                                when {
                                    // Plain RSS <title> tag
                                    tagName.equals("title", ignoreCase = true) -> {
                                        title = parser.safeNextText()
                                    }
                                    // Plain RSS <guid> tag
                                    tagName.equals("guid", ignoreCase = true) -> {
                                        guid = parser.safeNextText()
                                    }
                                    // Plain RSS <pubDate> tag
                                    tagName.equals("pubDate", ignoreCase = true) -> {
                                        pubDate = parser.safeNextText()
                                    }
                                    // Plain RSS <description> tag
                                    tagName.equals("description", ignoreCase = true) -> {
                                        if (description.isBlank()) {
                                            description = parser.safeNextText()
                                        }
                                    }
                                    // Namespaced <content:encoded> tag for rich show notes
                                    namespace == CONTENT_NAMESPACE && tagName.equals("encoded", ignoreCase = true) -> {
                                        val encodedText = parser.safeNextText()
                                        if (encodedText.isNotBlank()) {
                                            description = encodedText
                                        }
                                    }
                                    // Plain RSS <enclosure> tag for audio/video media streams
                                    tagName.equals("enclosure", ignoreCase = true) -> {
                                        val urlAttr = parser.getAttributeValue(null, "url") ?: ""
                                        val typeAttr = parser.getAttributeValue(null, "type") ?: ""
                                        if (urlAttr.isNotBlank() && mediaUrl.isBlank()) {
                                            mediaUrl = urlAttr
                                            mediaType = typeAttr
                                        }
                                    }
                                    // Namespaced <media:content> tag for media streams
                                    namespace == MEDIA_NAMESPACE && tagName.equals("content", ignoreCase = true) -> {
                                        val urlAttr = parser.getAttributeValue(null, "url") ?: ""
                                        val typeAttr = parser.getAttributeValue(null, "type") ?: ""
                                        if (urlAttr.isNotBlank() && mediaUrl.isBlank()) {
                                            mediaUrl = urlAttr
                                            mediaType = typeAttr
                                        }
                                    }
                                }
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (tagName.equals("item", ignoreCase = true)) {
                                insideItem = false
                                // Ensure the episode has a playable media URL before adding
                                if (mediaUrl.isNotBlank()) {
                                    val finalGuid = guid.ifBlank { mediaUrl.ifBlank { UUID.randomUUID().toString() } }
                                    val isVideo = isVideoMedia(mediaType, mediaUrl)
                                    episodes.add(
                                        Episode(
                                            guid = finalGuid,
                                            title = title.ifBlank { "Untitled Episode" },
                                            description = description.ifBlank { "No description available." },
                                            pubDate = pubDate,
                                            mediaUrl = mediaUrl,
                                            mediaType = mediaType,
                                            isVideo = isVideo
                                        )
                                    )
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }

        return episodes
    }

    /**
     * Determines whether the podcast feed is predominantly Audio, Video, or mixed Audio / Video.
     */
    fun determinePodcastType(feedUrl: String): String {
        return try {
            val episodes = parseFeed(feedUrl)
            if (episodes.isEmpty()) return "Unknown"

            val videoCount = episodes.count { it.isVideo }
            val audioCount = episodes.size - videoCount

            when {
                videoCount > 0 && audioCount == 0 -> "Video"
                videoCount == 0 && audioCount > 0 -> "Audio"
                videoCount > 0 && audioCount > 0 -> "Audio / Video"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Helper to classify whether a media stream is video based on MIME type and file extension.
     */
    private fun isVideoMedia(mediaType: String, mediaUrl: String): Boolean {
        if (mediaType.startsWith("video/", ignoreCase = true)) return true
        if (mediaType.startsWith("audio/", ignoreCase = true)) return false

        val cleanUrl = mediaUrl.substringBefore("?").lowercase()
        val videoExtensions = listOf(".mp4", ".m4v", ".mov", ".webm", ".mkv", ".3gp")
        return videoExtensions.any { cleanUrl.endsWith(it) }
    }

    /**
     * Safely reads text from the current XML tag without throwing if tag is empty or malformed.
     */
    private fun XmlPullParser.safeNextText(): String {
        return try {
            nextText()
        } catch (e: Exception) {
            ""
        }
    }
}
