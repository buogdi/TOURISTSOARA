package ro.touristsoara.logic

import ro.touristsoara.data.*

class TransitPlanner(
    private val stops: List<TransitStop>,
    private val routes: Map<String, TransitRouteInfo>
) {
    /**
     * Încearcă să înlocuiască un segment de mers pe jos (> 1.5 km) cu o linie directă SMTT.
     * Conform regulilor din ghid: NU se aplică pentru bicicletă (bikes_allowed = 2).
     */
    fun findDirectTransitSegmentOrNull(
        from: LatLngPoint,
        fromName: String,
        to: LatLngPoint,
        toName: String
    ): RouteSegment? {
        val straightDistKm = GeoUtils.estimatedUrbanDistanceKm(from, to)
        if (straightDistKm < 1.5) return null // Prea scurt, mergem pe jos

        // 1. Căutăm stațiile la <= 500m de plecare și sosire
        val nearStartStops = stops.filter {
            GeoUtils.distanceMeters(from, LatLngPoint(it.lat, it.lon)) <= 500.0
        }
        val nearEndStops = stops.filter {
            GeoUtils.distanceMeters(to, LatLngPoint(it.lat, it.lon)) <= 500.0
        }

        for (stopA in nearStartStops) {
            for (stopB in nearEndStops) {
                if (stopA.id == stopB.id) continue
                val commonRoutes = stopA.routeIds.intersect(stopB.routeIds.toSet())

                for (routeId in commonRoutes) {
                    val routeInfo = routes[routeId] ?: continue
                    val idxA = routeInfo.orderedStopIds.indexOf(stopA.id)
                    val idxB = routeInfo.orderedStopIds.indexOf(stopB.id)

                    // 2. Verificăm direcția: A trebuie să apară înaintea lui B
                    if (idxA != -1 && idxB != -1 && idxA < idxB) {
                        val stopsCount = idxB - idxA
                        val walkToA = GeoUtils.estimateTravelMinutes(from, LatLngPoint(stopA.lat, stopA.lon), 5.0)
                        val walkFromB = GeoUtils.estimateTravelMinutes(LatLngPoint(stopB.lat, stopB.lon), to, 5.0)
                        val waitMinutes = 6 // Timp mediu așteptare dacă orarul nu e încărcat
                        val rideMinutes = stopsCount * 2 // ~2 min / stație conform Secțiunii 6.4

                        return RouteSegment(
                            fromName = "$fromName (prin stația ${stopA.name})",
                            toName = "$toName (stația ${stopB.name})",
                            mode = TransportMode.TRANSIT,
                            distanceKm = straightDistKm,
                            durationMinutes = walkToA + waitMinutes + rideMinutes + walkFromB,
                            transitLineName = "Linia ${routeInfo.shortName}",
                            pathPoints = listOf(from, LatLngPoint(stopA.lat, stopA.lon), LatLngPoint(stopB.lat, stopB.lon), to)
                        )
                    }
                }
            }
        }
        return null // Fără linie directă -> mergem pe jos
    }
}
