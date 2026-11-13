package ro.touristsoara.ui.map

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import ro.touristsoara.data.model.Poi
import ro.touristsoara.data.repository.PoiRepository

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onPoiSelected: (Poi) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize osmdroid user agent (mandatory by OSM policy)
    remember {
        Configuration.getInstance().userAgentValue = "TOURISTSOARA-UniHack8"
    }

    val poiRepository = remember { PoiRepository(context) }
    val pois = remember { poiRepository.getPois() }

    // Layer toggles
    var showBikeLanes by remember { mutableStateOf(true) }
    var showTransit by remember { mutableStateOf(false) }

    // Simulated user position (Starts at Piața Victoriei)
    val timisoaraCenter = remember { GeoPoint(45.7535, 21.2255) }
    var userPosition by remember { mutableStateOf(timisoaraCenter) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var userMarkerRef by remember { mutableStateOf<Marker?>(null) }

    // Manage MapView Android Lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. OSM MapView embedded via AndroidView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.5)
                    controller.setCenter(timisoaraCenter)

                    // Add POI Markers
                    pois.forEach { poi ->
                        val marker = Marker(this).apply {
                            position = GeoPoint(poi.lat, poi.lng)
                            title = poi.name
                            snippet = poi.address ?: poi.description?.take(60)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                showInfoWindow()
                                onPoiSelected(poi)
                                true
                            }
                        }
                        overlays.add(marker)
                    }

                    // Add User / Simulation Marker
                    val userMarker = Marker(this).apply {
                        position = userPosition
                        title = "Locația Ta (Simulată)"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    userMarkerRef = userMarker
                    overlays.add(userMarker)

                    mapViewRef = this
                    invalidate()
                }
            },
            update = { mapView ->
                userMarkerRef?.position = userPosition
                mapView.invalidate()
            }
        )

        // 2. Top Bar: Timp & Grafic status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🌿 Traseu Verde · Timișoara",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Timp rămas: 2h 15m · Ești pe grafic ✅",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 3. Layer Toggles (Biciclete / Transport)
        Card(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomStart),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = showBikeLanes,
                        onCheckedChange = { showBikeLanes = it }
                    )
                    Text(" 🚲 Piste velo", style = MaterialTheme.typography.labelMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = showTransit,
                        onCheckedChange = { showTransit = it }
                    )
                    Text(" 🚌 Transport SMTT", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // 4. FAB: Simulează Locația (Demo button)
        FloatingActionButton(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                // Pick a target POI to simulate moving towards (e.g. Catedrala -> Palatul Lloyd -> Opera)
                val target = pois.firstOrNull { it.id == "palatul-culturii" } ?: pois.firstOrNull()
                if (target != null) {
                    userPosition = GeoPoint(target.lat, target.lng)
                    mapViewRef?.controller?.animateTo(userPosition)
                }
            }
        ) {
            Text("📍 Simulează Pas", modifier = Modifier.padding(horizontal = 8.dp))
        }

        // 5. OpenStreetMap ODbL Attribution
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "© OpenStreetMap contributors · Date SMTT Timișoara",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
