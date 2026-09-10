# SuperPodcast 🎧

An Android podcast discovery application built with Kotlin, Retrofit, Coroutines, and Glide. SuperPodcast searches the public iTunes Search API and discovers podcasts based on emotional mood criteria and semantic keyword scoring.

## 📱 Features

- **iTunes Search Networking**: Connects to the public iTunes Search API using Retrofit and Kotlin Coroutines.
- **Mood Discovery Engine**: Search podcasts by mood vibe presets (☕ *Cozy Rain*, 🔥 *Beast Mode*, 🕵️ *Midnight Mystery*, 💡 *Deep Focus*, 😂 *Stand-Up & Laughs*) or custom free-text queries.
- **Client-Side Vibe Scoring**: Analyzes titles, descriptions, and artist metadata against semantic keywords to calculate a 0–100% match score.
- **Dynamic Color Badges**: Visual percentage pill tags highlight how closely a podcast aligns with the selected vibe.
- **Modern Dark UI**: Designed with a sleek dark slate streaming aesthetic, squircle album art, and interactive empty states.

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **Networking**: Retrofit 2 + Gson Converter
- **Image Loading**: Glide 4
- **Asynchronous Processing**: Kotlin Coroutines (`Dispatchers.IO`, `Dispatchers.Default`, `lifecycleScope`)
- **UI Components**: RecyclerView, MaterialCardView, Material Chips, ConstraintLayout
- **Architecture**: Clean separation of Data (`ITunesApi`, models), Logic (`MoodScorer`), and UI (`MainActivity`, `PodcastAdapter`)

## 🚀 Getting Started

1. Clone this repository:
   ```bash
   git clone https://github.com/snaimio/AndroidApp4.git
   ```
2. Open the project in **Android Studio**.
3. Allow Gradle to sync dependencies.
4. Run on an Android Emulator or physical device (`API 24+`).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
