package ro.touristsoara.data

import kotlinx.serialization.Serializable

@Serializable
data class RouteSegment(
    val fromName: String,
    val toName: String,
    val mode: TransportMode,
    val distanceKm: Double,
    val durationMinutes: Int,
    val pathPoints: List<LatLngPoint> = emptyList(),
    val transitLineName: String? = null
)

@Serializable
data class RouteResult(
    val selectedPois: List<Poi>,
    val segments: List<RouteSegment>,
    val polylinePoints: List<LatLngPoint> = emptyList(),
    val totalWalkOrBikeMinutes: Int,
    val totalVisitMinutes: Int,
    val totalDurationMinutes: Int,
    val safetyBufferMinutes: Int = 15,
    val totalDistanceKm: Double,
    val isOfflineFallback: Boolean = true,
    val greenScore: Int = 0,
    val co2SavedGrams: Double = 0.0,
    val bikeLaneKm: Double = 0.0,
    val estimatedTicketCostRon: Int = 0
)
