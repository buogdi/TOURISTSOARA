package ro.touristsoara.logic

import ro.touristsoara.data.LatLngPoint
import ro.touristsoara.data.Poi

object MockDataDev3 {
    val piataVictoriei = LatLngPoint(45.7537, 21.2257)
    val garaDeNord = LatLngPoint(45.7509, 21.2076)

    val mockPois = listOf(
        Poi(
            id = "catedrala-mitropolitana",
            name = "Catedrala Mitropolitană",
            lat = 45.7506,
            lng = 21.2241,
            visitMinutes = 20
        ),
        Poi(
            id = "piata-unirii",
            name = "Piața Unirii",
            lat = 45.7580,
            lng = 21.2290,
            visitMinutes = 25
        ),
        Poi(
            id = "bastionul-theresia",
            name = "Bastionul Maria Theresia",
            lat = 45.7571,
            lng = 21.2339,
            visitMinutes = 20
        ),
        Poi(
            id = "castelul-huniade",
            name = "Castelul Huniade",
            lat = 45.7530,
            lng = 21.2269,
            visitMinutes = 15
        ),
        Poi(
            id = "parcul-rozelor",
            name = "Parcul Rozelor",
            lat = 45.7495,
            lng = 21.2312,
            visitMinutes = 20
        )
    )
}
