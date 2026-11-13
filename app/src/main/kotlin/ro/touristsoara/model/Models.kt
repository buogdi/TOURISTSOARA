package ro.touristsoara.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class AiText(
    val history: String,
    val funFact: String,
    val mustSee: String,
    val isGrounded: Boolean = true
)

@Serializable
data class Poi(
    val id: String,
    val name: String,
    val nameEn: String? = null,
    val description: String? = null,
    val descriptionEn: String? = null,
    val address: String? = null,
    val imagePath: String? = null,
    val lat: Double,
    val lng: Double,
    val visitMinutes: Int = 10,
    val source: String = "city", // "city" | "team" | "user"
    var enabled: Boolean = true,
    var aiSummary: AiText? = null
) {
    fun getDisplayName(language: Language): String {
        return if (language == Language.EN && !nameEn.isNullOrBlank()) nameEn else name
    }

    fun getDisplayDescription(language: Language): String {
        return if (language == Language.EN && !descriptionEn.isNullOrBlank()) {
            descriptionEn
        } else {
            description ?: "Obiectiv turistic din Timișoara"
        }
    }
}

enum class Language(val label: String, val flag: String) {
    RO("Română", "🇷🇴"),
    EN("English", "🇬🇧")
}

enum class TravelMode(
    val titleRo: String,
    val titleEn: String,
    val iconEmoji: String,
    val avgSpeedKmH: Double,
    val co2GramsPerKm: Double
) {
    WALKING("Pe jos", "Walking", "🚶", 4.5, 0.0),
    CYCLING("Bicicletă / i'Velo", "Bicycle / i'Velo", "🚲", 14.0, 0.0),
    TRANSIT_MIX("Mix Transport Public (SMTT)", "Public Transit Mix", "🚋", 18.0, 42.0)
}

enum class Interest(
    val labelRo: String,
    val labelEn: String,
    val icon: String
) {
    HISTORY("Istorie & Monumente", "History & Monuments", "🏛️"),
    ARCHITECTURE("Arhitectură Secession / Baroc", "Architecture", "🏰"),
    NATURE("Parcuri & Malul Begăi", "Parks & Bega River", "🌳"),
    FAMILY("Familie & Relaxare", "Family & Leisure", "👨‍👩‍👧"),
    FOOD("Cafenele & Gastronomie", "Cafes & Dining", "☕")
}

@Serializable
data class RouteResult(
    val startName: String,
    val destinationName: String,
    val timeBudgetMinutes: Int,
    val estimatedDurationMinutes: Int,
    val stops: List<Poi>,
    val totalDistanceKm: Double,
    val bikeLaneKm: Double,
    val co2SavedGrams: Double,
    val greenScore: Int,
    val transitCostRon: Double,
    val travelMode: TravelMode,
    val geminiStoryRecap: String? = null
)
