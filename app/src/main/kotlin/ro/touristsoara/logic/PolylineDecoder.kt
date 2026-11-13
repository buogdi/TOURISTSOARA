package ro.touristsoara.logic

import org.osmdroid.util.GeoPoint
import ro.touristsoara.data.LatLngPoint

object PolylineDecoder {
    /**
     * Decodează o polilinie codificată Google (precizie 5) în LatLngPoint (pentru logică și modele).
     */
    fun decodePolyline(encoded: String): List<LatLngPoint> {
        val out = mutableListOf<LatLngPoint>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var shift = 0
            var result = 0
            var b: Int
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

            out.add(LatLngPoint(lat / 1e5, lng / 1e5))
        }
        return out
    }

    /**
     * Decodează o polilinie în osmdroid GeoPoint (pentru afișare pe hartă).
     */
    fun decode(encoded: String): List<GeoPoint> {
        val poly = mutableListOf<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)

            poly.add(GeoPoint(lat / 1E5, lng / 1E5))
        }

        return poly
    }
}
