package ro.touristsoara.data

import kotlinx.serialization.Serializable

@Serializable
data class LatLngPoint(
    val lat: Double,
    val lng: Double
)
