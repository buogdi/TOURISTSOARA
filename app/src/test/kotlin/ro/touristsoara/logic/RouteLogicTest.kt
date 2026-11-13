package ro.touristsoara.logic

import org.junit.Assert.*
import org.junit.Test
import ro.touristsoara.data.*

class RouteLogicTest {

    private val piataVictoriei = LatLngPoint(45.7538, 21.2257)
    private val piataUnirii = LatLngPoint(45.7580, 21.2290)
    private val catedralaMitropolitana = LatLngPoint(45.7523, 21.2243)
    private val garaDeNord = LatLngPoint(45.7508, 21.2078)
    private val planner = RoutePlanner()

    @Test
    fun testDev3RoutePlannerSimulation() {
        val planner = RoutePlanner()

        // Simulează input-ul de la Dev 1 (2 ore jumatate, pe jos)
        val timpDisponibil = 150

        // 1. Alege obiectivele Greedy
        val selectate = planner.selectPoisGreedy(
            start = MockDataDev3.piataVictoriei,
            destination = MockDataDev3.garaDeNord,
            availableTimeMinutes = timpDisponibil,
            mode = TransportMode.WALK,
            activePois = MockDataDev3.mockPois
        )

        println("Obiective selectate (${selectate.size}): ${selectate.map { it.name }}")

        // 2. Construiește rezultatul
        val ruta = planner.buildFallbackRoute(
            start = MockDataDev3.piataVictoriei,
            destination = MockDataDev3.garaDeNord,
            destinationName = "Gara de Nord",
            selectedPois = selectate,
            mode = TransportMode.WALK
        )

        println("Timp total deplasare + vizite: ${ruta.totalDurationMinutes} min (Buget: $timpDisponibil min - include 15m buffer)")
        println("Kilometri totali (estimat pe străzi): ${ruta.totalDistanceKm} km")

        assertTrue(selectate.isNotEmpty())
        assertTrue(ruta.totalDurationMinutes <= (timpDisponibil - RoutePlanner.SAFETY_BUFFER_MINUTES))
    }

    @Test
    fun testGeoUtilsDistance() {
        val dist = GeoUtils.distanceMeters(piataVictoriei, piataUnirii)
        assertTrue("Distance should be around 500-600m, got $dist", dist in 400.0..700.0)
    }

    @Test
    fun testMockDataDev3() {
        val selected = planner.selectPoisGreedy(
            start = MockDataDev3.piataVictoriei,
            destination = MockDataDev3.garaDeNord,
            availableTimeMinutes = 120,
            mode = TransportMode.WALK,
            activePois = MockDataDev3.mockPois
        )
        assertTrue("Mock data should result in selected POIs", selected.isNotEmpty())

        val route = planner.buildFallbackRoute(
            start = MockDataDev3.piataVictoriei,
            destination = MockDataDev3.garaDeNord,
            destinationName = "Gara de Nord",
            selectedPois = selected,
            mode = TransportMode.WALK
        )

        assertTrue(route.totalDurationMinutes <= 120)
        assertTrue(route.greenScore > 0)
    }

    @Test
    fun testGeofenceChecker() {
        val userLocNearCatedrala = LatLngPoint(45.75235, 21.22435)
        val userLocFar = LatLngPoint(45.7600, 21.2400)

        assertTrue(GeofenceChecker.isArrived(userLocNearCatedrala, catedralaMitropolitana, 50.0))
        assertFalse(GeofenceChecker.isArrived(userLocFar, catedralaMitropolitana, 50.0))
    }

    @Test
    fun testGeofenceAndScheduleTracker() {
        val poi1 = Poi("1", "Catedrala", lat = catedralaMitropolitana.lat, lng = catedralaMitropolitana.lng, visitMinutes = 15)
        val tracker = GeofenceAndScheduleTracker(
            routePois = listOf(poi1),
            destination = garaDeNord,
            modeSpeedKmh = 5.0,
            totalAvailableMinutes = 60
        )

        val userLocNear = LatLngPoint(45.75232, 21.22432)
        val state = tracker.updateLocation(userLocNear)

        assertEquals(ScheduleStatus.ON_TRACK, state.status)
        assertEquals(poi1, state.arrivedAtPoi)
    }

    @Test
    fun testGreenScoreCalculator() {
        val segment = RouteSegment(
            fromName = "Start",
            toName = "Dest",
            mode = TransportMode.WALK,
            distanceKm = 2.0,
            durationMinutes = 24
        )

        val metrics = GreenScoreCalculator.calculate(listOf(segment))
        assertTrue(metrics.co2SavedGrams > 0)
        assertTrue(metrics.greenScore > 0)
        assertNotNull(metrics.methodologyCitationRo)
        assertNotNull(metrics.methodologyCitationEn)
    }

    @Test
    fun testRoutePlannerSelectPoisGreedy() {
        val start = piataVictoriei
        val dest = garaDeNord
        val pois = listOf(
            Poi("1", "Piața Unirii", lat = piataUnirii.lat, lng = piataUnirii.lng, visitMinutes = 15),
            Poi("2", "Catedrala", lat = catedralaMitropolitana.lat, lng = catedralaMitropolitana.lng, visitMinutes = 10)
        )

        val selected = planner.selectPoisGreedy(
            start = start,
            destination = dest,
            availableTimeMinutes = 120, // 2 hours
            mode = TransportMode.WALK,
            activePois = pois
        )

        assertEquals(2, selected.size)

        val result = planner.buildFallbackRoute(
            start = start,
            destination = dest,
            destinationName = "Gara de Nord",
            selectedPois = selected,
            mode = TransportMode.WALK
        )

        assertTrue("Green score should be positive", result.greenScore > 0)
        assertTrue("CO2 saved should be positive", result.co2SavedGrams > 0.0)
    }

    @Test
    fun testRoutePlannerTimeBudgetExceeded() {
        val start = piataVictoriei
        val dest = garaDeNord
        val pois = listOf(
            Poi("1", "Piața Unirii", lat = piataUnirii.lat, lng = piataUnirii.lng, visitMinutes = 60),
            Poi("2", "Catedrala", lat = catedralaMitropolitana.lat, lng = catedralaMitropolitana.lng, visitMinutes = 60)
        )

        val selected = planner.selectPoisGreedy(
            start = start,
            destination = dest,
            availableTimeMinutes = 30,
            mode = TransportMode.WALK,
            activePois = pois
        )

        assertEquals(0, selected.size)
    }

    @Test
    fun testShouldDropLastPoi() {
        val pois = listOf(
            Poi("1", "Piața Unirii", lat = piataUnirii.lat, lng = piataUnirii.lng, visitMinutes = 30),
            Poi("2", "Catedrala", lat = catedralaMitropolitana.lat, lng = catedralaMitropolitana.lng, visitMinutes = 30)
        )

        val shouldDrop = planner.shouldDropLastPoi(
            orsTravelMinutes = 30,
            selectedPois = pois,
            availableTimeMinutes = 90
        )

        assertTrue(shouldDrop)
    }

    @Test
    fun testPolylineDecoder() {
        val samplePolyline = "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
        val points = PolylineDecoder.decodePolyline(samplePolyline)
        assertTrue(points.isNotEmpty())
        val pointsAlias = PolylineDecoder.decode(samplePolyline)
        assertEquals(points.size, pointsAlias.size)
    }

    @Test
    fun testTransitPlannerSegmentOrNull() {
        val stopA = TransitStop("S1", "Catedrală", 45.7520, 21.2240, listOf("R1"))
        val stopB = TransitStop("S2", "Gara de Nord", 45.7510, 21.2085, listOf("R1"))
        val routeInfo = TransitRouteInfo("R1", "1", type = 0, orderedStopIds = listOf("S1", "S2"))

        val planner = TransitPlanner(
            stops = listOf(stopA, stopB),
            routes = mapOf("R1" to routeInfo)
        )

        val segment = planner.findDirectTransitSegmentOrNull(
            from = piataVictoriei,
            fromName = "Piața Victoriei",
            to = garaDeNord,
            toName = "Gara de Nord"
        )

        assertNotNull(segment)
        assertEquals(TransportMode.TRANSIT, segment?.mode)
        assertEquals("Linia 1", segment?.transitLineName)
    }
}
