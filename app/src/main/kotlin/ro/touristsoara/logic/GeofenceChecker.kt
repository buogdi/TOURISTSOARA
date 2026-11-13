package ro.touristsoara.logic

import ro.touristsoara.data.LatLngPoint
import ro.touristsoara.model.Poi

/**
 * Rezultatul detectării apropierii de un obiectiv turistic.
 */
data class ArrivalResult(
    val poi: Poi,
    val distanceMeters: Double
)

/**
 * Gestionează detectarea apropierii de obiective turistice (raza de 50 m)
 * și monitorizarea obiectivelor deja vizitate pentru a evita declanșările multiple.
 */
class GeofenceChecker(
    private val arrivalThresholdMeters: Double = DEFAULT_ARRIVAL_THRESHOLD_METERS
) {
    companion object {
        const val DEFAULT_ARRIVAL_THRESHOLD_METERS = 50.0

        /**
         * Verifică dacă locația utilizatorului este în raza de sosire la un obiectiv (<= 50m).
         */
        fun isArrived(
            userLocation: LatLngPoint,
            targetLocation: LatLngPoint,
            thresholdMeters: Double = DEFAULT_ARRIVAL_THRESHOLD_METERS
        ): Boolean {
            return GeoUtils.distanceMeters(userLocation, targetLocation) <= thresholdMeters
        }

        /**
         * Distanța rămasă în metri până la obiectiv.
         */
        fun distanceToTargetMeters(
            userLocation: LatLngPoint,
            targetLocation: LatLngPoint
        ): Double {
            return GeoUtils.distanceMeters(userLocation, targetLocation)
        }
    }

    private val visitedPoiIds = mutableSetOf<String>()

    /**
     * Verifică dacă utilizatorul a ajuns la un obiectiv nevizitat (distanță <= threshold).
     * Dacă mai multe obiective sunt în raza de 50 m, îl returnează pe cel mai apropiat.
     */
    fun checkArrival(
        userLat: Double,
        userLng: Double,
        pois: List<Poi>
    ): ArrivalResult? {
        val eligiblePois = pois.filter { it.enabled && !isVisited(it.id) }
        var closestResult: ArrivalResult? = null

        for (poi in eligiblePois) {
            val dist = getDistanceToPoi(userLat, userLng, poi)
            if (dist <= arrivalThresholdMeters) {
                if (closestResult == null || dist < closestResult.distanceMeters) {
                    closestResult = ArrivalResult(poi, dist)
                }
            }
        }

        return closestResult
    }

    /**
     * Calculează distanța de la utilizator la un POI anume.
     */
    fun getDistanceToPoi(userLat: Double, userLng: Double, poi: Poi): Double {
        return GeoUtils.distanceMeters(LatLngPoint(userLat, userLng), LatLngPoint(poi.lat, poi.lng))
    }

    /**
     * Găsește cel mai apropiat obiectiv din listă și distanța până la el (indiferent de threshold).
     */
    fun findNearestPoi(
        userLat: Double,
        userLng: Double,
        pois: List<Poi>
    ): Pair<Poi, Double>? {
        val enabledPois = pois.filter { it.enabled }
        if (enabledPois.isEmpty()) return null

        var nearestPoi: Poi? = null
        var minDistance = Double.MAX_VALUE

        for (poi in enabledPois) {
            val dist = getDistanceToPoi(userLat, userLng, poi)
            if (dist < minDistance) {
                minDistance = dist
                nearestPoi = poi
            }
        }

        return nearestPoi?.let { it to minDistance }
    }

    /**
     * Marchează un POI ca fiind vizitat.
     */
    fun markVisited(poiId: String) {
        visitedPoiIds.add(poiId)
    }

    /**
     * Verifică dacă un POI a fost deja vizitat.
     */
    fun isVisited(poiId: String): Boolean {
        return visitedPoiIds.contains(poiId)
    }

    /**
     * Resetează lista de obiective vizitate (de ex. la începerea unui nou tur).
     */
    fun resetVisited() {
        visitedPoiIds.clear()
    }

    /**
     * Numărul total de obiective vizitate până acum.
     */
    fun getVisitedCount(): Int = visitedPoiIds.size

    /**
     * Setul de ID-uri vizitate.
     */
    fun getVisitedIds(): Set<String> = visitedPoiIds.toSet()
}
