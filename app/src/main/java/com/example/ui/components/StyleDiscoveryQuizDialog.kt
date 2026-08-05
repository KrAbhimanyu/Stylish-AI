package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStylePreference

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleDiscoveryQuizDialog(
    onDismiss: () -> Unit,
    onCompleteQuiz: (UserStylePreference) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    var selectedInspiration by remember { mutableStateOf("Royal Ethnic Tradition & Heritage") }
    var selectedVibe by remember { mutableStateOf("Ethnic Chic & Modern") }
    var selectedPalette by remember { mutableStateOf("Rich & Warm Neutrals") }
    var selectedFit by remember { mutableStateOf("Tailored Regular") }
    var selectedDressCode by remember { mutableStateOf("Festive Ethnic, Smart Casual, Formal") }
    var selectedTopSize by remember { mutableStateOf("L") }
    var selectedBottomSize by remember { mutableStateOf("32") }
    var selectedShoeSize by remember { mutableStateOf("UK 10") }

    val inspirations = listOf(
        "Royal Ethnic Tradition & Heritage" to "👑 Sherwanis, Kurtas, Silk Sarees & Gold Accents",
        "Italian Sartorial Elegance" to "🎩 Tailored Linen Blazers, Tuxedos & Loafers",
        "High-Streetwear & Urban Edgy" to "👟 Cargo Pants, Graphic Tops & Designer Sneakers",
        "Clean Minimalist Modern" to "🤍 Neutral Tones, Crisp Shirts & Monochromatic Cuts",
        "Glamorous Nightlife & Red Carpet" to "✨ Sequins, Satin Tuxedos & Statement Blazers"
    )

    val vibes = listOf(
        "Ethnic Chic & Modern",
        "Minimalist & Clean",
        "Streetwear & Edgy",
        "Formal Elegance",
        "Bohemian",
        "Casual Cool"
    )

    val palettes = listOf(
        "Rich & Warm Neutrals",
        "Pastels & Soft Tones",
        "Jewel Tones (Emerald/Ruby)",
        "Monochrome (Black/White)",
        "Vibrant & Bold"
    )

    val fits = listOf("Slim Fit", "Tailored Regular", "Relaxed / Oversized")
    val topSizes = listOf("S", "M", "L", "XL", "XXL")
    val bottomSizes = listOf("28", "30", "32", "34", "36", "38")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("style_discovery_quiz_dialog"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STYLE DISCOVERY QUIZ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Step $step of 4: " + when (step) {
                                1 -> "Fashion Inspiration"
                                2 -> "Aesthetic Vibe"
                                3 -> "Color Palette"
                                else -> "Fit & Sizing Metrics"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Progress Indicator Bar
                        LinearProgressIndicator(
                            progress = { step / 4f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Step Content
                when (step) {
                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Which fashion aesthetic inspires you most?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            inspirations.forEach { (title, desc) ->
                                val isSelected = selectedInspiration == title
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quiz_inspiration_$title"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    onClick = {
                                        selectedInspiration = title
                                        selectedVibe = when (title) {
                                            "Royal Ethnic Tradition & Heritage" -> "Ethnic Chic & Modern"
                                            "Italian Sartorial Elegance" -> "Formal Elegance"
                                            "High-Streetwear & Urban Edgy" -> "Streetwear & Edgy"
                                            "Clean Minimalist Modern" -> "Minimalist & Clean"
                                            else -> "Casual Cool"
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "What vibe describes your dream wardrobe?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                vibes.forEach { vibe ->
                                    val isSelected = selectedVibe == vibe
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedVibe = vibe },
                                        label = { Text(vibe, fontSize = 12.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("quiz_vibe_$vibe")
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Which color palette makes you feel confident?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            palettes.forEach { palette ->
                                val isSelected = selectedPalette == palette
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPalette = palette },
                                    label = { Text(palette, fontSize = 13.sp, modifier = Modifier.fillMaxWidth()) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("quiz_palette_$palette")
                                )
                            }
                        }
                    }

                    4 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Fit Cut & Clothing Sizes:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Fit
                            Text("Preferred Cut:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                fits.forEach { fit ->
                                    FilterChip(
                                        selected = selectedFit == fit,
                                        onClick = { selectedFit = fit },
                                        label = { Text(fit, fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Top Size
                            Text("Top Size:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                topSizes.forEach { sz ->
                                    FilterChip(
                                        selected = selectedTopSize == sz,
                                        onClick = { selectedTopSize = sz },
                                        label = { Text(sz, fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Bottom Size
                            Text("Waist Size:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                bottomSizes.forEach { sz ->
                                    FilterChip(
                                        selected = selectedBottomSize == sz,
                                        onClick = { selectedBottomSize = sz },
                                        label = { Text(sz, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (step < 4) {
                Button(
                    onClick = { step++ },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("quiz_next_button")
                ) {
                    Text("Next Step")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.NavigateNext, contentDescription = null)
                }
            } else {
                Button(
                    onClick = {
                        val preference = UserStylePreference(
                            id = 1,
                            topSize = selectedTopSize,
                            bottomSize = selectedBottomSize,
                            shoeSize = selectedShoeSize,
                            primaryAestheticVibe = selectedVibe,
                            colorPreferences = selectedPalette,
                            preferredDressCodes = selectedDressCode,
                            preferredFit = selectedFit
                        )
                        onCompleteQuiz(preference)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("quiz_finish_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save & Generate Style DNA")
                }
            }
        },
        dismissButton = {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Skip Quiz")
                }
            }
        }
    )
}
