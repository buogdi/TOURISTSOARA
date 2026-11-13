package ro.touristsoara.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ro.touristsoara.model.Poi

class GeofenceCheckerTest {

    private val catedrala = Poi(
        id = "catedrala-mitropolitana",
        name = "Catedrala Mitropolitană Ortodoxă",
        lat = 45.75070,
        lng = 21.22423,
        description = "Construită între 1936 și 1946..."
    )

    private val palatulLloyd = Poi(
        id = "palatul-lloyd",
        name = "Palatul Lloyd",
        lat = 45.75355,
        lng = 21.22523,
        description = "Palatul a fost prima clădire..."
    )

    @Test
    fun testHaversineDistanceZeroForSamePoint() {
        val dist = DistanceUtils.distanceInMeters(
            catedrala.lat, catedrala.lng,
            catedrala.lat, catedrala.lng
        )
        assertEquals(0.0, dist, 0.001)
    }

    @Test
    fun testDistanceBetweenKnownPointsInTimisoara() {
        // Distanța reală de la Catedrală la Palatul Lloyd pe Piața Victoriei este de aprox. 320-330 m
        val dist = DistanceUtils.distanceInMeters(
            catedrala.lat, catedrala.lng,
            palatulLloyd.lat, palatulLloyd.lng
        )
        assertTrue("Distanța ar trebui să fie în jur de 325m, a fost: $dist", dist in 300.0..350.0)
    }

    @Test
    fun testArrivalDetectionWithin50Meters() {
        val checker = GeofenceChecker()

        // Punct la ~25 m nord de Catedrală (0.00022 grade latitudine ~ 24.5 m)
        val userNearLat = catedrala.lat + 0.00022
        val userNearLng = catedrala.lng

        val result = checker.checkArrival(userNearLat, userNearLng, listOf(catedrala, palatulLloyd))
        assertNotNull("Ar trebui să detecteze sosirea la Catedrală", result)
        assertEquals(catedrala.id, result?.poi?.id)
        assertTrue("Distanța ar trebui să fie <= 50m", (result?.distanceMeters ?: 100.0) <= 50.0)
    }

    @Test
    fun testNoArrivalDetectionWhenFurtherThan50Meters() {
        val checker = GeofenceChecker()

        // Utilizatorul este în fața Operei (~420m depărtare)
        val operaLat = 45.75441
        val operaLng = 21.22612

        val result = checker.checkArrival(operaLat, operaLng, listOf(catedrala))
        assertNull("Nu ar trebui să detecteze sosire la >50m", result)
    }

    @Test
    fun testVisitedPoiNotTriggeredAgainUntilReset() {
        val checker = GeofenceChecker()
        val userNearLat = catedrala.lat + 0.0001
        val userNearLng = catedrala.lng

        // Prima detectare
        val result1 = checker.checkArrival(userNearLat, userNearLng, listOf(catedrala))
        assertNotNull(result1)

        // Marcăm ca vizitat
        checker.markVisited(catedrala.id)
        assertTrue(checker.isVisited(catedrala.id))
        assertEquals(1, checker.getVisitedCount())

        // A doua detectare în aceeași locație nu mai declanșează
        val result2 = checker.checkArrival(userNearLat, userNearLng, listOf(catedrala))
        assertNull("Obiectivul vizitat nu trebuie să se redeschidă la nesfârșit", result2)

        // După resetare, poate fi detectat din nou
        checker.resetVisited()
        assertEquals(0, checker.getVisitedCount())
        val result3 = checker.checkArrival(userNearLat, userNearLng, listOf(catedrala))
        assertNotNull("După resetare, obiectivul poate fi detectat din nou", result3)
    }
}
