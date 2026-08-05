package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStylePreference
import com.example.ui.components.StyleDiscoveryQuizDialog
import com.example.ui.viewmodel.StylistViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StyleProfileScreen(
    viewModel: StylistViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val roomPref by viewModel.userStylePreference.collectAsState()
    val smartAnalysis by viewModel.smartStyleAnalysis.collectAsState()

    var showQuizDialog by remember { mutableStateOf(false) }

    // Mutable state for editing preferences before saving
    var selectedTopSize by remember(roomPref) { mutableStateOf(roomPref?.topSize ?: "L") }
    var selectedBottomSize by remember(roomPref) { mutableStateOf(roomPref?.bottomSize ?: "32") }
    var selectedShoeSize by remember(roomPref) { mutableStateOf(roomPref?.shoeSize ?: "UK 10") }
    var selectedVibe by remember(roomPref) { mutableStateOf(roomPref?.primaryAestheticVibe ?: "Ethnic Chic & Modern") }
    var selectedColorPref by remember(roomPref) { mutableStateOf(roomPref?.colorPreferences ?: "Rich & Warm Neutrals") }
    var selectedDressCode by remember(roomPref) { mutableStateOf(roomPref?.preferredDressCodes ?: "Festive Ethnic, Smart Casual, Formal") }
    var selectedFit by remember(roomPref) { mutableStateOf(roomPref?.preferredFit ?: "Tailored Regular") }

    val topSizes = listOf("S", "M", "L", "XL", "XXL")
    val bottomSizes = listOf("28", "30", "32", "34", "36", "38")
    val shoeSizes = listOf("UK 7", "UK 8", "UK 9", "UK 10", "UK 11")
    val fits = listOf("Slim Fit", "Tailored Regular", "Relaxed / Oversized")
    val vibes = listOf("Ethnic Chic & Modern", "Minimalist & Clean", "Streetwear & Edgy", "Formal Elegance", "Bohemian", "Casual Cool", "Preppy")
    val palettes = listOf("Rich & Warm Neutrals", "Pastels & Soft Tones", "Jewel Tones (Emerald/Ruby)", "Monochrome (Black/White)", "Vibrant & Bold")
    val dressCodes = listOf(
        "Festive Ethnic, Smart Casual, Formal",
        "Smart Casual & Brunch Wear",
        "Business Formal & Corporate",
        "High-Streetwear & Party Casual",
        "Traditional Wedding & Festive"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("style_profile_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Personal Style & Fit Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Room DB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure your size metrics, fit rules, and aesthetic style choices. Saved directly in Room database.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Interactive Style Discovery Quiz Onboarding Trigger
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("style_quiz_profile_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Style Discovery Quiz",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Answer 4 questions about fashion inspirations to populate your Room DB profile",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = { showQuizDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("launch_quiz_profile_button")
                    ) {
                        Text("Start Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 0. Smart AI Style Analysis Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("smart_style_analysis_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Smart AI Style Analysis",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Smart AI Style Analysis",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = "Auto DB Sync",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (smartAnalysis != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Analyzed ${smartAnalysis?.analyzedOutfitCount} top-rated outfits from your lookbook gallery:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            smartAnalysis?.keyInsights?.forEach { insight ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("• ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    Text(insight, fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Analyze your highest-rated outfits to automatically discover your style DNA (vibe, palette, fit) and update Room DB preferences.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Button(
                        onClick = { viewModel.runSmartStyleAnalysis() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_smart_style_analysis_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (smartAnalysis != null) "Re-run Smart Style Analysis" else "Run Smart Style Analysis Now",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. Clothing Sizes Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = "Clothing Sizes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clothing & Footwear Sizes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Top Size
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Top / Shirt Size:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            topSizes.forEach { size ->
                                val isSelected = selectedTopSize == size
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTopSize = size },
                                    label = { Text(size, fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("top_size_chip_$size")
                                )
                            }
                        }
                    }

                    // Bottom Size
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Bottom / Waist Size (Inches):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            bottomSizes.forEach { size ->
                                val isSelected = selectedBottomSize == size
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedBottomSize = size },
                                    label = { Text(size, fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("bottom_size_chip_$size")
                                )
                            }
                        }
                    }

                    // Shoe Size
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Shoe / Footwear Size:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            shoeSizes.forEach { size ->
                                val isSelected = selectedShoeSize == size
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedShoeSize = size },
                                    label = { Text(size, fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("shoe_size_chip_$size")
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Preferred Fit Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Fit Preference",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Garment Cut & Preferred Fit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fits.forEach { fit ->
                            val isSelected = selectedFit == fit
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFit = fit },
                                label = { Text(fit, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Preferred Dress Codes Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Preferred Dress Codes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Primary Dress Code Focus",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dressCodes.forEach { dc ->
                            val isSelected = selectedDressCode == dc
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDressCode = dc },
                                label = { Text(dc, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // 4. Primary Fashion Aesthetic Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Aesthetic Vibe",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aesthetic Vibe & Style DNA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

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
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Color Palette Preference Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color Palette",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Color Palette Preference",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        palettes.forEach { palette ->
                            val isSelected = selectedColorPref == palette
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedColorPref = palette },
                                label = { Text(palette, fontSize = 12.sp) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Save Button Action
        item {
            Button(
                onClick = {
                    val updatedPreference = UserStylePreference(
                        id = 1,
                        topSize = selectedTopSize,
                        bottomSize = selectedBottomSize,
                        shoeSize = selectedShoeSize,
                        primaryAestheticVibe = selectedVibe,
                        colorPreferences = selectedColorPref,
                        preferredDressCodes = selectedDressCode,
                        preferredFit = selectedFit
                    )
                    viewModel.saveUserStylePreferenceToDb(updatedPreference)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_style_preferences_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save to Database")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Settings to Room Database",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active Room Database Profile Summary Box
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Saved State",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PERSISTED ROOM DATABASE SNAPSHOT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "• Sizes: Top (${roomPref?.topSize ?: "L"}), Pants (${roomPref?.bottomSize ?: "32"}), Footwear (${roomPref?.shoeSize ?: "UK 10"})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "• Fit & Cut: ${roomPref?.preferredFit ?: "Tailored Regular"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "• Style Vibe: ${roomPref?.primaryAestheticVibe ?: "Ethnic Chic & Modern"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "• Palette: ${roomPref?.colorPreferences ?: "Rich & Warm Neutrals"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "• Dress Codes: ${roomPref?.preferredDressCodes ?: "Festive Ethnic, Smart Casual, Formal"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showQuizDialog) {
        StyleDiscoveryQuizDialog(
            onDismiss = { showQuizDialog = false },
            onCompleteQuiz = { preference ->
                viewModel.completeStyleDiscoveryQuiz(preference)
                showQuizDialog = false
            }
        )
    }
}
