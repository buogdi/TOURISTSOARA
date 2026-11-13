package ro.touristsoara.logic

import ro.touristsoara.data.LatLngPoint
import kotlin.math.*

object GeoUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0
    private const val URBAN_DETOUR_FACTOR = 1.3 // Factor de ocolire stradală din secțiunea 8

    /** Distanța în linie dreaptă în metri (Haversine) */
    fun distanceMeters(p1: LatLngPoint, p2: LatLngPoint): Double {
        val dLat = Math.toRadians(p2.lat - p1.lat)
        val dLng = Math.toRadians(p2.lng - p1.lng)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.lat)) * cos(Math.toRadians(p2.lat)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /** Distanța estimată pe străzi în km (Haversine * 1.3) */
    fun estimatedUrbanDistanceKm(p1: LatLngPoint, p2: LatLngPoint): Double {
        return (distanceMeters(p1, p2) / 1000.0) * URBAN_DETOUR_FACTOR
    }

    /** Timpul estimat în minute între 2 puncte pentru algoritmul Greedy */
    fun estimateTravelMinutes(p1: LatLngPoint, p2: LatLngPoint, speedKmh: Double): Int {
        val distKm = estimatedUrbanDistanceKm(p1, p2)
        return ceil((distKm / speedKmh) * 60.0).toInt()
    }
}
