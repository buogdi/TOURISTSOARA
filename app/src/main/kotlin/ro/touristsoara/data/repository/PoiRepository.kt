package ro.touristsoara.data.repository

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ro.touristsoara.data.model.Poi

@Serializable
private data class RawPoi(
    val id: String,
    val name: String,
    val nameEn: String? = null,
    val address: String? = null,
    val description: String? = null,
    val descriptionEn: String? = null,
    val imagePath: String? = null,
    val pinPath: String? = null,
    val latitude: Double,
    val longitude: Double
)

@Serializable
private data class RawPoiResponse(
    val pois: List<RawPoi> = emptyList()
)

@Serializable
private data class RawOverrides(
    val visitMinutes: Map<String, Int> = emptyMap(),
    val disabled: List<String> = emptyList(),
    val extra: List<Poi> = emptyList()
)

class PoiRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var cachedPois: List<Poi>? = null

    fun getPois(): List<Poi> {
        cachedPois?.let { return it }

        // 1. Layer 1: City snapshot
        val cityPois = runCatching {
            val jsonString = context.assets.open("pois_city.json").bufferedReader().use { it.readText() }
            val response = json.decodeFromString<RawPoiResponse>(jsonString)
            response.pois.map { raw ->
                Poi(
                    id = raw.id,
                    name = raw.name,
                    nameEn = raw.nameEn,
                    address = raw.address,
                    description = raw.description,
                    descriptionEn = raw.descriptionEn,
                    imagePath = raw.imagePath,
                    lat = raw.latitude,
                    lng = raw.longitude,
                    visitMinutes = 10,
                    source = "city"
                )
            }
        }.getOrDefault(emptyList())

        // 2. Layer 2: Team overrides
        val overrides = runCatching {
            val jsonString = context.assets.open("pois_overrides.json").bufferedReader().use { it.readText() }
            json.decodeFromString<RawOverrides>(jsonString)
        }.getOrNull()

        val defaultVisit = overrides?.visitMinutes?.get("default") ?: 10
        val finalPois = cityPois.map { poi ->
            val visit = overrides?.visitMinutes?.get(poi.id) ?: defaultVisit
            val isDisabled = overrides?.disabled?.contains(poi.id) == true
            poi.copy(
                visitMinutes = visit,
                enabled = !isDisabled
            )
        }.toMutableList()

        overrides?.extra?.forEach { extraPoi ->
            finalPois.add(extraPoi.copy(source = "team"))
        }

        cachedPois = finalPois
        return finalPois
    }
}
