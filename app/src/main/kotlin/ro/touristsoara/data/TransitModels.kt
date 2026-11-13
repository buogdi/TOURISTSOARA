package ro.touristsoara.data

import kotlinx.serialization.Serializable

@Serializable
data class TransitStop(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val routeIds: List<String> = emptyList()
) {
    fun toLatLng(): LatLngPoint = LatLngPoint(lat, lon)
}

@Serializable
data class TransitRouteInfo(
    val routeId: String,
    val shortName: String, // ex: "1", "E4", "M14"
    val type: Int,         // 0 = tramvai, 11 = troleibuz, 3 = autobuz
    val orderedStopIds: List<String> = emptyList()
)

@Serializable
data class TransitRoute(
    val id: String,
    val name: String,
    val shortName: String,
    val type: Int, // 0 = Tram, 11 = Trolleybus, 3 = Bus
    val color: String? = null,
    val pathEncoded: String? = null
)

@Serializable
data class DirectTransitOption(
    val routeId: String,
    val routeName: String,
    val boardStop: TransitStop,
    val alightStop: TransitStop,
    val walkToBoardKm: Double,
    val transitDistanceKm: Double,
    val walkFromAlightKm: Double,
    val estimatedTotalMinutes: Int
)
