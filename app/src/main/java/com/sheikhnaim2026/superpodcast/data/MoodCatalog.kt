package com.sheikhnaim2026.superpodcast.data

// Curated list of distinct moods with semantic search keywords
object MoodCatalog {

    // Predefined mood categories with related keywords for scoring
    val predefinedMoods: List<Mood> = listOf(
        Mood(
            id = "cozy",
            label = "☕ Rainy Loft",
            seedTerm = "relaxing",
            keywords = listOf("cozy", "calm", "sleep", "rain", "quiet", "gentle", "soft", "peaceful", "bedtime", "ambient", "loft")
        ),
        Mood(
            id = "gym",
            label = "🔥 High Octane",
            seedTerm = "fitness",
            keywords = listOf("intense", "power", "strength", "energy", "hardcore", "burn", "workout", "hustle", "beast", "training", "drive")
        ),
        Mood(
            id = "crime",
            label = "🕵️ True Noir 2AM",
            seedTerm = "true crime",
            keywords = listOf("crime", "murder", "mystery", "dark", "unsolved", "case", "investigation", "secret", "killer", "suspense", "noir")
        ),
        Mood(
            id = "focus",
            label = "🧠 Mind Lab",
            seedTerm = "productivity",
            keywords = listOf("focus", "study", "mindset", "code", "tech", "science", "philosophy", "lofi", "deep work", "learning", "brain")
        ),
        Mood(
            id = "comedy",
            label = "🍿 Pop & Banter",
            seedTerm = "comedy",
            keywords = listOf("funny", "laugh", "humor", "silly", "joke", "hilarious", "standup", "banter", "improv", "satire", "pop")
        )
    )

    // Find a preset mood by its id or text
    fun findByLabelOrId(query: String): Mood? {
        val normalized = query.trim().lowercase()

        // Check exact ID match first
        predefinedMoods.firstOrNull { it.id.equals(normalized, ignoreCase = true) }?.let { return it }

        // Check label without emoji characters
        predefinedMoods.firstOrNull { 
            val cleanLabel = it.label.replace(Regex("[^a-zA-Z0-9 /]"), "").trim().lowercase()
            cleanLabel.equals(normalized, ignoreCase = true) 
        }?.let { return it }

        // Check if query is inside the label text
        if (normalized.length >= 3) {
            predefinedMoods.firstOrNull { it.label.lowercase().contains(normalized) }?.let { return it }
        }

        return null
    }

    // Returns a random mood from the catalog for the surprise dice roll
    fun getRandomMood(): Mood {
        return predefinedMoods.random()
    }

    // Create a dynamic mood if the user types custom search text
    fun fromFreeText(text: String): Mood {
        val trimmed = text.trim()
        val splitWords = trimmed.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
        return Mood(
            id = "custom",
            label = trimmed,
            seedTerm = trimmed,
            keywords = if (splitWords.isNotEmpty()) splitWords else listOf(trimmed.lowercase())
        )
    }
}
