package ro.touristsoara.ui.map

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.EmojiNature
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SkipNext
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var showBikeLanes by remember { mutableStateOf(true) }
    var showTransitLines by remember { mutableStateOf(true) }

    // Simulation index for Demo (F7)
    var currentStopIndex by remember { mutableIntStateOf(0) }
    val currentPoi = route.stops.getOrNull(currentStopIndex) ?: route.stops.firstOrNull()

    // Pulse animation for user location marker
    val infiniteTransition = rememberInfiniteTransition(label = "UserPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Map Canvas: Stylized Vector Map of Timișoara
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background City Grid Tint
            drawRect(color = Color(0xFFF3F6F3))

            // Bega Canal Flowing across the city (curves through center)
            val begaPath = Path().apply {
                moveTo(0f, height * 0.68f)
                cubicTo(
                    width * 0.3f, height * 0.65f,
                    width * 0.6f, height * 0.73f,
                    width, height * 0.69f
                )
            }
            drawPath(
                path = begaPath,
                color = Color(0xFFB3E5FC),
                style = Stroke(width = 36f, cap = StrokeCap.Round)
            )
            drawPath(
                path = begaPath,
                color = Color(0xFF81D4FA),
                style = Stroke(width = 24f, cap = StrokeCap.Round)
            )

            // Parks along Bega (Parcul Rozelor, Parcul Central)
            drawCircle(
                color = Color(0xFFC8E6C9).copy(alpha = 0.7f),
                radius = 70f,
                center = Offset(width * 0.35f, height * 0.60f)
            )
            drawCircle(
                color = Color(0xFFC8E6C9).copy(alpha = 0.7f),
                radius = 80f,
                center = Offset(width * 0.70f, height * 0.62f)
            )

            // Historic Citadel / Cetate Square Area
            drawRoundRect(
                color = Color(0xFFFFECB3).copy(alpha = 0.35f),
                topLeft = Offset(width * 0.20f, height * 0.22f),
                size = androidx.compose.ui.geometry.Size(width * 0.60f, height * 0.32f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f, 30f)
            )

            // Layer 1: OSM Bike Lanes (Green dashed lines)
            if (showBikeLanes) {
                val bikeLane1 = Path().apply {
                    moveTo(width * 0.05f, height * 0.72f)
                    lineTo(width * 0.95f, height * 0.72f)
                }
                drawPath(
                    path = bikeLane1,
                    color = BikeLaneGreen,
                    style = Stroke(width = 7f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
                )
                val bikeLane2 = Path().apply {
                    moveTo(width * 0.30f, height * 0.15f)
                    lineTo(width * 0.30f, height * 0.85f)
                }
                drawPath(
                    path = bikeLane2,
                    color = BikeLaneGreen,
                    style = Stroke(width = 7f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
                )
            }

            // Layer 2: SMTT Public Transit Lines (Tram / Bus)
            if (showTransitLines) {
                val tram1 = Path().apply {
                    moveTo(width * 0.10f, height * 0.38f)
                    lineTo(width * 0.90f, height * 0.38f)
                }
                drawPath(
                    path = tram1,
                    color = TransitBlue,
                    style = Stroke(width = 6f)
                )
                val tram2 = Path().apply {
                    moveTo(width * 0.75f, height * 0.10f)
                    lineTo(width * 0.75f, height * 0.80f)
                }
                drawPath(
                    path = tram2,
                    color = Color(0xFFFFA000),
                    style = Stroke(width = 6f)
                )
            }

            // Selected Green Route Polyline
            val stopPoints = listOf(
                Offset(width * 0.26f, height * 0.48f), // Piața Victoriei / Catedrala
                Offset(width * 0.36f, height * 0.36f), // Palatul Culturii / Lloyd
                Offset(width * 0.50f, height * 0.31f), // Piața Libertății
                Offset(width * 0.62f, height * 0.26f), // Piața Unirii / Domul
                Offset(width * 0.76f, height * 0.34f)  // Bastionul Theresia
            )

            val routePath = Path()
            stopPoints.forEachIndexed { index, point ->
                if (index == 0) routePath.moveTo(point.x, point.y)
                else routePath.lineTo(point.x, point.y)
            }

            // Draw route shadow & main active green line
            drawPath(
                path = routePath,
                color = Color(0x331B6B48),
                style = Stroke(width = 14f, cap = StrokeCap.Round)
            )
            drawPath(
                path = routePath,
                color = Color(0xFF1B6B48),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // Draw POI markers on map
            stopPoints.forEachIndexed { index, point ->
                val isCurrent = index == currentStopIndex
                val markerColor = if (isCurrent) Color(0xFFD48806) else Color(0xFF1B6B48)

                // Pin circle
                drawCircle(
                    color = Color.White,
                    radius = if (isCurrent) 20f else 16f,
                    center = point
                )
                drawCircle(
                    color = markerColor,
                    radius = if (isCurrent) 16f else 12f,
                    center = point
                )
            }

            // Current simulated user location marker with radar pulse
            val activePoint = stopPoints.getOrElse(currentStopIndex) { stopPoints.first() }
            drawCircle(
                color = Color(0x442196F3),
                radius = pulseRadius,
                center = activePoint
            )
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = activePoint
            )
            drawCircle(
                color = Color(0xFF1976D2),
                radius = 8f,
                center = activePoint
            )
        }

        // Top Status & Navigation Controls
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

                // Time Buffer & Schedule Status Banner (F2 / F3)
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

            // Layer Toggle Chips (F8 & F9)
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

        // Floating Demo Action: "Simulează Sosirea" (F7)
        FloatingActionButton(
            onClick = {
                // Step forward to next POI or loop
                currentStopIndex = (currentStopIndex + 1) % route.stops.size
                currentPoi?.let { onOpenPoiDetail(it) }
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

        // Attribution Badge (OSM, SMTT, Primăria TM)
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

        // Bottom Sheet / Card: Current Landmark on Route
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun RouteMapScreenPreview() {
    val sampleRoute = ro.touristsoara.data.PoiDataProvider.calculateGreenRoute(
        startName = "Piața Victoriei",
        destinationName = "Gara de Nord",
        budgetMinutes = 120,
        travelMode = ro.touristsoara.model.TravelMode.WALKING,
        interests = setOf(ro.touristsoara.model.Interest.HISTORY),
        language = Language.RO
    )
    ro.touristsoara.ui.theme.TouristsoaraTheme {
        RouteMapScreen(
            route = sampleRoute,
            currentLanguage = Language.RO,
            onBackToInput = {},
            onOpenPoiDetail = {},
            onFinishTour = {}
        )
    }
}
