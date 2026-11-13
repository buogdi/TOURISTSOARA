package ro.touristsoara.logic

import ro.touristsoara.data.LatLngPoint

object GeofenceChecker {
    private const val DEFAULT_ARRIVAL_THRESHOLD_METERS = 50.0

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
