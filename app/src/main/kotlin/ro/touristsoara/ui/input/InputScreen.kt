package ro.touristsoara.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.touristsoara.model.Interest
import ro.touristsoara.model.Language
import ro.touristsoara.model.TravelMode
import ro.touristsoara.ui.theme.BikeLaneGreen
import ro.touristsoara.ui.theme.GroundedGeminiGradientEnd
import ro.touristsoara.ui.theme.GroundedGeminiGradientStart

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InputScreen(
    currentLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    onPlanRoute: (
        start: String,
        destination: String,
        budgetMinutes: Int,
        mode: TravelMode,
        interests: Set<Interest>
    ) -> Unit,
    onOpenPoiManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startLocation by remember { mutableStateOf("Piața Victoriei (Centru)") }
    var destinationLocation by remember { mutableStateOf("Gara de Nord Timișoara") }
    var budgetMinutes by remember { mutableFloatStateOf(120f) } // default 2 hours
    var travelMode by remember { mutableStateOf(TravelMode.WALKING) }
    var selectedInterests by remember {
        mutableStateOf(setOf(Interest.HISTORY, Interest.ARCHITECTURE))
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Header & Language Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TOURISTȘOARA",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "🌿", fontSize = 24.sp)
                }
                Text(
                    text = if (currentLanguage == Language.RO)
                        "Planificator Verde pe Buget de Timp"
                    else
                        "Green Mobility Time-Budget Planner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Language pill toggle
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable {
                    onLanguageChange(
                        if (currentLanguage == Language.RO) Language.EN else Language.RO
                    )
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${currentLanguage.flag} ${currentLanguage.name}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Catchphrase Pitch Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💡", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (currentLanguage == Language.RO)
                        "„Ai 2-3 ore până la tren? Spune-ne cât timp ai, te plimbăm verde prin oraș și te ducem la timp la destinație!”"
                    else
                        "“Have a few hours before your train? Tell us your time, explore green Timișoara, and arrive right on schedule!”",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Section 1: Departure & Destination
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (currentLanguage == Language.RO) "Traseu & Puncte" else "Route & Locations",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = startLocation,
                    onValueChange = { startLocation = it },
                    label = {
                        Text(if (currentLanguage == Language.RO) "Punct de plecare" else "Start location")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick start suggestions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionPill(
                        label = "📍 Piața Victoriei",
                        onClick = { startLocation = "Piața Victoriei" }
                    )
                    SuggestionPill(
                        label = "🏛️ Piața Unirii",
                        onClick = { startLocation = "Piața Unirii" }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = destinationLocation,
                    onValueChange = { destinationLocation = it },
                    label = {
                        Text(if (currentLanguage == Language.RO) "Destinație finală" else "Final destination")
                    },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick destination suggestions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionPill(
                        label = "🚂 Gara de Nord",
                        onClick = { destinationLocation = "Gara de Nord Timișoara" }
                    )
                    SuggestionPill(
                        label = "🛍️ Iulius Town",
                        onClick = { destinationLocation = "Iulius Town Timișoara" }
                    )
                }
            }
        }

        // Section 2: Time Budget Slider
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLanguage == Language.RO) "Timp Disponibil" else "Time Budget",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = formatMinutes(budgetMinutes.toInt()),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Slider(
                    value = budgetMinutes,
                    onValueChange = { budgetMinutes = it },
                    valueRange = 45f..300f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Quick Budget Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BudgetChip(label = "1h", selected = budgetMinutes.toInt() == 60) { budgetMinutes = 60f }
                    BudgetChip(label = "1h 30m", selected = budgetMinutes.toInt() == 90) { budgetMinutes = 90f }
                    BudgetChip(label = "2h", selected = budgetMinutes.toInt() == 120) { budgetMinutes = 120f }
                    BudgetChip(label = "2h 30m", selected = budgetMinutes.toInt() == 150) { budgetMinutes = 150f }
                    BudgetChip(label = "3h", selected = budgetMinutes.toInt() == 180) { budgetMinutes = 180f }
                }

                // Buffer note
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (currentLanguage == Language.RO)
                            "🛡️ Include automat buffer de siguranță de 15 min pentru sosire la timp!"
                        else
                            "🛡️ Automatically includes a 15-min safety buffer for on-time arrival!",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Section 3: Green Travel Mode
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (currentLanguage == Language.RO) "Mod de Deplasare Verde" else "Green Travel Mode",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModeSelectionCard(
                        title = if (currentLanguage == Language.RO) TravelMode.WALKING.titleRo else TravelMode.WALKING.titleEn,
                        emoji = TravelMode.WALKING.iconEmoji,
                        subtext = "0g CO₂/km",
                        selected = travelMode == TravelMode.WALKING,
                        onClick = { travelMode = TravelMode.WALKING },
                        modifier = Modifier.weight(1f)
                    )
                    ModeSelectionCard(
                        title = if (currentLanguage == Language.RO) TravelMode.CYCLING.titleRo else TravelMode.CYCLING.titleEn,
                        emoji = TravelMode.CYCLING.iconEmoji,
                        subtext = if (currentLanguage == Language.RO) "Piste OSM" else "OSM Lanes",
                        selected = travelMode == TravelMode.CYCLING,
                        onClick = { travelMode = TravelMode.CYCLING },
                        modifier = Modifier.weight(1f)
                    )
                    ModeSelectionCard(
                        title = if (currentLanguage == Language.RO) "Mix Tramvai" else "Transit Mix",
                        emoji = TravelMode.TRANSIT_MIX.iconEmoji,
                        subtext = "SMTT Timiș",
                        selected = travelMode == TravelMode.TRANSIT_MIX,
                        onClick = { travelMode = TravelMode.TRANSIT_MIX },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 4: Interests for Grounded Gemini Personalization
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (currentLanguage == Language.RO) "Ce te interesează?" else "Your Interests",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (currentLanguage == Language.RO)
                                "Personalizează poveștile Gemini AI"
                            else
                                "Personalizes Gemini AI insights",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(text = "✨ Gemini", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Interest.entries.forEach { interest ->
                        val isSelected = selectedInterests.contains(interest)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedInterests = if (isSelected) {
                                    if (selectedInterests.size > 1) selectedInterests - interest else selectedInterests
                                } else {
                                    selectedInterests + interest
                                }
                            },
                            label = {
                                Text("${interest.icon} ${if (currentLanguage == Language.RO) interest.labelRo else interest.labelEn}")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Action Buttons Row: Generate Route + Manage POIs
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    onPlanRoute(
                        startLocation,
                        destinationLocation,
                        budgetMinutes.toInt(),
                        travelMode,
                        selectedInterests
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (currentLanguage == Language.RO)
                            "Generează Traseul Verde 🌿"
                        else
                            "Generate Green Route 🌿",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }

            OutlinedButton(
                onClick = onOpenPoiManager,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentLanguage == Language.RO)
                        "Personalizează Lista de Obiective (F6)"
                    else
                        "Customize Landmarks List (F6)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SuggestionPill(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BudgetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ModeSelectionCard(
    title: String,
    emoji: String,
    subtext: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val containerColor = if (selected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val rem = minutes % 60
    return if (hours > 0) {
        if (rem > 0) "${hours}h ${rem}m" else "${hours}h"
    } else {
        "${rem}m"
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
fun InputScreenPreview() {
    ro.touristsoara.ui.theme.TouristsoaraTheme {
        InputScreen(
            currentLanguage = Language.RO,
            onLanguageChange = {},
            onPlanRoute = { _, _, _, _, _ -> },
            onOpenPoiManager = {}
        )
    }
}
