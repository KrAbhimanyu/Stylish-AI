package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.OutfitRecommendation
import com.example.util.OutfitImageExporter
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutfitRecommendationCard(
    recommendation: OutfitRecommendation,
    onSaveOutfit: () -> Unit,
    modifier: Modifier = Modifier,
    onRateOutfit: (Int) -> Unit = {},
    onRegenerateThisOption: (() -> Unit)? = null,
    isRegeneratingThisOption: Boolean = false,
    activeOptionIndex: Int = 0,
    onDownloadToGallery: (() -> Unit)? = null,
    onPushToCalendar: (() -> Unit)? = null,
    onDislikeOutfit: ((reason: String, note: String) -> Unit)? = null
) {
    var isSaved by remember { mutableStateOf(false) }
    var showOutfitImagePreview by remember { mutableStateOf(true) }
    var showDislikeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val renderedImagePath = remember(recommendation) {
        try {
            OutfitImageExporter.generateOutfitCardImage(context, recommendation)
        } catch (e: Exception) {
            ""
        }
    }
    val outfitImageFile = remember(renderedImagePath) {
        if (renderedImagePath.isNotBlank()) File(renderedImagePath) else null
    }

    val talkBackAltText = remember(recommendation, activeOptionIndex) {
        "Outfit Option ${activeOptionIndex + 1}: ${recommendation.title}. " +
        "Style Vibe: ${recommendation.vibeTag}. " +
        "Target Persona: ${recommendation.targetPersona}. " +
        "Main Top: ${recommendation.top}. " +
        "Main Bottom: ${recommendation.bottom}. " +
        (if (recommendation.outerwear.isNotBlank()) "Outerwear Layer: ${recommendation.outerwear}. " else "") +
        "Footwear: ${recommendation.footwear}. " +
        (if (recommendation.accessories.isNotEmpty()) "Accessories: ${recommendation.accessories.joinToString(", ")}. " else "") +
        "Styling Tip: ${recommendation.stylingTips}. " +
        (if (recommendation.outfitImagePrompt.isNotBlank()) "AI Visual Prompt: ${recommendation.outfitImagePrompt}." else "")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = talkBackAltText
            }
            .testTag("outfit_recommendation_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Stylist Avatar & Persona Visual Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth().testTag("ai_stylist_avatar_banner")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (recommendation.isCoupleOutfit) "AURA • DUAL AVATARS FOR COUPLE LOOK" else "AURA • AI STYLIST AVATAR FOR ${recommendation.targetPersona.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "AVATAR GENERATED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (recommendation.isCoupleOutfit) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Partner 1 Husband Avatar Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        val p1Url = recommendation.partner1?.avatarImageUrl
                                        if (!p1Url.isNullOrBlank()) {
                                            AsyncImage(
                                                model = p1Url,
                                                contentDescription = "Partner 1 Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Text("👨", fontSize = 22.sp, modifier = Modifier.wrapContentSize(Alignment.Center))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(recommendation.partner1?.partnerTitle ?: "Husband / Groom", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = recommendation.partner1?.avatarImagePrompt?.take(35)?.let { "$it..." } ?: "Style Coordinated Avatar",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Partner 2 Wife Avatar Card
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        val p2Url = recommendation.partner2?.avatarImageUrl
                                        if (!p2Url.isNullOrBlank()) {
                                            AsyncImage(
                                                model = p2Url,
                                                contentDescription = "Partner 2 Avatar",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                                            )
                                        } else {
                                            Text("👩", fontSize = 22.sp, modifier = Modifier.wrapContentSize(Alignment.Center))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(recommendation.partner2?.partnerTitle ?: "Wife / Bride", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = recommendation.partner2?.avatarImagePrompt?.take(35)?.let { "$it..." } ?: "Style Coordinated Avatar",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Individual Avatar Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (emoji, roleTitle) = when (recommendation.targetPersona) {
                                    "Boy" -> "👦" to "Boy / Young Gentleman"
                                    "Girl" -> "👧" to "Girl / Young Lady"
                                    "Kid" -> "👶" to "Junior Kid Fit"
                                    "Wife" -> "👩" to "Wife / Lady"
                                    "Husband" -> "👨" to "Husband / Gentleman"
                                    else -> "✨" to recommendation.targetPersona
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    if (recommendation.avatarImageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = recommendation.avatarImageUrl,
                                            contentDescription = "Persona Avatar Image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else {
                                        Text(emoji, fontSize = 26.sp, modifier = Modifier.wrapContentSize(Alignment.Center))
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(roleTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (recommendation.avatarImagePrompt.isNotBlank()) "Avatar Prompt: “${recommendation.avatarImagePrompt}”"
                                        else "“I tailored this ${recommendation.vibeTag} look specifically for ${recommendation.targetPersona}!”",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (recommendation.isAiGenerated) "GEMINI STYLIST" else "STYLING ENGINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = recommendation.vibeTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = recommendation.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        isSaved = true
                        onSaveOutfit()
                    },
                    modifier = Modifier.testTag("save_outfit_button")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.CheckCircle else Icons.Filled.BookmarkBorder,
                        contentDescription = "Save Outfit",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Rendered Visual Outfit Showcase Card (using returned option image URL or rendered bitmap file)
            val displayImageModel = recommendation.outfitImageUrl.ifBlank { outfitImageFile?.absolutePath ?: "" }
            if (displayImageModel.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .testTag("outfit_visual_image_card"),
                    onClick = { showOutfitImagePreview = !showOutfitImagePreview }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = displayImageModel,
                            contentDescription = "AI Outfit Visual Card Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(bottomEnd = 16.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (recommendation.isCoupleOutfit) "Rendered Couple Outfit Visual Card" else "Rendered ${recommendation.targetPersona} Outfit Visual Card",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (onDownloadToGallery != null) {
                                SmallFloatingActionButton(
                                    onClick = onDownloadToGallery,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.testTag("download_to_gallery_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Download Image to Gallery",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Image", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (onPushToCalendar != null) {
                                SmallFloatingActionButton(
                                    onClick = onPushToCalendar,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.testTag("push_to_calendar_reminder_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Push Outfit to Calendar Reminder",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Calendar Reminder", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(topStart = 12.dp),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = "Tap to view full high-res image",
                                fontSize = 10.sp,
                                color = Color.LightGray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Display Gemini AI Image Generation Prompt Card
            if (recommendation.outfitImagePrompt.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().testTag("outfit_image_prompt_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GEMINI IMAGE GENERATION PROMPT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "“${recommendation.outfitImagePrompt}”",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Garments Breakdown List
            if (recommendation.isCoupleOutfit && recommendation.partner1 != null && recommendation.partner2 != null) {
                Text(
                    text = "PAIRED OUTFIT LOOKS FOR COUPLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                // Partner 1 Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "👨 ${recommendation.partner1.partnerTitle}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutfitItemRow(label = "Top / Ethnic Wear", value = recommendation.partner1.top, isMatchedInCloset = false)
                        OutfitItemRow(label = "Bottom / Trousers", value = recommendation.partner1.bottom, isMatchedInCloset = false)
                        if (recommendation.partner1.outerwear.isNotBlank()) {
                            OutfitItemRow(label = "Outerwear / Jacket", value = recommendation.partner1.outerwear, isMatchedInCloset = false)
                        }
                        OutfitItemRow(label = "Footwear", value = recommendation.partner1.footwear, isMatchedInCloset = false)
                    }
                }

                // Partner 2 Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "👩 ${recommendation.partner2.partnerTitle}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        OutfitItemRow(label = "Top / Dress / Lehenga", value = recommendation.partner2.top, isMatchedInCloset = false)
                        OutfitItemRow(label = "Bottom / Skirt", value = recommendation.partner2.bottom, isMatchedInCloset = false)
                        if (recommendation.partner2.outerwear.isNotBlank()) {
                            OutfitItemRow(label = "Dupatta / Layer", value = recommendation.partner2.outerwear, isMatchedInCloset = false)
                        }
                        OutfitItemRow(label = "Footwear", value = recommendation.partner2.footwear, isMatchedInCloset = false)
                    }
                }
            } else {
                Text(
                    text = "OUTFIT COMPONENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutfitItemRow(label = "Top / Main", value = recommendation.top, isMatchedInCloset = recommendation.closetItemMatches.any { it.contains(recommendation.top, ignoreCase = true) })
                    OutfitItemRow(label = "Bottom / Trousers", value = recommendation.bottom, isMatchedInCloset = recommendation.closetItemMatches.any { it.contains(recommendation.bottom, ignoreCase = true) })

                    if (recommendation.outerwear.isNotBlank()) {
                        OutfitItemRow(label = "Outerwear / Layer", value = recommendation.outerwear, isMatchedInCloset = recommendation.closetItemMatches.any { it.contains(recommendation.outerwear, ignoreCase = true) })
                    }

                    OutfitItemRow(label = "Footwear", value = recommendation.footwear, isMatchedInCloset = recommendation.closetItemMatches.any { it.contains(recommendation.footwear, ignoreCase = true) })
                }
            }

            // Accessories Chips
            if (recommendation.accessories.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ACCESSORIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        recommendation.accessories.forEach { acc ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(acc, fontSize = 12.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Color Palette
            if (recommendation.colorPaletteNames.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "COLOR PALETTE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = recommendation.colorPaletteNames.joinToString(" • "),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        recommendation.colorPaletteHexes.forEach { hexStr ->
                            val parsedColor = try {
                                Color(android.graphics.Color.parseColor(hexStr))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                        }
                    }
                }
            }

            // Weather Comfort Advisory Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Weather",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Weather Comfort Match",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${recommendation.weatherComfortScore}% Match",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = recommendation.weatherAdvice,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // Styling Tips
            if (recommendation.stylingTips.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Tips",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Stylist Pro-Tip",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = recommendation.stylingTips,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Missing Pieces & Trend Accessories Suggestions
            if (recommendation.missingPiecesToElevate.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Trend Upgrade",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MISSING PIECES TO ELEVATE LOOK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Trend Suggestions",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    recommendation.missingPiecesToElevate.forEach { piece ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("missing_piece_card_${piece.itemName.lowercase().replace(" ", "_")}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = piece.itemName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${piece.category} • ${piece.trendTag}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Text(
                                    text = piece.reasonToElevate,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )

                                OutlinedButton(
                                    onClick = {
                                        val query = if (piece.searchQuery.isNotBlank()) piece.searchQuery else piece.itemName
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)))
                                        try {
                                            context.startActivity(webIntent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .testTag("search_missing_piece_${piece.itemName.lowercase().replace(" ", "_")}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Online",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Find Online", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Regenerate Single Option Button
            if (onRegenerateThisOption != null) {
                OutlinedButton(
                    onClick = onRegenerateThisOption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("regenerate_single_option_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isRegeneratingThisOption) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Regenerating Option ${activeOptionIndex + 1}...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate Option",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Not liking Option ${activeOptionIndex + 1}? Regenerate This Item",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Rating & Dislike Reason Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RATE OR REFINE THIS LOOK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (recommendation.userRating > 0) "Rating saved! Future AI suggestions refined." else "Tap stars or Dislike to refine AI taste",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { showDislikeDialog = true },
                        modifier = Modifier.testTag("outfit_dislike_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Dislike and Provide Reason",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    StarRatingBar(
                        rating = recommendation.userRating,
                        onRatingSelected = { newRating ->
                            onRateOutfit(newRating)
                        },
                        starSize = 22.dp
                    )
                }
            }

            if (showDislikeDialog) {
                OutfitDislikeFeedbackDialog(
                    outfitTitle = recommendation.title,
                    onDismiss = { showDislikeDialog = false },
                    onSubmitFeedback = { reason, note ->
                        onDislikeOutfit?.invoke(reason, note)
                    }
                )
            }
        }
    }
}

@Composable
private fun OutfitItemRow(
    label: String,
    value: String,
    isMatchedInCloset: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isMatchedInCloset) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "In Closet",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "In Closet",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
