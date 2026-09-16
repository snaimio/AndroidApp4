# SuperPodcast 🎧

SuperPodcast is an Android podcast discovery and streaming application built with modern Android architecture components. It combines intelligent mood-based discovery via the Apple iTunes Search API with full RSS feed management, SQLite local subscription persistence via Room, audio/video/HLS streaming with Media3 ExoPlayer, and background update detection powered by WorkManager.

---

## 📱 Implemented Features

### Search & Discovery
- **iTunes Search API Integration**: Searches iTunes podcast catalog with Retrofit and Kotlin Coroutines.
- **Mood Scoring & Ranking**: Evaluates show titles, artist information, and episode descriptions against curated semantic keywords to rank podcasts by vibe percentage (0–100%).
- **Interactive Mood Chips**: Quick-tap choice chips for instant vibes (☕ *Rainy Loft*, 🔥 *High Octane*, 🕵️ *True Noir 2AM*, 🧠 *Mind Lab*, 🍿 *Pop & Banter*).
- **Surprise Vibe Dice Roll**: Generates randomized mood queries with an interactive vinyl record animation.

### RSS & Episode Management
- **Namespaced XML Pull Parser**: Custom XML parser supporting standard RSS tags (`<title>`, `<guid>`, `<pubDate>`, `<enclosure>`) and namespaced extensions (`<content:encoded>`, `<media:content>`).
- **Media Type Detection**: Detects and categorizes streams as Audio or Video using MIME types and file extensions.
- **Rich Episode Catalog**: Displays scrollable episode lists with formatted release dates, media type badges, and cleaned HTML show notes.

### Playback
- **Media3 ExoPlayer Streaming**: Embedded media player supporting high-performance audio and video playback.
- **Adaptive HLS Support**: Seamlessly streams HTTP Live Streaming (`.m3u8` / `application/x-mpegURL`) feeds.
- **Lifecycle Preservation**: Audio streaming survives orientation and configuration changes, releasing hardware resources in `onDestroy()`.

### Subscriptions
- **Room Database Persistence**: Stores subscribed podcasts in a local SQLite database with unique iTunes track IDs.
- **Reactive UI Flow**: Observes database changes in real-time via Kotlin Coroutines `Flow`.
- **Subscriptions Library**: Dedicated screen displaying all subscribed podcasts sorted alphabetically (A–Z) with direct click-to-detail navigation.
- **Subscribe / Unsubscribe Toggle**: One-tap toggle button on the detail view that instantly syncs database state.

### Notifications
- **Notification Channel (API 26+)**: Configures dedicated "Podcast Updates" channel with default priority.
- **Runtime Permission Handling (API 33+)**: Prompts for `POST_NOTIFICATIONS` runtime permission on Android 13+.
- **Dual Notification Strategy**:
  - **Foreground Banner**: Unexported in-app `BroadcastReceiver` triggers Toast notifications when the app is active.
  - **Background Push**: Dispatches system tray notifications with `BigTextStyle` expanders and PendingIntent navigation when closed or backgrounded.

### Background Work
- **WorkManager Periodic & One-Time Tasks**: Enqueues 15-minute periodic update checks and immediate one-time sync with network connectivity constraints.
- **GUID Tracking with SharedPreferences**: Tracks the latest episode GUID per subscribed podcast to detect newly published episodes without duplicate alerts on initial subscription.

---

## 🛠️ Built With

- **Kotlin**: Core language
- **Room**: Local database persistence (SQLite) with KSP annotation processing
- **WorkManager**: Background periodic & one-time tasks
- **Media3 ExoPlayer**: Audio, video, and HLS media streaming
- **Retrofit**: Network REST client for iTunes Search API
- **Glide**: Image loading and caching for podcast cover art
- **Material Components**: Modern dark-theme UI components, cards, chips, and toolbars

---

## 🚀 How to Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/snaimio/AndroidApp4.git
   ```
2. **Open in Android Studio**: Open Android Studio and select **File > Open**, then choose the cloned `AndroidApp4` directory.
3. **Sync Gradle**: Allow Android Studio to sync Gradle dependencies and build the project index.
4. **Run**: Select an emulator or connected physical device running **Android 13+ (API 33+)** (or any device running API 24+) and click **Run 'app'** (`Shift + F10`).

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
