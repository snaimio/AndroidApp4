package com.sheikhnaim2026.superpodcast.data

// MoodCatalog: Repository holding preset mood categories and helper methods.
// Acts as the dictionary for mapping user mood vibes into search queries and scoring keywords.
object MoodCatalog {

    // Predefined mood categories with curated semantic keywords
    val predefinedMoods: List<Mood> = listOf(
        // Cozy mood: searches for relaxing shows and looks for sleep/quiet/rain terms
        Mood(
            id = "cozy",
            label = "☕ Rainy Loft",
            seedTerm = "relaxing",
            keywords = listOf("cozy", "calm", "sleep", "rain", "quiet", "gentle", "soft", "peaceful", "bedtime", "ambient", "loft")
        ),
        // High intensity mood: searches for fitness shows and looks for workout/energy terms
        Mood(
            id = "gym",
            label = "🔥 High Octane",
            seedTerm = "fitness",
            keywords = listOf("intense", "power", "strength", "energy", "hardcore", "burn", "workout", "hustle", "beast", "training", "drive")
        ),
        // Mystery/True crime mood: searches for true crime shows and looks for dark/case terms
        Mood(
            id = "crime",
            label = "🕵️ True Noir 2AM",
            seedTerm = "true crime",
            keywords = listOf("crime", "murder", "mystery", "dark", "unsolved", "case", "investigation", "secret", "killer", "suspense", "noir")
        ),
        // Study/Productivity mood: searches for productivity shows and looks for tech/code terms
        Mood(
            id = "focus",
            label = "🧠 Mind Lab",
            seedTerm = "productivity",
            keywords = listOf("focus", "study", "mindset", "code", "tech", "science", "philosophy", "lofi", "deep work", "learning", "brain")
        ),
        // Comedy mood: searches for comedy shows and looks for funny/banter terms
        Mood(
            id = "comedy",
            label = "🍿 Pop & Banter",
            seedTerm = "comedy",
            keywords = listOf("funny", "laugh", "humor", "silly", "joke", "hilarious", "standup", "banter", "improv", "satire", "pop")
        )
    )

    // Searches for a predefined mood matching either the mood ID or the display label
    fun findByLabelOrId(query: String): Mood? {
        val normalized = query.trim().lowercase()

        // 1. Try matching the exact ID first (e.g. "cozy", "gym")
        predefinedMoods.firstOrNull { it.id.equals(normalized, ignoreCase = true) }?.let { return it }

        // 2. Try matching the label text with emojis stripped out
        predefinedMoods.firstOrNull { 
            val cleanLabel = it.label.replace(Regex("[^a-zA-Z0-9 /]"), "").trim().lowercase()
            cleanLabel.equals(normalized, ignoreCase = true) 
        }?.let { return it }

        // 3. Fallback to substring search if query is at least 3 characters long
        if (normalized.length >= 3) {
            predefinedMoods.firstOrNull { it.label.lowercase().contains(normalized) }?.let { return it }
        }

        return null
    }

    // Picks a random mood preset for the "Surprise Vibe" dice roll feature
    fun getRandomMood(): Mood {
        return predefinedMoods.random()
    }

    // Creates a custom mood on the fly when the user enters free-form text in the search bar
    fun fromFreeText(text: String): Mood {
        val trimmed = text.trim()
        // Split text into individual words for scoring keywords
        val splitWords = trimmed.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
        return Mood(
            id = "custom",
            label = trimmed,
            seedTerm = trimmed,
            keywords = if (splitWords.isNotEmpty()) splitWords else listOf(trimmed.lowercase())
        )
    }
}
