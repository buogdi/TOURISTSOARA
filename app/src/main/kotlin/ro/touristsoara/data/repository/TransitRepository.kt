package ro.touristsoara.data.repository

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.osmdroid.util.GeoPoint
import ro.touristsoara.logic.PolylineDecoder

data class TransitLine(
    val routeId: String,
    val name: String,
    val colorHex: String,
    val points: List<GeoPoint>
)

object TransitRepository {

    private var cachedTransitLines: List<TransitLine>? = null

    /**
     * Loads public transit lines from assets/transit_network.json
     * Cached in memory after first load.
     */
    fun getTransitLines(context: Context): List<TransitLine> {
        cachedTransitLines?.let { return it }

        val list = mutableListOf<TransitLine>()
        try {
            val jsonString = context.assets.open("transit_network.json").bufferedReader().use { it.readText() }
            val root = Json.parseToJsonElement(jsonString).jsonObject

            // Build route metadata map: id -> (shortName, color)
            val routeMeta = mutableMapOf<String, Pair<String, String>>()
            val routesArray = root["routes"]?.jsonArray
            routesArray?.forEach { elem ->
                val obj = elem.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: ""
                val shortName = obj["shortName"]?.jsonPrimitive?.content ?: id
                val color = obj["color"]?.jsonPrimitive?.content ?: "1976D2"
                routeMeta[id] = Pair(shortName, color)
            }

            // Extract paths
            val pathsArray = root["paths"]?.jsonArray
            // Take the first 35 prominent lines to maintain high render performance
            pathsArray?.take(35)?.forEach { elem ->
                val obj = elem.jsonObject
                val routeId = obj["routeId"]?.jsonPrimitive?.content ?: ""
                val polylineStr = obj["polyline"]?.jsonPrimitive?.content ?: ""
                if (polylineStr.isNotEmpty()) {
                    val points = PolylineDecoder.decode(polylineStr)
                    if (points.isNotEmpty()) {
                        val meta = routeMeta[routeId] ?: Pair(routeId, "1976D2")
                        list.add(
                            TransitLine(
                                routeId = routeId,
                                name = meta.first,
                                colorHex = meta.second,
                                points = points
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        cachedTransitLines = list
        return list
    }

    /**
     * Official Timișoara bike path corridors (Bega Green Corridor, Cetate, University, Coposu).
     */
    fun getBikeLanes(): List<List<GeoPoint>> {
        return listOf(
            // 1. Malul Begăi North (Splaiul Tudor Vladimirescu -> Parcul Rozelor -> Parcul Regina Maria -> Uzina de Apă)
            listOf(
                GeoPoint(45.7420, 21.2050),
                GeoPoint(45.7460, 21.2140),
                GeoPoint(45.7485, 21.2185),
                GeoPoint(45.7495, 21.2260),
                GeoPoint(45.7505, 21.2335),
                GeoPoint(45.7530, 21.2420),
                GeoPoint(45.7570, 21.2505),
                GeoPoint(45.7620, 21.2650)
            ),
            // 2. Malul Begăi South (Splaiul Nistrului -> Parcul Copiilor -> Complex Studențesc)
            listOf(
                GeoPoint(45.7430, 21.2060),
                GeoPoint(45.7470, 21.2150),
                GeoPoint(45.7480, 21.2200),
                GeoPoint(45.7488, 21.2280),
                GeoPoint(45.7492, 21.2350),
                GeoPoint(45.7510, 21.2440),
                GeoPoint(45.7555, 21.2520)
            ),
            // 3. Axa Universitară (Bd. Vasile Pârvan -> Bd. Corneliu Coposu -> Michelangelo)
            listOf(
                GeoPoint(45.7462, 21.2270),
                GeoPoint(45.7478, 21.2315),
                GeoPoint(45.7498, 21.2335),
                GeoPoint(45.7512, 21.2380)
            ),
            // 4. Centru Istoric & Inelul 1 (Piața Victoriei -> Piața Libertății -> Piața Unirii -> Bastion)
            listOf(
                GeoPoint(45.7510, 21.2245),
                GeoPoint(45.7535, 21.2255),
                GeoPoint(45.7555, 21.2272),
                GeoPoint(45.7580, 21.2290),
                GeoPoint(45.7572, 21.2350)
            )
        )
    }
}
