package ro.touristsoara.logic

import ro.touristsoara.data.LatLngPoint
import ro.touristsoara.data.RouteSegment
import kotlin.math.roundToInt

data class GreenMetricsResult(
    val totalDistanceKm: Double,
    val bikeLaneKm: Double,
    val co2SavedGrams: Int,
    val greenScore: Int,
    val estimatedTicketCostRon: Int, // P1: 5 lei (60 min) daca s-a folosit SMTT
    val methodologyCitationRo: String,
    val methodologyCitationEn: String
)

object GreenScoreCalculator {

    // Sursă: European Environment Agency (EEA) - medie pasager-km în mediul urban
    const val CAR_CO2_GRAMS_PER_KM = 120.0
    const val TRANSIT_CO2_GRAMS_PER_KM = 45.0
    const val BIKE_LANE_PROXIMITY_METERS = 15.0

    fun calculate(
        segments: List<RouteSegment>,
        bikeLanePolylines: List<List<LatLngPoint>> = emptyList(),
        routePolyline: List<LatLngPoint> = emptyList()
    ): GreenMetricsResult {
        val totalKm = segments.sumOf { it.distanceKm }

        // co2_economisit_g = km_traseu * factor_masina - sum(km_segment * factor_mod_segment)
        val carBaselineGrams = totalKm * CAR_CO2_GRAMS_PER_KM
        val actualRouteGrams = segments.sumOf { seg ->
            seg.distanceKm * seg.mode.co2GramsPerKm
        }
        val co2SavedGrams = (carBaselineGrams - actualRouteGrams).coerceAtLeast(0.0).roundToInt()

        // Calculăm câți km din traseu sunt la < 15m de o pistă de biciclete OSM
        val bikeLaneKm = calculateBikeLaneOverlapKm(routePolyline, bikeLanePolylines)

        // Green Score = round(co2_economisit_g / 10) + 2 * km_pe_pista_de_biciclete
        val greenScore = (co2SavedGrams / 10.0 + 2.0 * bikeLaneKm).roundToInt()

        // Cost bilet SMTT (P1: din /api/mobile/wallet/types -> 5 RON bilet 60 min)
        val usedTransit = segments.any { it.transitLineName != null || it.mode == ro.touristsoara.data.TransportMode.TRANSIT }
        val ticketCostRon = if (usedTransit) 5 else 0

        return GreenMetricsResult(
            totalDistanceKm = (totalKm * 10.0).roundToInt() / 10.0,
            bikeLaneKm = (bikeLaneKm * 10.0).roundToInt() / 10.0,
            co2SavedGrams = co2SavedGrams,
            greenScore = greenScore,
            estimatedTicketCostRon = ticketCostRon,
            methodologyCitationRo = "Calcul bazat pe factorii medii de emisie EEA (Agenția Europeană de Mediu): 120 g CO₂/pasager-km pentru autoturism, 45 g CO₂/pasager-km pentru transport public (SMTT) și 0 g pentru mers pe jos/bicicletă.",
            methodologyCitationEn = "Calculated using European Environment Agency (EEA) average emission factors: 120 g CO₂/passenger-km for passenger cars, 45 g CO₂/passenger-km for public transit (SMTT), and 0 g for walking/cycling."
        )
    }

    /**
     * Aproximează lungimea porțiunilor din traseu aflate la mai puțin de 15 m
     * de orice nod al unei piste de biciclete din bike_lanes.geojson.
     */
    private fun calculateBikeLaneOverlapKm(
        routePoints: List<LatLngPoint>,
        bikeLanes: List<List<LatLngPoint>>
    ): Double {
        if (routePoints.size < 2 || bikeLanes.isEmpty()) return 0.0
        val allBikeNodes = bikeLanes.flatten()
        if (allBikeNodes.isEmpty()) return 0.0

        var overlapMeters = 0.0
        for (i in 0 until routePoints.size - 1) {
            val p1 = routePoints[i]
            val p2 = routePoints[i + 1]
            val midPoint = LatLngPoint((p1.lat + p2.lat) / 2.0, (p1.lng + p2.lng) / 2.0)

            val isNearBikeLane = allBikeNodes.any { bikeNode ->
                GeoUtils.distanceMeters(midPoint, bikeNode) <= BIKE_LANE_PROXIMITY_METERS
            }
            if (isNearBikeLane) {
                overlapMeters += GeoUtils.distanceMeters(p1, p2)
            }
        }
        return overlapMeters / 1000.0
    }
}
