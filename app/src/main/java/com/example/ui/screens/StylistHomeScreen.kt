package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.OutfitRecommendation
import com.example.ui.components.DailyStyleInsightCard
import com.example.ui.components.OccasionChipSelector
import com.example.ui.components.OutfitRecommendationCard
import com.example.ui.components.PerformanceMetricsCard
import com.example.ui.components.SeasonalTrendsCard
import com.example.ui.components.StyleDiscoveryQuizDialog
import com.example.ui.components.StyleTrendsChartCard
import com.example.ui.components.VoiceCommandHelpDialog
import com.example.ui.components.WeatherContextBar
import com.example.ui.viewmodel.StylistViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StylistHomeScreen(
    viewModel: StylistViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val savedOutfits by viewModel.savedOutfits.collectAsState()
    var promptInput by remember { mutableStateOf("") }
    var showVoiceHelpDialog by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                promptInput = spokenText
                viewModel.generateRecommendation(spokenText, uiState.selectedOccasion)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your mood or event requirement...")
                }
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice command.", Toast.LENGTH_SHORT).show()
        }
    }

    var showQuizDialog by remember { mutableStateOf(false) }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.scanAndSyncCalendar(context)
    }

    val quickPrompts = listOf(
        "Marriage Sangeet evening dance fit",
        "Hot summer beach day outing",
        "Smart casual Friday office pitch",
        "Rooftop cocktail party night"
    )

    val performanceLogs by viewModel.performanceLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("stylist_home_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Weather Context Header
        item {
            WeatherContextBar(
                weather = uiState.weather,
                onUpdateWeather = { viewModel.updateWeather(it) }
            )
        }

        // Performance & Latency Monitor Card
        item {
            PerformanceMetricsCard(logs = performanceLogs)
        }

        // Offline Cached Data Alert Pill
        if (uiState.isOfflineCachedData) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth().testTag("offline_cached_data_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📱 Offline Mode: Displaying locally cached AI recommendations",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Calendar Dress Code Scanner & Quiz Banners
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Calendar Scanner Banner
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("calendar_permission_scanner_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Calendar Dress Code Scanner",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Scan upcoming events for dress codes",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                calendarPermissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("scan_calendar_permission_button")
                        ) {
                            Text("Scan Events", fontSize = 11.sp)
                        }
                    }
                }

                // Interactive Style Discovery Quiz Banner
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("launch_quiz_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Style Discovery Quiz",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Answer 4 quick questions for personalized AI DNA",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Button(
                            onClick = { showQuizDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("start_style_quiz_button")
                        ) {
                            Text("Take Quiz", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Gemini Seasonal Trends Card
        item {
            SeasonalTrendsCard(
                trends = uiState.seasonalTrends,
                isLoading = uiState.isLoadingTrends,
                onRefreshTrends = { viewModel.fetchSeasonalTrends() }
            )
        }

        // Daily Actionable Style Insight
        item {
            DailyStyleInsightCard(
                stylePreference = uiState.stylePreference,
                onApplyTipToOutfit = { tip ->
                    viewModel.generateRecommendation(
                        prompt = "Suggest 5 outfits applying this daily style insight: $tip",
                        occasion = uiState.selectedOccasion,
                        persona = uiState.selectedTargetPersona,
                        gender = uiState.selectedGender
                    )
                }
            )
        }

        // Data Visualization Chart: 30-Day Popular Colors & Style Trends Line Chart
        item {
            StyleTrendsChartCard(savedOutfits = savedOutfits)
        }

        // Occasion Chips
        item {
            OccasionChipSelector(
                selectedOccasion = uiState.selectedOccasion,
                onOccasionSelected = { option ->
                    viewModel.updateOccasion(option.title)
                    promptInput = option.defaultPrompt
                    viewModel.generateRecommendation(option.defaultPrompt, option.title, uiState.selectedTargetPersona)
                }
            )
        }

        // Mandatory Gender Selection Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("gender_selection_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GENDER SELECTION (MANDATORY)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "REQUIRED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val genders = listOf("Male", "Female", "Other")
                        genders.forEach { gender ->
                            val isSelected = uiState.selectedGender.equals(gender, ignoreCase = true)
                            val labelText = when (gender) {
                                "Male" -> "👨 Male"
                                "Female" -> "👩 Female"
                                else -> "⚧ Other"
                            }
                            FilterChip(
                                modifier = Modifier.weight(1f).testTag("gender_chip_$gender"),
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateGender(gender)
                                    viewModel.generateRecommendation(promptInput, uiState.selectedOccasion, uiState.selectedTargetPersona, gender)
                                },
                                label = {
                                    Text(
                                        text = labelText,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Target Persona Selector (Couple, Husband, Wife, Boy, Girl, Kid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "TARGET PERSONA / LOOK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                val personas = listOf("Couple", "Husband", "Wife", "Boy", "Girl", "Kid")
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(personas) { persona ->
                        val isSelected = uiState.selectedTargetPersona.equals(persona, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.updateTargetPersona(persona)
                                viewModel.generateRecommendation(promptInput, uiState.selectedOccasion, persona, uiState.selectedGender)
                            },
                            label = {
                                val labelText = when (persona) {
                                    "Couple" -> "Couple 💑"
                                    "Husband" -> "Husband 👨"
                                    "Wife" -> "Wife 👩"
                                    "Boy" -> "Boy 👦"
                                    "Girl" -> "Girl 👧"
                                    "Kid" -> "Kid 👶"
                                    else -> persona
                                }
                                Text(labelText, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Interactive Prompt Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tell Stylist Agent what you want to wear for:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        placeholder = { Text("e.g., I'm going to a cousin's wedding sangeet tonight...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stylist_prompt_input"),
                        shape = RoundedCornerShape(16.dp),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { showVoiceHelpDialog = true },
                                    modifier = Modifier.testTag("voice_command_help_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = "Voice Command Help & Examples",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val hasPerm = androidx.core.content.ContextCompat.checkSelfPermission(
                                            context,
                                            android.Manifest.permission.RECORD_AUDIO
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                        if (hasPerm) {
                                            try {
                                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your mood or event requirements...")
                                                }
                                                speechRecognizerLauncher.launch(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Voice command unavailable on this device", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    modifier = Modifier.testTag("voice_input_mic_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Input Microphone",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val pLower = promptInput.lowercase()
                                        val detectedPersona = when {
                                            pLower.contains("boy") || pLower.contains("son") -> "Boy"
                                            pLower.contains("girl") || pLower.contains("daughter") -> "Girl"
                                            pLower.contains("kid") || pLower.contains("toddler") -> "Kid"
                                            pLower.contains("wife") || pLower.contains("women") || pLower.contains("lady") -> "Wife"
                                            pLower.contains("husband") || pLower.contains("men") || pLower.contains("man") || pLower.contains("male") -> "Husband"
                                            pLower.contains("couple") || pLower.contains("both") -> "Couple"
                                            else -> uiState.selectedTargetPersona
                                        }
                                        viewModel.updateTargetPersona(detectedPersona)
                                        viewModel.generateRecommendation(promptInput, uiState.selectedOccasion, detectedPersona)
                                    },
                                    modifier = Modifier.testTag("ask_ai_stylist_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send Prompt",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )

                    // Quick Prompt Chips
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        quickPrompts.forEach { qp ->
                            SuggestionChip(
                                onClick = {
                                    promptInput = qp
                                    val qpLower = qp.lowercase()
                                    val detectedPersona = when {
                                        qpLower.contains("boy") || qpLower.contains("son") -> "Boy"
                                        qpLower.contains("girl") || qpLower.contains("daughter") -> "Girl"
                                        qpLower.contains("kid") || qpLower.contains("toddler") -> "Kid"
                                        qpLower.contains("wife") || qpLower.contains("women") || qpLower.contains("lady") -> "Wife"
                                        qpLower.contains("husband") || qpLower.contains("men") || qpLower.contains("man") || qpLower.contains("male") -> "Husband"
                                        qpLower.contains("couple") || qpLower.contains("both") -> "Couple"
                                        else -> uiState.selectedTargetPersona
                                    }
                                    viewModel.updateTargetPersona(detectedPersona)
                                    viewModel.generateRecommendation(qp, uiState.selectedOccasion, detectedPersona)
                                },
                                label = { Text(qp, fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Loading State
        if (uiState.isLoadingRecommendation) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Curating custom outfit from weather & wardrobe...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 5 Outfit Suggestions Carousel with Smooth Horizontal Swipe Pager
        if (uiState.outfitSuggestionsList.isNotEmpty()) {
            item {
                val pagerState = androidx.compose.foundation.pager.rememberPagerState(
                    initialPage = uiState.activeRecommendationIndex,
                    pageCount = { uiState.outfitSuggestionsList.size }
                )

                LaunchedEffect(uiState.activeRecommendationIndex) {
                    if (pagerState.currentPage != uiState.activeRecommendationIndex && uiState.outfitSuggestionsList.isNotEmpty()) {
                        pagerState.animateScrollToPage(uiState.activeRecommendationIndex)
                    }
                }

                LaunchedEffect(pagerState.currentPage) {
                    if (pagerState.currentPage != uiState.activeRecommendationIndex) {
                        viewModel.selectSuggestionIndex(pagerState.currentPage)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "5 AI OUTFIT SUGGESTIONS CAROUSEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Option ${uiState.activeRecommendationIndex + 1} of ${uiState.outfitSuggestionsList.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.outfitSuggestionsList.size) { index ->
                            val item = uiState.outfitSuggestionsList[index]
                            val isSelected = index == uiState.activeRecommendationIndex
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectSuggestionIndex(index) },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Option ${index + 1}: ${item.vibeTag}",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        IconButton(
                                            onClick = { viewModel.regenerateOptionAtIndex(index) },
                                            modifier = Modifier.size(18.dp).testTag("regenerate_option_chip_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Regenerate Option ${index + 1}",
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Text(
                        text = "👈 Swipe horizontally between the 5 suggested looks 👉",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    AnimatedVisibility(
                        visible = !uiState.isLoadingRecommendation,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().testTag("outfit_carousel_pager"),
                            pageSpacing = 16.dp
                        ) { page ->
                            val rec = uiState.outfitSuggestionsList[page]
                            OutfitRecommendationCard(
                                recommendation = rec,
                                onSaveOutfit = { viewModel.saveCurrentRecommendation(context) },
                                onRateOutfit = { rating -> viewModel.rateCurrentRecommendation(rating) },
                                onRegenerateThisOption = { viewModel.regenerateOptionAtIndex(page) },
                                isRegeneratingThisOption = uiState.isRegeneratingSingleOption && (page == uiState.activeRecommendationIndex),
                                activeOptionIndex = page,
                                onDownloadToGallery = { viewModel.downloadOutfitImageToGallery(context, rec) },
                                onPushToCalendar = { viewModel.pushOutfitToCalendarReminder(context, rec) },
                                onDislikeOutfit = { reason, note ->
                                    viewModel.recordOutfitDislikeFeedback(rec, reason, note)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Styling History
        val historyList = uiState.recommendationHistory.filterNot { it.title == uiState.currentRecommendation?.title && it.top == uiState.currentRecommendation?.top }
        if (historyList.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT OUTFIT INSPIRATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(historyList) { rec ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectRecommendation(rec) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = rec.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${rec.occasion} • ${rec.top} + ${rec.bottom}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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

    if (showVoiceHelpDialog) {
        VoiceCommandHelpDialog(
            onDismiss = { showVoiceHelpDialog = false },
            onSelectCommand = { command ->
                promptInput = command
                showVoiceHelpDialog = false
                viewModel.generateRecommendation(
                    prompt = command,
                    occasion = uiState.selectedOccasion,
                    persona = uiState.selectedTargetPersona,
                    gender = uiState.selectedGender
                )
            }
        )
    }
}
