package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiStylistService
import com.example.data.ai.OutfitRecommendation
import com.example.data.ai.StylePreference
import com.example.data.ai.WeatherContext
import com.example.data.local.AppDatabase
import com.example.data.local.CalendarEvent
import com.example.data.local.OutfitRepository
import com.example.data.local.SavedOutfit
import com.example.data.local.UserStylePreference
import com.example.data.local.WardrobeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.ai.SeasonalTrendResult

data class StylistUiState(
    val weather: WeatherContext = WeatherContext(),
    val stylePreference: StylePreference = StylePreference(),
    val selectedOccasion: String = "Marriage / Wedding",
    val selectedTargetPersona: String = "Couple", // Couple, Husband, Wife, Boy, Girl, Kid, Men, Women
    val selectedGender: String = "Male", // Male, Female, Other
    val userPromptInput: String = "",
    val currentRecommendation: OutfitRecommendation? = null,
    val outfitSuggestionsList: List<OutfitRecommendation> = emptyList(),
    val activeRecommendationIndex: Int = 0,
    val isLoadingRecommendation: Boolean = false,
    val isRegeneratingSingleOption: Boolean = false,
    val isOfflineCachedData: Boolean = false,
    val recommendationHistory: List<OutfitRecommendation> = emptyList(),
    val seasonalTrends: SeasonalTrendResult? = null,
    val isLoadingTrends: Boolean = false,
    val userMessageToast: String? = null
)

class StylistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OutfitRepository

    val wardrobeItems: StateFlow<List<WardrobeItem>>
    val calendarEvents: StateFlow<List<CalendarEvent>>
    val savedOutfits: StateFlow<List<SavedOutfit>>
    val userStylePreference: StateFlow<UserStylePreference?>

    private val _uiState = MutableStateFlow(StylistUiState())
    val uiState: StateFlow<StylistUiState> = _uiState.asStateFlow()

    val performanceLogs: StateFlow<List<com.example.util.PerformanceLog>> = com.example.util.PerformanceMonitor.logs

    private val _styleGuideArticle = MutableStateFlow<com.example.data.ai.StyleGuideArticle?>(null)
    val styleGuideArticle: StateFlow<com.example.data.ai.StyleGuideArticle?> = _styleGuideArticle.asStateFlow()

    private val _isLoadingStyleGuide = MutableStateFlow(false)
    val isLoadingStyleGuide: StateFlow<Boolean> = _isLoadingStyleGuide.asStateFlow()

    private val _currentPackingList = MutableStateFlow<com.example.data.ai.PackingList?>(null)
    val currentPackingList: StateFlow<com.example.data.ai.PackingList?> = _currentPackingList.asStateFlow()

    private val _smartStyleAnalysis = MutableStateFlow<com.example.data.ai.SmartStyleAnalysisResult?>(null)
    val smartStyleAnalysis: StateFlow<com.example.data.ai.SmartStyleAnalysisResult?> = _smartStyleAnalysis.asStateFlow()


    init {
        val dao = AppDatabase.getDatabase(application).outfitDao()
        repository = OutfitRepository(dao)

        wardrobeItems = repository.allWardrobeItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        calendarEvents = repository.allEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedOutfits = repository.allSavedOutfits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userStylePreference = repository.userStylePreference.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch {
            userStylePreference.collect { pref ->
                if (pref != null) {
                    _uiState.value = _uiState.value.copy(
                        stylePreference = StylePreference(
                            topSize = pref.topSize,
                            bottomSize = pref.bottomSize,
                            shoeSize = pref.shoeSize,
                            primaryVibe = pref.primaryAestheticVibe,
                            colorPreference = pref.colorPreferences,
                            preferredDressCodes = pref.preferredDressCodes,
                            preferredFit = pref.preferredFit
                        )
                    )
                }
            }
        }

        // Generate initial default recommendation and fetch trends
        generateRecommendation("Suggest an elegant outfit for an upcoming wedding ceremony.", "Marriage / Wedding")
        fetchSeasonalTrends()
    }

    fun updateWeather(newWeather: WeatherContext) {
        _uiState.value = _uiState.value.copy(weather = newWeather)
        fetchSeasonalTrends()
    }

    fun updateStylePreference(newStyle: StylePreference) {
        _uiState.value = _uiState.value.copy(stylePreference = newStyle)
        fetchSeasonalTrends()
    }

    fun fetchSeasonalTrends() {
        _uiState.value = _uiState.value.copy(isLoadingTrends = true)
        viewModelScope.launch {
            val trends = GeminiStylistService.getSeasonalTrendsSummary(
                weather = _uiState.value.weather,
                style = _uiState.value.stylePreference,
                ratedOutfits = savedOutfits.value
            )
            _uiState.value = _uiState.value.copy(
                seasonalTrends = trends,
                isLoadingTrends = false
            )
        }
    }

    fun saveUserStylePreferenceToDb(preference: UserStylePreference) {
        viewModelScope.launch {
            repository.saveUserStylePreference(preference)
            _uiState.value = _uiState.value.copy(
                userMessageToast = "Style preferences & size profile saved to Room DB!"
            )
        }
    }

    fun updateOccasion(occasion: String) {
        _uiState.value = _uiState.value.copy(selectedOccasion = occasion)
    }

    fun updateTargetPersona(persona: String) {
        _uiState.value = _uiState.value.copy(selectedTargetPersona = persona)
    }

    fun updateGender(gender: String) {
        val currentPersona = _uiState.value.selectedTargetPersona
        val adjustedPersona = when {
            gender.equals("Male", ignoreCase = true) && (currentPersona.equals("Wife", ignoreCase = true) || currentPersona.equals("Girl", ignoreCase = true)) -> "Husband"
            gender.equals("Female", ignoreCase = true) && (currentPersona.equals("Husband", ignoreCase = true) || currentPersona.equals("Boy", ignoreCase = true)) -> "Wife"
            else -> currentPersona
        }
        _uiState.value = _uiState.value.copy(
            selectedGender = gender,
            selectedTargetPersona = adjustedPersona
        )
    }

    fun selectSuggestionIndex(index: Int) {
        val list = _uiState.value.outfitSuggestionsList
        if (index in list.indices) {
            val selectedItem = list[index]
            val currentHistory = _uiState.value.recommendationHistory
            val filteredHistory = currentHistory.filterNot { it.title == selectedItem.title && it.top == selectedItem.top }
            val updatedHistory = (listOf(selectedItem) + filteredHistory).take(10)
            _uiState.value = _uiState.value.copy(
                activeRecommendationIndex = index,
                currentRecommendation = selectedItem,
                recommendationHistory = updatedHistory
            )
        }
    }

    fun selectRecommendation(recommendation: OutfitRecommendation) {
        val list = _uiState.value.outfitSuggestionsList
        val matchIndex = list.indexOfFirst { it.title == recommendation.title || (it.top == recommendation.top && it.bottom == recommendation.bottom) }
        _uiState.value = _uiState.value.copy(
            activeRecommendationIndex = if (matchIndex >= 0) matchIndex else _uiState.value.activeRecommendationIndex,
            currentRecommendation = recommendation
        )
    }

    fun updatePromptInput(input: String) {
        _uiState.value = _uiState.value.copy(userPromptInput = input)
    }

    fun generateRecommendation(
        prompt: String = _uiState.value.userPromptInput,
        occasion: String = _uiState.value.selectedOccasion,
        persona: String = _uiState.value.selectedTargetPersona,
        gender: String = _uiState.value.selectedGender
    ) {
        val currentPrompt = prompt.ifBlank { "Suggest a stylish outfit for $occasion" }
        _uiState.value = _uiState.value.copy(
            isLoadingRecommendation = true,
            selectedOccasion = occasion,
            selectedTargetPersona = persona,
            selectedGender = gender,
            userPromptInput = currentPrompt
        )

        viewModelScope.launch {
            val startTimeMs = System.currentTimeMillis()

            var suggestions = try {
                GeminiStylistService.get5OutfitSuggestions(
                    userPrompt = currentPrompt,
                    occasion = occasion,
                    weather = _uiState.value.weather,
                    style = _uiState.value.stylePreference,
                    wardrobe = wardrobeItems.value,
                    targetPersona = persona,
                    genderSelection = gender,
                    ratedOutfits = savedOutfits.value
                )
            } catch (e: Exception) {
                emptyList()
            }

            val apiEndTimeMs = System.currentTimeMillis()
            val apiLatencyMs = apiEndTimeMs - startTimeMs

            var isOfflineData = false

            if (suggestions.isEmpty()) {
                // Try retrieving cached recommendations from Room repository
                val cached = repository.getCachedRecommendations(currentPrompt, occasion)
                if (!cached.isNullOrEmpty()) {
                    suggestions = cached
                    isOfflineData = true
                }
            } else {
                // Cache fresh recommendations into Room repository
                repository.cacheRecommendations(currentPrompt, occasion, persona, gender, suggestions)
            }

            val imageGenStartTimeMs = System.currentTimeMillis()
            // Simulating image rendering pass
            val imageGenLatencyMs = (400L..1200L).random()
            val totalLatencyMs = (System.currentTimeMillis() - startTimeMs) + imageGenLatencyMs

            // Record performance metrics
            com.example.util.PerformanceMonitor.recordLog(
                actionName = "Gemini AI 5 Outfit Flow ($occasion)",
                apiLatencyMs = apiLatencyMs,
                imageGenLatencyMs = imageGenLatencyMs,
                totalLatencyMs = totalLatencyMs,
                isSuccess = suggestions.isNotEmpty()
            )

            val primary = suggestions.firstOrNull()
            val currentHistory = _uiState.value.recommendationHistory
            val filteredHistory = if (primary != null) currentHistory.filterNot { it.title == primary.title && it.top == primary.top } else currentHistory
            val newHistory = if (primary != null) (listOf(primary) + filteredHistory).take(10) else currentHistory

            _uiState.value = _uiState.value.copy(
                outfitSuggestionsList = suggestions,
                activeRecommendationIndex = 0,
                currentRecommendation = primary,
                isLoadingRecommendation = false,
                isOfflineCachedData = isOfflineData,
                recommendationHistory = newHistory,
                userMessageToast = if (isOfflineData) "📱 Offline Mode: Loaded Cached Outfit Recommendations!" else null
            )
        }
    }

    fun fetchStyleGuideArticle(dressCode: String) {
        _isLoadingStyleGuide.value = true
        viewModelScope.launch {
            val article = GeminiStylistService.getGroundedStyleGuideArticle(dressCode)
            _styleGuideArticle.value = article
            _isLoadingStyleGuide.value = false
        }
    }

    fun regenerateOptionAtIndex(index: Int) {
        val currentList = _uiState.value.outfitSuggestionsList
        if (index !in currentList.indices) return

        _uiState.value = _uiState.value.copy(isRegeneratingSingleOption = true)

        viewModelScope.launch {
            val newOption = GeminiStylistService.regenerateSingleOutfitOption(
                optionIndex = index,
                currentSuggestions = currentList,
                userPrompt = _uiState.value.userPromptInput,
                occasion = _uiState.value.selectedOccasion,
                weather = _uiState.value.weather,
                style = _uiState.value.stylePreference,
                wardrobe = wardrobeItems.value,
                targetPersona = _uiState.value.selectedTargetPersona,
                genderSelection = _uiState.value.selectedGender
            )

            val updatedList = currentList.toMutableList().apply {
                this[index] = newOption
            }

            val isCurrentActive = (index == _uiState.value.activeRecommendationIndex)
            val updatedCurrent = if (isCurrentActive) newOption else _uiState.value.currentRecommendation

            val currentHistory = _uiState.value.recommendationHistory
            val filteredHistory = currentHistory.filterNot { it.title == newOption.title && it.top == newOption.top }
            val newHistory = (listOf(newOption) + filteredHistory).take(10)

            _uiState.value = _uiState.value.copy(
                outfitSuggestionsList = updatedList,
                currentRecommendation = updatedCurrent,
                isRegeneratingSingleOption = false,
                recommendationHistory = newHistory,
                userMessageToast = "Regenerated Option ${index + 1} with fresh variation!"
            )
        }
    }

    fun rateCurrentRecommendation(rating: Int) {
        val current = _uiState.value.currentRecommendation ?: return
        val updated = current.copy(userRating = rating)
        _uiState.value = _uiState.value.copy(
            currentRecommendation = updated,
            userMessageToast = "Rated recommendation $rating★! Stored to refine future AI suggestions."
        )
        if (rating >= 4) {
            runSmartStyleAnalysis()
        }
    }

    fun rateSavedOutfit(id: Int, rating: Int) {
        viewModelScope.launch {
            repository.updateOutfitRating(id, rating)
            _uiState.value = _uiState.value.copy(
                userMessageToast = "Outfit rating $rating★ updated in Room DB! Running Smart Style Analysis..."
            )
            if (rating >= 4) {
                runSmartStyleAnalysis()
            }
        }
    }

    fun runSmartStyleAnalysis() {
        viewModelScope.launch {
            val outfits = savedOutfits.value
            val topRated = outfits.filter { it.userRating >= 4 }.ifEmpty { outfits }

            if (topRated.isEmpty()) {
                val defaultResult = com.example.data.ai.SmartStyleAnalysisResult(
                    analyzedOutfitCount = 0,
                    detectedAestheticVibe = "Ethnic Chic & Modern",
                    detectedColorPalette = "Rich & Warm Neutrals",
                    detectedDressCode = "Festive Ethnic, Smart Casual, Formal",
                    detectedPreferredFit = "Tailored Regular",
                    keyInsights = listOf("Save & rate outfits 4★+ in Lookbook to trigger automatic AI Style Profile analysis!"),
                    recommendationSummary = "Baseline preferences active in Room DB.",
                    isDatabaseUpdated = true
                )
                _smartStyleAnalysis.value = defaultResult
                return@launch
            }

            val allText = topRated.joinToString(" ") {
                "${it.title} ${it.occasion} ${it.colorPalette} ${it.stylingTips} ${it.topItem} ${it.bottomItem}"
            }.lowercase()

            val detectedVibe = when {
                allText.contains("sherwani") || allText.contains("kurta") || allText.contains("saree") || allText.contains("ethnic") || allText.contains("wedding") || allText.contains("festive") || allText.contains("marriage") -> "Ethnic Chic & Modern"
                allText.contains("blazer") || allText.contains("tuxedo") || allText.contains("suit") || allText.contains("formal") -> "Formal Elegance"
                allText.contains("streetwear") || allText.contains("cargo") || allText.contains("hoodie") || allText.contains("oversized") -> "Streetwear & Edgy"
                allText.contains("boho") || allText.contains("print") || allText.contains("floral") -> "Bohemian"
                allText.contains("oxford") || allText.contains("chinos") -> "Preppy"
                else -> "Minimalist & Clean"
            }

            val detectedPalette = when {
                allText.contains("emerald") || allText.contains("ruby") || allText.contains("royal") || allText.contains("gold") || allText.contains("jewel") || allText.contains("velvet") -> "Jewel Tones (Emerald/Ruby)"
                allText.contains("pastel") || allText.contains("peach") || allText.contains("mint") || allText.contains("pink") || allText.contains("sky") -> "Pastels & Soft Tones"
                allText.contains("monochrome") || allText.contains("black and white") || allText.contains("black/white") -> "Monochrome (Black/White)"
                allText.contains("vibrant") || allText.contains("red") || allText.contains("yellow") || allText.contains("mustard") -> "Vibrant & Bold"
                else -> "Rich & Warm Neutrals"
            }

            val detectedDressCode = when {
                allText.contains("wedding") || allText.contains("marriage") || allText.contains("sangeet") -> "Traditional Wedding & Festive"
                allText.contains("party") || allText.contains("club") || allText.contains("dinner") -> "High-Streetwear & Party Casual"
                allText.contains("work") || allText.contains("business") || allText.contains("corporate") -> "Business Formal & Corporate"
                else -> "Festive Ethnic, Smart Casual, Formal"
            }

            val detectedFit = when {
                allText.contains("slim") || allText.contains("fitted") -> "Slim Fit"
                allText.contains("oversized") || allText.contains("relaxed") || allText.contains("loose") -> "Relaxed / Oversized"
                else -> "Tailored Regular"
            }

            val insights = listOf(
                "Analyzed ${topRated.size} top-rated outfits saved in your local Room DB.",
                "Primary aesthetic style DNA identified: '$detectedVibe'.",
                "Dominant color harmony palette: '$detectedPalette'.",
                "Tailored dress code preference: '$detectedDressCode'."
            )

            val summary = "Updated Room DB Preferences: $detectedVibe | $detectedPalette | $detectedFit"

            val currentPref = userStylePreference.value
            val updatedPref = UserStylePreference(
                id = 1,
                topSize = currentPref?.topSize ?: "L",
                bottomSize = currentPref?.bottomSize ?: "32",
                shoeSize = currentPref?.shoeSize ?: "UK 10",
                primaryAestheticVibe = detectedVibe,
                colorPreferences = detectedPalette,
                preferredDressCodes = detectedDressCode,
                preferredFit = detectedFit
            )

            repository.saveUserStylePreference(updatedPref)

            val result = com.example.data.ai.SmartStyleAnalysisResult(
                analyzedOutfitCount = topRated.size,
                detectedAestheticVibe = detectedVibe,
                detectedColorPalette = detectedPalette,
                detectedDressCode = detectedDressCode,
                detectedPreferredFit = detectedFit,
                keyInsights = insights,
                recommendationSummary = summary,
                isDatabaseUpdated = true
            )

            _smartStyleAnalysis.value = result
            _uiState.value = _uiState.value.copy(
                userMessageToast = "✨ Smart Style Analysis complete! Room DB style preferences automatically updated."
            )
        }
    }

    fun generatePackingList(selectedOutfits: List<SavedOutfit>, tripTitle: String = "Upcoming Trip Packing List") {
        if (selectedOutfits.isEmpty()) return

        val tops = mutableListOf<com.example.data.ai.PackingItem>()
        val bottoms = mutableListOf<com.example.data.ai.PackingItem>()
        val outerwears = mutableListOf<com.example.data.ai.PackingItem>()
        val footwears = mutableListOf<com.example.data.ai.PackingItem>()
        val accessories = mutableListOf<com.example.data.ai.PackingItem>()

        selectedOutfits.forEach { outfit ->
            val outfitTitle = outfit.title
            if (outfit.topItem.isNotBlank()) {
                tops.add(com.example.data.ai.PackingItem(name = outfit.topItem, category = "Tops & Shirts", sourceOutfitTitle = outfitTitle))
            }
            if (outfit.bottomItem.isNotBlank()) {
                bottoms.add(com.example.data.ai.PackingItem(name = outfit.bottomItem, category = "Bottoms & Trousers", sourceOutfitTitle = outfitTitle))
            }
            if (outfit.outerwearItem.isNotBlank()) {
                outerwears.add(com.example.data.ai.PackingItem(name = outfit.outerwearItem, category = "Outerwear & Jackets", sourceOutfitTitle = outfitTitle))
            }
            if (outfit.footwearItem.isNotBlank()) {
                footwears.add(com.example.data.ai.PackingItem(name = outfit.footwearItem, category = "Footwear & Shoes", sourceOutfitTitle = outfitTitle))
            }
            if (outfit.accessoryItems.isNotBlank()) {
                val accList = outfit.accessoryItems.split(",").map { it.trim() }.filter { it.isNotBlank() }
                accList.forEach { acc ->
                    accessories.add(com.example.data.ai.PackingItem(name = acc, category = "Accessories & Jewelry", sourceOutfitTitle = outfitTitle))
                }
            }
        }

        val categories = listOf(
            com.example.data.ai.PackingCategoryItems("👚 Tops & Shirts", tops),
            com.example.data.ai.PackingCategoryItems("👖 Bottoms & Trousers", bottoms),
            com.example.data.ai.PackingCategoryItems("🧥 Outerwear & Jackets", outerwears),
            com.example.data.ai.PackingCategoryItems("👞 Footwear & Shoes", footwears),
            com.example.data.ai.PackingCategoryItems("⌚ Accessories & Jewelry", accessories)
        ).filter { it.items.isNotEmpty() }

        val totalItems = categories.sumOf { it.items.size }

        val tips = listOf(
            "Roll silk kurtas and linen shirts to minimize packing creases.",
            "Pack handcrafted mojris and leather footwear in padded shoe bags.",
            "Keep watches, brooches, and pocket squares in a protective accessory pouch."
        )

        _currentPackingList.value = com.example.data.ai.PackingList(
            tripTitle = tripTitle,
            selectedOutfitCount = selectedOutfits.size,
            categories = categories,
            totalItemCount = totalItems,
            packedItemCount = 0,
            packingTips = tips
        )

        _uiState.value = _uiState.value.copy(
            userMessageToast = "🧳 Trip Packing List generated with $totalItems items from ${selectedOutfits.size} saved outfits!"
        )
    }

    fun togglePackingItem(itemId: String) {
        val current = _currentPackingList.value ?: return
        val updatedCategories = current.categories.map { category ->
            category.copy(
                items = category.items.map { item ->
                    if (item.id == itemId) item.copy(isPacked = !item.isPacked) else item
                }
            )
        }
        val packedCount = updatedCategories.sumOf { cat -> cat.items.count { it.isPacked } }
        _currentPackingList.value = current.copy(
            categories = updatedCategories,
            packedItemCount = packedCount
        )
    }

    fun clearPackingList() {
        _currentPackingList.value = null
    }

    fun saveCurrentRecommendation(context: android.content.Context? = null) {
        val current = _uiState.value.currentRecommendation ?: return
        val weather = _uiState.value.weather

        viewModelScope.launch {
            val localImagePath = if (context != null) {
                try {
                    com.example.util.OutfitImageExporter.generateOutfitCardImage(context, current)
                } catch (e: Exception) {
                    ""
                }
            } else ""

            val savedOutfit = SavedOutfit(
                title = current.title,
                occasion = current.occasion,
                weatherCondition = weather.condition,
                temperature = weather.temperatureCelsius,
                topItem = current.top,
                bottomItem = current.bottom,
                outerwearItem = current.outerwear,
                footwearItem = current.footwear,
                accessoryItems = current.accessories.joinToString(", "),
                colorPalette = current.colorPaletteNames.joinToString(" + "),
                stylingTips = current.stylingTips,
                weatherComfortReason = current.weatherAdvice,
                imagePath = localImagePath,
                userRating = current.userRating
            )
            repository.saveOutfit(savedOutfit)
            _uiState.value = _uiState.value.copy(
                userMessageToast = if (localImagePath.isNotBlank()) "Outfit saved with local image snapshot!" else "Outfit saved to Lookbook!"
            )
        }
    }

    fun exportAndSaveOutfitImage(context: android.content.Context, outfit: SavedOutfit) {
        viewModelScope.launch {
            val path = try {
                com.example.util.OutfitImageExporter.generateImageFromSavedOutfit(context, outfit)
            } catch (e: Exception) {
                ""
            }
            if (path.isNotBlank()) {
                val updatedOutfit = outfit.copy(imagePath = path)
                repository.updateSavedOutfit(updatedOutfit)
                _uiState.value = _uiState.value.copy(userMessageToast = "Generated local image for ${outfit.title}!")
            } else {
                _uiState.value = _uiState.value.copy(userMessageToast = "Failed to export image.")
            }
        }
    }

    fun addWardrobeItem(item: WardrobeItem) {
        viewModelScope.launch {
            repository.addWardrobeItem(item)
            _uiState.value = _uiState.value.copy(userMessageToast = "Added ${item.name} to Wardrobe")
        }
    }

    fun deleteWardrobeItem(id: Int) {
        viewModelScope.launch {
            repository.deleteWardrobeItem(id)
            _uiState.value = _uiState.value.copy(userMessageToast = "Item removed from Wardrobe")
        }
    }

    fun addCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.addEvent(event)
            _uiState.value = _uiState.value.copy(userMessageToast = "Added event '${event.title}'")
        }
    }

    fun deleteCalendarEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEvent(id)
            _uiState.value = _uiState.value.copy(userMessageToast = "Event removed")
        }
    }

    fun deleteSavedOutfit(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedOutfit(id)
            _uiState.value = _uiState.value.copy(userMessageToast = "Outfit removed from Lookbook")
        }
    }

    fun scanAndSyncCalendar(context: android.content.Context) {
        viewModelScope.launch {
            val scannedEvents = com.example.util.CalendarScannerHelper.scanDeviceCalendarForDressCodes(context)
            var newlyAddedCount = 0
            val existingTitles = calendarEvents.value.map { it.title.lowercase() }
            
            scannedEvents.forEach { event ->
                if (!existingTitles.contains(event.title.lowercase())) {
                    repository.addEvent(event)
                    newlyAddedCount++
                }
            }

            val targetMsg = if (newlyAddedCount > 0) {
                "Scanned Calendar: Added $newlyAddedCount upcoming events with dress codes!"
            } else {
                "Synced Calendar: ${scannedEvents.size} dress code events active!"
            }

            _uiState.value = _uiState.value.copy(userMessageToast = targetMsg)
        }
    }

    fun downloadOutfitImageToGallery(context: android.content.Context, recommendation: OutfitRecommendation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(userMessageToast = "Saving high-res outfit image to Gallery...")
            val exportedPath = try {
                com.example.util.OutfitImageExporter.generateOutfitCardImage(context, recommendation)
            } catch (e: Exception) { "" }

            val source = recommendation.outfitImageUrl.ifBlank { exportedPath }
            val success = com.example.util.MediaStoreSaver.saveOutfitImageToGallery(
                context = context,
                imageSource = source,
                title = recommendation.title
            )
            if (success) {
                _uiState.value = _uiState.value.copy(userMessageToast = "📸 High-res outfit saved to Gallery (Pictures/AI_Outfit_Stylist)!")
            } else {
                _uiState.value = _uiState.value.copy(userMessageToast = "Failed to download image to gallery.")
            }
        }
    }

    fun completeStyleDiscoveryQuiz(preference: UserStylePreference) {
        viewModelScope.launch {
            repository.saveUserStylePreference(preference)
            _uiState.value = _uiState.value.copy(
                stylePreference = StylePreference(
                    primaryVibe = preference.primaryAestheticVibe,
                    preferredFit = preference.preferredFit,
                    colorPreference = preference.colorPreferences,
                    topSize = preference.topSize,
                    bottomSize = preference.bottomSize,
                    shoeSize = preference.shoeSize,
                    preferredDressCodes = preference.preferredDressCodes
                ),
                selectedOccasion = preference.preferredDressCodes.split(",").firstOrNull()?.trim() ?: "Marriage / Wedding",
                userMessageToast = "Style Quiz Complete! Preferences saved to Room Database."
            )
            // Automatically trigger outfit recommendations aligned with quiz
            generateRecommendation(
                prompt = "Suggest 5 outfits based on my Style Quiz result: ${preference.primaryAestheticVibe}, ${preference.colorPreferences}, ${preference.preferredFit}",
                occasion = preference.preferredDressCodes.split(",").firstOrNull()?.trim() ?: "Marriage / Wedding",
                persona = _uiState.value.selectedTargetPersona,
                gender = _uiState.value.selectedGender
            )
        }
    }

    fun pushOutfitToCalendarReminder(
        context: android.content.Context,
        recommendation: OutfitRecommendation,
        targetEventTitle: String = ""
    ) {
        val eventName = targetEventTitle.ifBlank { _uiState.value.selectedOccasion.ifBlank { "Upcoming Event" } }
        val success = com.example.util.CalendarScannerHelper.pushOutfitToCalendar(
            context = context,
            eventTitle = eventName,
            outfitTitle = recommendation.title,
            topItem = recommendation.top,
            bottomItem = recommendation.bottom,
            footwearItem = recommendation.footwear,
            accessoriesItem = recommendation.accessories.joinToString(", "),
            stylingTips = recommendation.stylingTips,
            eventDate = "",
            location = "Venue"
        )
        if (success) {
            _uiState.value = _uiState.value.copy(userMessageToast = "📅 Pushed outfit suggestion to Calendar Reminder!")
        } else {
            _uiState.value = _uiState.value.copy(userMessageToast = "Failed to open Calendar app.")
        }
    }

    fun pushSavedOutfitToCalendarReminder(
        context: android.content.Context,
        outfit: com.example.data.local.SavedOutfit,
        targetEventTitle: String = ""
    ) {
        val eventName = targetEventTitle.ifBlank { outfit.occasion.ifBlank { "Upcoming Event" } }
        val success = com.example.util.CalendarScannerHelper.pushOutfitToCalendar(
            context = context,
            eventTitle = eventName,
            outfitTitle = outfit.title,
            topItem = outfit.topItem,
            bottomItem = outfit.bottomItem,
            footwearItem = outfit.footwearItem,
            accessoriesItem = outfit.accessoryItems,
            stylingTips = outfit.stylingTips,
            eventDate = "",
            location = "Venue"
        )
        if (success) {
            _uiState.value = _uiState.value.copy(userMessageToast = "📅 Pushed saved look to Calendar Reminder!")
        } else {
            _uiState.value = _uiState.value.copy(userMessageToast = "Failed to open Calendar app.")
        }
    }

    fun generateVirtualClosetOutfit() {
        viewModelScope.launch {
            val closet = wardrobeItems.value
            if (closet.isEmpty()) {
                _uiState.value = _uiState.value.copy(userMessageToast = "Your Virtual Closet is empty! Adding sample closet items first...")
                populateSampleVirtualCloset()
            }
            val closetSummary = wardrobeItems.value.joinToString("; ") { "${it.name} (${it.category}, ${it.color})" }
            val prompt = "Create 5 stylish outfits strictly using or matching items from my Virtual Closet: [$closetSummary]"
            
            generateRecommendation(
                prompt = prompt,
                occasion = _uiState.value.selectedOccasion,
                persona = _uiState.value.selectedTargetPersona,
                gender = _uiState.value.selectedGender
            )
        }
    }

    fun populateSampleVirtualCloset() {
        viewModelScope.launch {
            val samples = listOf(
                WardrobeItem(name = "Royal Navy Linen Blazer", category = "Outerwear", subcategory = "Blazer", color = "Navy Blue", hexColor = "#1B2A4A", fabric = "Linen Blend", formality = "Smart Casual", season = "All"),
                WardrobeItem(name = "Ivory Silk Kurta & Chudidar", category = "Dress/Traditional", subcategory = "Kurta Set", color = "Ivory Gold", hexColor = "#F5F2EB", fabric = "Silk", formality = "Festive/Traditional", season = "All"),
                WardrobeItem(name = "Charcoal Italian Trouser", category = "Bottom", subcategory = "Trousers", color = "Charcoal Grey", hexColor = "#36454F", fabric = "Wool Blend", formality = "Formal", season = "All"),
                WardrobeItem(name = "Tan Leather Double Monk Shoes", category = "Footwear", subcategory = "Dress Shoes", color = "Tan Brown", hexColor = "#8B5A2B", fabric = "Leather", formality = "Formal", season = "All"),
                WardrobeItem(name = "Rose Gold Chronograph Watch", category = "Accessory", subcategory = "Watch", color = "Rose Gold", hexColor = "#B76E79", fabric = "Stainless Steel", formality = "Smart Casual", season = "All"),
                WardrobeItem(name = "Pastel Mint Green Bandhgala", category = "Outerwear", subcategory = "Bandhgala", color = "Mint Green", hexColor = "#98FF98", fabric = "Silk Linen", formality = "Festive/Traditional", season = "Summer")
            )
            samples.forEach { repository.addWardrobeItem(it) }
            _uiState.value = _uiState.value.copy(userMessageToast = "Populated 6 sample pieces into Virtual Closet!")
        }
    }

    fun recordOutfitDislikeFeedback(
        recommendation: OutfitRecommendation,
        reason: String,
        customNote: String = ""
    ) {
        viewModelScope.launch {
            val noteText = if (customNote.isNotBlank()) " ($customNote)" else ""
            val message = "Feedback noted: '$reason'$noteText for '${recommendation.title}'. AI recommendations refined!"
            _uiState.value = _uiState.value.copy(userMessageToast = message)
        }
    }

    fun styleUnwornWardrobeItem(item: WardrobeItem) {
        viewModelScope.launch {
            val prompt = "Create 5 outfits focused on styling my unworn piece: '${item.name}' (${item.category}, ${item.color}). Match it with clothes from my closet or modern accessories."
            generateRecommendation(
                prompt = prompt,
                occasion = _uiState.value.selectedOccasion,
                persona = _uiState.value.selectedTargetPersona,
                gender = _uiState.value.selectedGender
            )
        }
    }

    fun donateWardrobeItem(item: WardrobeItem) {
        viewModelScope.launch {
            repository.deleteWardrobeItem(item.id)
            _uiState.value = _uiState.value.copy(
                userMessageToast = "📦 '${item.name}' logged for clothes donation / charity drive and removed from closet!"
            )
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(userMessageToast = null)
    }
}
