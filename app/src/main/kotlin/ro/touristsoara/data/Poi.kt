package ro.touristsoara.data

import kotlinx.serialization.Serializable

@Serializable
data class AiText(
    val history: String,
    val funFact: String,
    val mustSee: String
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
    fun toLatLng(): LatLngPoint = LatLngPoint(lat, lng)
}
