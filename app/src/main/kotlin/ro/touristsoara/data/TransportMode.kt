package ro.touristsoara.data

import kotlinx.serialization.Serializable

@Serializable
enum class TransportMode(val speedKmh: Double, val co2GramsPerKm: Double) {
    WALK(speedKmh = 5.0, co2GramsPerKm = 0.0),
    BIKE(speedKmh = 15.0, co2GramsPerKm = 0.0),
    TRANSIT(speedKmh = 20.0, co2GramsPerKm = 45.0),
    MIX(speedKmh = 10.0, co2GramsPerKm = 20.0);

    companion object {
        const val CAR_CO2_FACTOR_GRAMS_PER_KM = 120.0
    }
}
