package ro.touristsoara.logic

import ro.touristsoara.data.LatLngPoint
import ro.touristsoara.data.Poi

enum class ScheduleStatus {
    ON_TRACK,   // Ești pe grafic ✅
    RUNNING_LATE // Întârzii ⚠️ (am intrat în buffer-ul de 15 min sau nu mai ajunge timpul)
}

data class TrackerState(
    val remainingMinutesUntilDeadline: Int,
    val estimatedMinutesNeededToFinish: Int,
    val status: ScheduleStatus,
    val arrivedAtPoi: Poi? = null, // Nenul când utilizatorul intră în raza de 50m a unui POI nevizitat
    val suggestedPoiToSkip: Poi? = null // P1: obiectivul pe care îl putem sări dacă întârziem
)

class GeofenceAndScheduleTracker(
    private val routePois: List<Poi>,
    private val destination: LatLngPoint,
    private val modeSpeedKmh: Double,
    private val totalAvailableMinutes: Int,
    private val startTimeEpochMillis: Long = System.currentTimeMillis()
) {
    companion object {
        const val GEOFENCE_RADIUS_METERS = 50.0
    }

    private val visitedPoiIds = mutableSetOf<String>()

    fun updateLocation(
        userLocation: LatLngPoint,
        currentTimeEpochMillis: Long = System.currentTimeMillis()
    ): TrackerState {
        // 1. Verificăm dacă am ajuns la un POI nevizitat (<= 50 m)
        var newlyArrivedPoi: Poi? = null
        for (poi in routePois) {
            if (poi.id !in visitedPoiIds) {
                val dist = GeoUtils.distanceMeters(userLocation, LatLngPoint(poi.lat, poi.lng))
                if (dist <= GEOFENCE_RADIUS_METERS) {
                    visitedPoiIds.add(poi.id)
                    newlyArrivedPoi = poi
                    break
                }
            }
        }

        // 2. Calculăm timpul rămas până la plecarea trenului / deadline
        val elapsedMinutes = ((currentTimeEpochMillis - startTimeEpochMillis) / 60000L).toInt()
        val remainingMinutes = (totalAvailableMinutes - elapsedMinutes).coerceAtLeast(0)

        // 3. Calculăm cât timp mai avem nevoie pentru POI-urile rămase + drumul spre destinație
        val unvisitedPois = routePois.filter { it.id !in visitedPoiIds }
        var pos = userLocation
        var neededMinutes = 0

        for (poi in unvisitedPois) {
            val poiPos = LatLngPoint(poi.lat, poi.lng)
            neededMinutes += GeoUtils.estimateTravelMinutes(pos, poiPos, modeSpeedKmh)
            neededMinutes += poi.visitMinutes
            pos = poiPos
        }
        neededMinutes += GeoUtils.estimateTravelMinutes(pos, destination, modeSpeedKmh)

        // Dacă timpul necesar depășește timpul rămas minus 5 min marjă critică -> RUNNING_LATE
        val isLate = neededMinutes > remainingMinutes
        val status = if (isLate) ScheduleStatus.RUNNING_LATE else ScheduleStatus.ON_TRACK

        return TrackerState(
            remainingMinutesUntilDeadline = remainingMinutes,
            estimatedMinutesNeededToFinish = neededMinutes,
            status = status,
            arrivedAtPoi = newlyArrivedPoi,
            suggestedPoiToSkip = if (isLate) unvisitedPois.lastOrNull() else null
        )
    }

    fun markVisitedManually(poiId: String) {
        visitedPoiIds.add(poiId)
    }
}
