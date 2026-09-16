# SuperPodcast 🎧

An advanced Android podcast streaming & discovery application built with Kotlin, Room Database, Media3 ExoPlayer, WorkManager, Retrofit, Coroutines, and Glide. SuperPodcast searches the public iTunes Search API, parses RSS XML feeds, manages local subscriptions in SQLite via Room, plays audio/video/HLS streams with ExoPlayer, and detects new episode releases in the background using WorkManager.

---

## 📱 Features

### 1. Podcast Discovery & Mood Scoring ("MoodCast")
- **iTunes Search Networking**: Connects to the public Apple iTunes Search API using Retrofit 2 and Kotlin Coroutines.
- **Mood Discovery Engine**: Search podcasts by mood vibe presets (☕ *Rainy Loft*, 🔥 *High Octane*, 🕵️ *True Noir 2AM*, 🧠 *Mind Lab*, 🍿 *Pop & Banter*) or custom free-text queries.
- **🎲 Surprise Vibe Dice Roll**: Interactive shuffle button that picks a random vibe with a smooth 360° vinyl spin animation.
- **Client-Side Vibe Scoring**: Analyzes titles, descriptions, and artist metadata against semantic keywords to calculate a 0–100% match score.
- **Dynamic Resonance Badges**: Visual percentage tags highlight how closely a podcast aligns with the selected vibe.

### 2. RSS XML Feed Parsing & Episode Management
- **Namespaced XML Pull Parsing**: Custom [PodcastRssParser] supporting plain RSS and namespaced tags (`<content:encoded>`, `<media:content>`, `<enclosure>`).
- **Media Type Detection**: Automatically classifies streams as Audio, Video, or mixed Audio / Video.
- **Episode List**: Displays full episode catalogs with publication dates, audio/video badges, and clean HTML show notes.

### 3. Media Streaming with ExoPlayer (Media3)
- **Audio, Video & HLS**: Full hardware-accelerated playback of MP3, MP4, and `.m3u8` HTTP Live Streaming (HLS) feeds.
- **Integrated Player Controls**: Embedded `androidx.media3.ui.PlayerView` with playback controls, buffering states, and aspect ratio fitting.
- **Lifecycle Resilient**: Player persists during screen rotation and pauses/releases cleanly.

### 4. Local Subscriptions with Room Database
- **SQLite Persistence**: Local `@Entity` and `@Dao` storing subscribed podcasts with iTunes track ID, title, artist, artwork, and RSS feed URL.
- **Reactive UI**: Subscriptions library observes Room via Kotlin Coroutines `Flow`, updating the list in real-time.
- **One-Tap Toggle**: Subscribe / unsubscribe button in the detail view immediately synchronizes with the database.

### 5. Background Updates & Notifications
- **WorkManager Scheduling**: Enqueues periodic (15-minute) and one-time background workers with active network constraints.
- **New-Episode Detection**: Compares latest episode GUIDs against `SharedPreferences` to detect newly published releases.
- **Dual Notification Strategy**:
  - **Foreground Alert**: Displays in-app Toast notifications via an unexported `BroadcastReceiver` when the app is active.
  - **Background Push Notification**: Posts system notification channel alerts on Android O+ with `BigTextStyle` expanders and click-to-detail navigation.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin
- **Database**: AndroidX Room 2.6.1 (with KSP annotation processing)
- **Media Playback**: AndroidX Media3 ExoPlayer 1.4.1 (ExoPlayer, HLS, UI)
- **Background Work**: AndroidX WorkManager 2.9.1 (`CoroutineWorker`)
- **Networking**: Retrofit 2.11.0 + Gson Converter
- **Image Loading**: Glide 4.16.0
- **Asynchronous Execution**: Kotlin Coroutines (`Dispatchers.IO`, `Dispatchers.Default`, `lifecycleScope`, `Flow`)
- **UI & Layouts**: ConstraintLayout, MaterialCardView, Material Chips, NestedScrollView, PlayerView, RecyclerView

---

## 📂 Project Structure

```
com.sheikhnaim2026.superpodcast/
├── data/
│   ├── AppDatabase.kt               # Room database singleton
│   ├── Episode.kt                   # Parsed RSS episode data model
│   ├── ITunesApi.kt                 # Retrofit Search API interface
│   ├── Mood.kt                      # Mood category model
│   ├── MoodCatalog.kt               # Predefined mood presets & dice generator
│   ├── NotificationHelper.kt        # Notification channels & push builder
│   ├── PodcastResponse.kt           # Gson models for iTunes API
│   ├── PodcastRssParser.kt          # XMLPullParser with namespace support
│   ├── PodcastUpdateScheduler.kt    # WorkManager periodic scheduler
│   ├── PodcastUpdateWorker.kt       # CoroutineWorker for update detection
│   ├── ScoredPodcast.kt             # Model pairing Podcast with match score
│   ├── SubscribedPodcast.kt         # Room @Entity for subscriptions
│   └── SubscriptionDao.kt           # Room @Dao for database queries
├── logic/
│   └── MoodScorer.kt                # Keyword density scoring & ranking engine
└── ui/
    ├── EpisodeAdapter.kt            # RecyclerView adapter for episodes
    ├── MainActivity.kt              # Main discovery, search, & notifications
    ├── PodcastAdapter.kt            # RecyclerView adapter for search results
    ├── PodcastDetailActivity.kt     # Detail view with ExoPlayer & subscribe toggle
    ├── SubscriptionAdapter.kt       # RecyclerView adapter for subscriptions
    └── SubscriptionsActivity.kt     # Subscriptions library screen
```

---

## 🚀 Getting Started

1. Clone this repository:
   ```bash
   git clone https://github.com/snaimio/AndroidApp4.git
   ```
2. Open the project in **Android Studio**.
3. Allow Gradle to sync dependencies.
4. Run on an Android Emulator or physical device (`API 24+` / Android 7.0+).

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
