package ro.touristsoara.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ro.touristsoara.model.AiText
import ro.touristsoara.model.Poi
import kotlin.math.roundToInt

/**
 * Dialog afișat la sosirea la un obiectiv turistic (< 50 m)
 * conținând cardul structurat generat cu Gemini.
 */
@Composable
fun PoiArrivalDialog(
    poi: Poi,
    distanceMeters: Double?,
    aiText: AiText?,
    isLoadingAi: Boolean = false,
    onContinueRoute: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            PoiArrivalCardContent(
                poi = poi,
                distanceMeters = distanceMeters,
                aiText = aiText,
                isLoadingAi = isLoadingAi,
                onContinueRoute = onContinueRoute,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Conținutul propriu-zis al cardului de sosire cu datele generate de Gemini.
 * Poate fi integrat și ca BottomSheet sau direct în hartă.
 */
@Composable
fun PoiArrivalCardContent(
    poi: Poi,
    distanceMeters: Double?,
    aiText: AiText?,
    isLoadingAi: Boolean,
    onContinueRoute: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        // 1. Badge Sosire / Distanță
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (distanceMeters != null && distanceMeters <= 50.0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "🎯 Ai sosit! (${distanceMeters.roundToInt()} m)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else if (distanceMeters != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "📍 La ${distanceMeters.roundToInt()} m",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Timp estimat de vizită
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "⏱️ ${poi.visitMinutes} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Nume Obiectiv și Adresă
        Text(
            text = poi.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        poi.address?.let { addr ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📍 $addr",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // 3. Conținut AI (Gemini) sau Stare de încărcare
        if (isLoadingAi) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Generăm rezumatul cu Gemini AI...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (aiText != null) {
            // Secțiunile structurate: Istorie, Curiozitate, Ce merită văzut
            AiSectionItem(
                icon = "🏛️",
                title = "Istoric",
                text = aiText.history
            )

            Spacer(modifier = Modifier.height(12.dp))

            AiSectionItem(
                icon = "💡",
                title = "Curiozitate",
                text = aiText.funFact
            )

            Spacer(modifier = Modifier.height(12.dp))

            AiSectionItem(
                icon = "👀",
                title = "Ce merită văzut",
                text = aiText.mustSee
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Etichetă sursă oficială / grounded
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (aiText.isGrounded) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                }
            ) {
                Text(
                    text = if (aiText.isGrounded) {
                        "✨ Sursa: Ghidul turistic al Primăriei Timișoara · rezumat generat cu Gemini"
                    } else {
                        "⚠️ Nu avem o descriere oficială verificată pentru acest loc în ghidul Primăriei"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (aiText.isGrounded) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            // Fallback dacă nu avem încă date AI generate
            Text(
                text = poi.description ?: "Descriere indisponibilă pentru acest obiectiv.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Butoane de acțiune
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Închide")
            }

            Button(
                onClick = onContinueRoute,
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Continuă traseul")
            }
        }
    }
}

@Composable
private fun AiSectionItem(
    icon: String,
    title: String,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PoiArrivalCardPreview() {
    MaterialTheme {
        PoiArrivalCardContent(
            poi = Poi(
                id = "catedrala-mitropolitana",
                name = "Catedrala Mitropolitană Ortodoxă",
                address = "Bulevardul Regele Ferdinand I",
                lat = 45.7507,
                lng = 21.22423,
                visitMinutes = 20,
                description = "Construită între 1936 și 1946..."
            ),
            distanceMeters = 38.4,
            aiText = AiText(
                history = "Catedrala a fost construită între 1936 și 1946, combinând stilul tradițional moldovenesc cu cel bizantin. A fost până în 2018 cea mai înaltă biserică din România.",
                funFact = "Are o înălțime de 90 de metri și cele 7 clopote au fost acordate de maestrul Sabin Drăgoi.",
                mustSee = "Merită admirat mozaicul fațadei și impresionanta colecție de artă religioasă din subsol.",
                isGrounded = true
            ),
            isLoadingAi = false,
            onContinueRoute = {},
            onDismiss = {}
        )
    }
}
