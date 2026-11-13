package ro.touristsoara.ui.map

<<<<<<< HEAD
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
=======
import android.graphics.Paint
import android.view.ViewGroup
>>>>>>> c316298 (v1.0)
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ro.touristsoara.data.repository.TransitRepository
import ro.touristsoara.model.Language
import ro.touristsoara.model.Poi
import ro.touristsoara.model.RouteResult
import ro.touristsoara.ui.theme.BikeLaneGreen
import ro.touristsoara.ui.theme.TransitBlue

@Composable
fun RouteMapScreen(
    route: RouteResult,
    currentLanguage: Language,
    onBackToInput: () -> Unit,
    onOpenPoiDetail: (Poi) -> Unit,
    onFinishTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showBikeLanes by remember { mutableStateOf(true) }
    var showTransitLines by remember { mutableStateOf(true) }

    // Simulation index for Demo
    var currentStopIndex by remember { mutableIntStateOf(0) }
    val currentPoi = route.stops.getOrNull(currentStopIndex) ?: route.stops.firstOrNull()

    // Initialize osmdroid user agent (mandatory by OSM policy)
    remember { Configuration.getInstance().userAgentValue = "TOURISTSOARA-UniHack8" }

    // Pre-load transit & bike data
    val transitLines = remember { TransitRepository.getTransitLines(context) }
    val bikeLanes = remember { TransitRepository.getBikeLanes() }

    // Map center: first POI or Piața Victoriei
    val mapCenter = remember {
        val first = route.stops.firstOrNull()
        if (first != null) GeoPoint(first.lat, first.lng) else GeoPoint(45.7535, 21.2255)
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

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

    // Helper: rebuild overlays (called on factory and on toggle changes)
    fun rebuildOverlays(mapView: MapView) {
        mapView.overlays.clear()

        // --- Layer 1: Transit polylines ---
        if (showTransitLines) {
            transitLines.forEach { line ->
                val polyline = Polyline(mapView).apply {
                    setPoints(line.points)
                    outlinePaint.apply {
                        color = try {
                            android.graphics.Color.parseColor("#${line.colorHex}")
                        } catch (_: Exception) {
                            TransitBlue.toArgb()
                        }
                        strokeWidth = 5f
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                        alpha = 180
                    }
                    infoWindow = null
                }
                mapView.overlays.add(polyline)
            }
        }

        // --- Layer 2: Bike lanes (green dashed) ---
        if (showBikeLanes) {
            bikeLanes.forEach { lane ->
                val polyline = Polyline(mapView).apply {
                    setPoints(lane)
                    outlinePaint.apply {
                        color = BikeLaneGreen.toArgb()
                        strokeWidth = 7f
                        style = Paint.Style.STROKE
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(25f, 12f), 0f)
                        isAntiAlias = true
                    }
                    infoWindow = null
                }
                mapView.overlays.add(polyline)
            }
        }

        // --- Layer 3: Green Route Polyline (connects all stops) ---
        if (route.stops.size >= 2) {
            val routePoints = route.stops.map { GeoPoint(it.lat, it.lng) }
            // Shadow
            val shadowLine = Polyline(mapView).apply {
                setPoints(routePoints)
                outlinePaint.apply {
                    color = android.graphics.Color.argb(50, 27, 107, 72)
                    strokeWidth = 16f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }
                infoWindow = null
            }
            mapView.overlays.add(shadowLine)
            // Main green line
            val routeLine = Polyline(mapView).apply {
                setPoints(routePoints)
                outlinePaint.apply {
                    color = android.graphics.Color.parseColor("#1B6B48")
                    strokeWidth = 8f
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    isAntiAlias = true
                }
                infoWindow = null
            }
            mapView.overlays.add(routeLine)
        }

        // --- Layer 4: POI Markers ---
        route.stops.forEachIndexed { index, poi ->
            val isCurrent = index == currentStopIndex
            val marker = Marker(mapView).apply {
                position = GeoPoint(poi.lat, poi.lng)
                title = "${index + 1}. ${poi.getDisplayName(currentLanguage)}"
                snippet = poi.address ?: "Timișoara"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _, _ ->
                    showInfoWindow()
                    onOpenPoiDetail(poi)
                    true
                }
            }
            mapView.overlays.add(marker)
        }

        // --- Layer 5: User / simulation marker (blue dot at current stop) ---
        val activeStop = route.stops.getOrNull(currentStopIndex) ?: route.stops.firstOrNull()
        if (activeStop != null) {
            val userMarker = Marker(mapView).apply {
                position = GeoPoint(activeStop.lat, activeStop.lng)
                title = if (currentLanguage == Language.RO) "Locația ta (simulată)" else "Your location (simulated)"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mapView.overlays.add(userMarker)
        }

        mapView.invalidate()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // ═══════════════════════════════════════════════
        // 1. OSM MapView (replaces the mock Canvas)
        // ═══════════════════════════════════════════════
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
                    controller.setCenter(mapCenter)
                    mapViewRef = this
                    rebuildOverlays(this)
                }
            },
            update = { mapView ->
                rebuildOverlays(mapView)
            }
        )

        // ═══════════════════════════════════════════════
        // 2. Top Status & Navigation Controls (Dev 1 UI)
        // ═══════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar: Back Button & Time Status Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    IconButton(onClick = onBackToInput) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Time Buffer & Schedule Status Banner
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentLanguage == Language.RO) "Ești pe grafic" else "On Schedule",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "✅", fontSize = 14.sp)
                            }
                            Text(
                                text = if (currentLanguage == Language.RO)
                                    "Timp rămas: ${route.estimatedDurationMinutes}m (buffer +15m)"
                                else
                                    "Remaining: ${route.estimatedDurationMinutes}m (+15m buffer)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${route.stops.size} POI",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Layer Toggle Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showBikeLanes,
                    onClick = { showBikeLanes = !showBikeLanes },
                    label = { Text("🌿 Piste OSM") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE8F5E9),
                        selectedLabelColor = BikeLaneGreen
                    ),
                    shape = RoundedCornerShape(20.dp)
                )

                FilterChip(
                    selected = showTransitLines,
                    onClick = { showTransitLines = !showTransitLines },
                    label = { Text("🚋 SMTT Transit") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsTransit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE1F5FE),
                        selectedLabelColor = TransitBlue
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // ═══════════════════════════════════════════════
        // 3. Floating Demo Action: "Simulează Sosirea"
        // ═══════════════════════════════════════════════
        FloatingActionButton(
            onClick = {
                currentStopIndex = (currentStopIndex + 1) % route.stops.size
                val nextPoi = route.stops.getOrNull(currentStopIndex)
                if (nextPoi != null) {
                    mapViewRef?.controller?.animateTo(GeoPoint(nextPoi.lat, nextPoi.lng))
                    onOpenPoiDetail(nextPoi)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FastForward, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLanguage == Language.RO) "Simulează Sosirea" else "Simulate Arrival",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // ═══════════════════════════════════════════════
        // 4. Attribution Badge (OSM, SMTT, Primăria TM)
        // ═══════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 175.dp)
        ) {
            Text(
                text = "© OpenStreetMap contributors · SMTT v4.31 · Primăria Timișoara",
                color = Color.White,
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        // ═══════════════════════════════════════════════
        // 5. Bottom Card: Current Landmark on Route
        // ═══════════════════════════════════════════════
        if (currentPoi != null) {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stop header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Oprirea ${currentStopIndex + 1} din ${route.stops.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏱️ ${currentPoi.visitMinutes} min vizită",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Landmark Name & Address
                    Column {
                        Text(
                            text = currentPoi.getDisplayName(currentLanguage),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentPoi.address ?: "Timișoara",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenPoiDetail(currentPoi) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == Language.RO) "Card AI Gemini" else "Gemini AI Card",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onFinishTour,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentLanguage == Language.RO) "Finalizează Turul" else "Finish Tour",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
