package ro.touristsoara.logic

import ro.touristsoara.data.*

class RoutePlanner {

    companion object {
        const val SAFETY_BUFFER_MINUTES = 15
    }

    /**
     * Pasul 1: Selectează obiectivele prin algoritmul Greedy (fără apeluri API).
     */
    fun selectPoisGreedy(
        start: LatLngPoint,
        destination: LatLngPoint,
        availableTimeMinutes: Int,
        mode: TransportMode,
        activePois: List<Poi>
    ): List<Poi> {
        var remainingMinutes = availableTimeMinutes - SAFETY_BUFFER_MINUTES
        if (remainingMinutes <= 0) return emptyList()

        val speed = if (mode == TransportMode.BIKE) TransportMode.BIKE.speedKmh else TransportMode.WALK.speedKmh

        // Verificăm dacă măcar drumul direct start -> destinație încape în timp
        val directTime = GeoUtils.estimateTravelMinutes(start, destination, speed)
        if (directTime > remainingMinutes) return emptyList()

        var currentPos = start
        val selected = mutableListOf<Poi>()
        val candidates = activePois.filter { it.enabled }.toMutableList()

        while (true) {
            // Filtru: t(pozitie -> c) + c.visitMinutes + t(c -> destinatie) <= timpRamas
            val validCandidates = candidates.filter { c ->
                val cPos = LatLngPoint(c.lat, c.lng)
                val toCandidate = GeoUtils.estimateTravelMinutes(currentPos, cPos, speed)
                val toDestination = GeoUtils.estimateTravelMinutes(cPos, destination, speed)
                (toCandidate + c.visitMinutes + toDestination) <= remainingMinutes
            }

            if (validCandidates.isEmpty()) break

            // Alegem candidatul cel mai apropiat de poziția curentă
            val bestPoi = validCandidates.minByOrNull { c ->
                GeoUtils.estimateTravelMinutes(currentPos, LatLngPoint(c.lat, c.lng), speed)
            } ?: break

            val bestPos = LatLngPoint(bestPoi.lat, bestPoi.lng)
            val travelToBest = GeoUtils.estimateTravelMinutes(currentPos, bestPos, speed)

            selected.add(bestPoi)
            candidates.remove(bestPoi)
            remainingMinutes -= (travelToBest + bestPoi.visitMinutes)
            currentPos = bestPos
        }

        return selected
    }

    /**
     * Pasul 2: Construiește rezultatul final.
     * Dacă OpenRouteService (ORS) returnează o durată reală care depășește bugetul,
     * apelăm `trimToFitBudget` care scoate ultimul obiectiv.
     */
    fun buildFallbackRoute(
        start: LatLngPoint,
        destination: LatLngPoint,
        destinationName: String,
        selectedPois: List<Poi>,
        mode: TransportMode,
        bikeLanePolylines: List<List<LatLngPoint>> = emptyList()
    ): RouteResult {
        val waypoints = mutableListOf<Pair<String, LatLngPoint>>()
        waypoints.add("Start" to start)
        selectedPois.forEach { waypoints.add(it.name to LatLngPoint(it.lat, it.lng)) }
        waypoints.add(destinationName to destination)

        val speed = if (mode == TransportMode.BIKE) TransportMode.BIKE.speedKmh else TransportMode.WALK.speedKmh
        val segments = mutableListOf<RouteSegment>()
        val polyline = waypoints.map { it.second }

        for (i in 0 until waypoints.size - 1) {
            val from = waypoints[i]
            val to = waypoints[i + 1]
            val distKm = GeoUtils.estimatedUrbanDistanceKm(from.second, to.second)
            val mins = GeoUtils.estimateTravelMinutes(from.second, to.second, speed)
            segments.add(
                RouteSegment(
                    fromName = from.first,
                    toName = to.first,
                    mode = if (mode == TransportMode.BIKE) TransportMode.BIKE else TransportMode.WALK,
                    distanceKm = distKm,
                    durationMinutes = mins,
                    pathPoints = listOf(from.second, to.second)
                )
            )
        }

        val walkOrBikeMins = segments.sumOf { it.durationMinutes }
        val visitMins = selectedPois.sumOf { it.visitMinutes }
        val totalDistKm = segments.sumOf { it.distanceKm }

        val greenRes = GreenScoreCalculator.calculate(segments, bikeLanePolylines, polyline)

        return RouteResult(
            selectedPois = selectedPois,
            segments = segments,
            polylinePoints = polyline,
            totalWalkOrBikeMinutes = walkOrBikeMins,
            totalVisitMinutes = visitMins,
            totalDurationMinutes = walkOrBikeMins + visitMins,
            safetyBufferMinutes = SAFETY_BUFFER_MINUTES,
            totalDistanceKm = totalDistKm,
            isOfflineFallback = true,
            greenScore = greenRes.greenScore,
            co2SavedGrams = greenRes.co2SavedGrams.toDouble(),
            bikeLaneKm = greenRes.bikeLaneKm,
            estimatedTicketCostRon = greenRes.estimatedTicketCostRon
        )
    }

    /**
     * Dacă după apelul OpenRouteService durata reală + vizitele > timpDisponibil - 15 min,
     * scoatem ultimul obiectiv până ne încadrăm.
     */
    fun shouldDropLastPoi(
        orsTravelMinutes: Int,
        selectedPois: List<Poi>,
        availableTimeMinutes: Int
    ): Boolean {
        val totalVisit = selectedPois.sumOf { it.visitMinutes }
        return (orsTravelMinutes + totalVisit) > (availableTimeMinutes - SAFETY_BUFFER_MINUTES)
    }
}
