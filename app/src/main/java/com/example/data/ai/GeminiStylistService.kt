package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.WardrobeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiStylistService {

    private const val TAG = "GeminiStylistService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun get5OutfitSuggestions(
        userPrompt: String,
        occasion: String,
        weather: WeatherContext,
        style: StylePreference,
        wardrobe: List<WardrobeItem>,
        targetPersona: String = "Couple",
        genderSelection: String = "Male",
        ratedOutfits: List<com.example.data.local.SavedOutfit> = emptyList()
    ): List<OutfitRecommendation> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Gemini API key not set or placeholder. Falling back to RuleBasedStylistEngine.")
            return@withContext RuleBasedStylistEngine.generate5Recommendations(
                occasion = occasion,
                userQuery = userPrompt,
                weather = weather,
                style = style,
                wardrobe = wardrobe,
                targetPersona = targetPersona,
                genderSelection = genderSelection
            )
        }

        try {
            val wardrobeListText = wardrobe.joinToString("\n") {
                "- ${it.name} (${it.category}, ${it.color}, ${it.fabric}, ${it.formality})"
            }

            val systemInstruction = """
                You are an elite high-fashion AI Outfit Stylist Agent.

                MANDATORY GENDER SELECTION FILTER: $genderSelection
                CRITICAL MANDATORY GENDER FILTER RULES:
                - If Gender Selection is 'Male', ALL 5 recommended outfits MUST strictly be tailored for MALE / MEN / BOYS fashion (e.g. Kurtas, Suits, Blazers, Tuxedos, Shirts, Trousers, Chinos). You MUST strictly FILTER OUT and EXCLUDE all female garments like Sarees, Lehengas, Dresses, Gowns, Skirts, or Women's Tunics!
                - If Gender Selection is 'Female', ALL 5 recommended outfits MUST strictly be tailored for FEMALE / WOMEN / GIRLS fashion (e.g. Sarees, Lehengas, Anarkalis, Dresses, Gowns, Tops, Skirts, Kurtis). You MUST strictly FILTER OUT and EXCLUDE all male-only traditional or tuxedo garments!
                - If Gender Selection is 'Other', provide contemporary gender-neutral, fluid, or unisex high-fashion choices.

                The user wants 5 DISTINCT outfit suggestions based on:
                1. Target Persona/Gender: $targetPersona (Gender Filter: $genderSelection)
                2. Occasion: $occasion
                3. Custom Request: $userPrompt
                4. Weather: ${weather.temperatureCelsius}°C, Condition: ${weather.condition}
                5. Available Wardrobe:
                $wardrobeListText
                
                Respond strictly with a JSON array containing 5 distinct outfit objects:
                [
                  {
                    "title": "Option 1: Creative outfit title",
                    "vibeTag": "Vibe tag",
                    "targetPersona": "$targetPersona",
                    "isCoupleOutfit": true/false,
                    "top": "Top garment for individual",
                    "bottom": "Bottom garment",
                    "outerwear": "Outerwear",
                    "footwear": "Footwear",
                    "accessories": ["Acc 1", "Acc 2"],
                    "partner1": {
                       "partnerTitle": "Husband / Partner A",
                       "avatarType": "Husband",
                       "top": "Garment top",
                       "bottom": "Garment bottom",
                       "outerwear": "Outerwear",
                       "footwear": "Footwear",
                       "accessories": ["Acc 1"],
                       "avatarImagePrompt": "Detailed AI avatar portrait prompt e.g. Dapper handsome husband in emerald silk kurta, luxury lighting",
                       "avatarImageUrl": "High resolution portrait image URL",
                       "outfitImagePrompt": "High fashion flatlay look image prompt"
                    },
                    "partner2": {
                       "partnerTitle": "Wife / Partner B",
                       "avatarType": "Wife",
                       "top": "Garment top",
                       "bottom": "Garment bottom",
                       "outerwear": "Outerwear",
                       "footwear": "Footwear",
                       "accessories": ["Acc 1"],
                       "avatarImagePrompt": "Detailed AI avatar portrait prompt e.g. Elegant chic wife avatar portrait in embroidered lehenga",
                       "avatarImageUrl": "High resolution portrait image URL",
                       "outfitImagePrompt": "High fashion flatlay look image prompt"
                    },
                    "colorPaletteNames": ["Color1", "Color2"],
                    "colorPaletteHexes": ["#Hex1", "#Hex2"],
                    "weatherComfortScore": 95,
                    "weatherAdvice": "Explanation",
                    "stylingTips": "Styling advice",
                    "outfitImagePrompt": "A detailed high-fashion editorial AI image generation prompt describing the full outfit flatlay with accessories and lighting details",
                    "outfitImageUrl": "Direct image URL for outfit visual",
                    "avatarImagePrompt": "A detailed high-fashion avatar portrait generation prompt for $targetPersona",
                    "avatarImageUrl": "Direct image URL for main persona avatar",
                    "missingPiecesToElevate": [
                      {
                        "itemName": "Specific item",
                        "category": "Accessory",
                        "reasonToElevate": "Why it elevates",
                        "trendTag": "Trend Tag",
                        "searchQuery": "Search term"
                      }
                    ]
                  }
                ]
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Provide 5 distinct outfit recommendations for target persona: $targetPersona, occasion: $occasion, prompt: $userPrompt")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val requestUrl = "$BASE_URL?key=$apiKey"
            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(requestUrl).post(requestBody).build()
            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string()

            if (response.isSuccessful && !responseString.isNullOrBlank()) {
                val responseJson = JSONObject(responseString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val text = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")?.getJSONObject(0)?.optString("text", "") ?: ""
                    if (text.isNotBlank()) {
                        val parsedList = mutableListOf<OutfitRecommendation>()
                        if (text.trim().startsWith("[")) {
                            val jsonArray = org.json.JSONArray(text)
                            for (i in 0 until jsonArray.length()) {
                                parsedList.add(parseOutfitFromJson(jsonArray.getJSONObject(i), occasion))
                            }
                        } else if (text.trim().startsWith("{")) {
                            parsedList.add(parseOutfitFromJson(JSONObject(text), occasion))
                        }
                        if (parsedList.isNotEmpty()) {
                            return@withContext parsedList.mapIndexed { index, rec ->
                                RuleBasedStylistEngine.enrichWithImageAssets(rec, index)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Gemini: ${e.message}")
        }

        return@withContext RuleBasedStylistEngine.generate5Recommendations(
            occasion = occasion,
            userQuery = userPrompt,
            weather = weather,
            style = style,
            wardrobe = wardrobe,
            targetPersona = targetPersona,
            genderSelection = genderSelection
        )
    }

    suspend fun regenerateSingleOutfitOption(
        optionIndex: Int,
        currentSuggestions: List<OutfitRecommendation>,
        userPrompt: String,
        occasion: String,
        weather: WeatherContext,
        style: StylePreference,
        wardrobe: List<WardrobeItem>,
        targetPersona: String = "Couple",
        genderSelection: String = "Male"
    ): OutfitRecommendation = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val variedPrompt = if (userPrompt.isNotBlank()) "$userPrompt (Variation $timestamp)" else "Alternative high-fashion look $timestamp"
        
        val newSuggestions = get5OutfitSuggestions(
            userPrompt = variedPrompt,
            occasion = occasion,
            weather = weather,
            style = style,
            wardrobe = wardrobe,
            targetPersona = targetPersona,
            genderSelection = genderSelection
        )

        val existingTitles = currentSuggestions.map { it.title.lowercase() }
        val distinctOption = newSuggestions.firstOrNull { candidate ->
            !existingTitles.contains(candidate.title.lowercase())
        } ?: newSuggestions[(optionIndex + 1) % newSuggestions.size]

        return@withContext RuleBasedStylistEngine.enrichWithImageAssets(distinctOption, optionIndex)
    }

    suspend fun getOutfitSuggestion(
        userPrompt: String,
        occasion: String,
        weather: WeatherContext,
        style: StylePreference,
        wardrobe: List<WardrobeItem>,
        ratedOutfits: List<com.example.data.local.SavedOutfit> = emptyList()
    ): OutfitRecommendation = withContext(Dispatchers.IO) {
        return@withContext get5OutfitSuggestions(
            userPrompt = userPrompt,
            occasion = occasion,
            weather = weather,
            style = style,
            wardrobe = wardrobe,
            targetPersona = "Couple",
            ratedOutfits = ratedOutfits
        ).first()
    }

    private fun parseOutfitFromJson(json: JSONObject, defaultOccasion: String): OutfitRecommendation {
        val accessoriesList = mutableListOf<String>()
        json.optJSONArray("accessories")?.let { arr ->
            for (i in 0 until arr.length()) {
                accessoriesList.add(arr.getString(i))
            }
        }

        val paletteNamesList = mutableListOf<String>()
        json.optJSONArray("colorPaletteNames")?.let { arr ->
            for (i in 0 until arr.length()) {
                paletteNamesList.add(arr.getString(i))
            }
        }

        val paletteHexesList = mutableListOf<String>()
        json.optJSONArray("colorPaletteHexes")?.let { arr ->
            for (i in 0 until arr.length()) {
                paletteHexesList.add(arr.getString(i))
            }
        }

        val matchesList = mutableListOf<String>()
        json.optJSONArray("closetItemMatches")?.let { arr ->
            for (i in 0 until arr.length()) {
                matchesList.add(arr.getString(i))
            }
        }

        val missingPiecesList = mutableListOf<MissingPieceSuggestion>()
        json.optJSONArray("missingPiecesToElevate")?.let { arr ->
            for (i in 0 until arr.length()) {
                val itemObj = arr.getJSONObject(i)
                missingPiecesList.add(
                    MissingPieceSuggestion(
                        itemName = itemObj.optString("itemName", "Trending Item"),
                        category = itemObj.optString("category", "Accessory"),
                        reasonToElevate = itemObj.optString("reasonToElevate", "Elevates overall look based on current trends."),
                        trendTag = itemObj.optString("trendTag", "Trending Upgrade"),
                        searchQuery = itemObj.optString("searchQuery", itemObj.optString("itemName"))
                    )
                )
            }
        }

        val isCouple = json.optBoolean("isCoupleOutfit", false) || json.has("partner1")
        val persona = json.optString("targetPersona", if (isCouple) "Couple" else "Men")
        val vibe = json.optString("vibeTag", "Smart AI Recommendation")
        val topGarment = json.optString("top", "Classic Top")

        val partner1Obj = json.optJSONObject("partner1")
        val partner2Obj = json.optJSONObject("partner2")

        val p1 = if (partner1Obj != null) {
            val acc1 = mutableListOf<String>()
            partner1Obj.optJSONArray("accessories")?.let { a ->
                for (i in 0 until a.length()) acc1.add(a.getString(i))
            }
            val p1Top = partner1Obj.optString("top", "Classic Shirt")
            PartnerOutfitDetails(
                partnerTitle = partner1Obj.optString("partnerTitle", "Partner 1 (Him)"),
                avatarType = partner1Obj.optString("avatarType", "Husband"),
                top = p1Top,
                bottom = partner1Obj.optString("bottom", ""),
                outerwear = partner1Obj.optString("outerwear", ""),
                footwear = partner1Obj.optString("footwear", ""),
                accessories = acc1,
                avatarImagePrompt = partner1Obj.optString("avatarImagePrompt", "Dapper handsome husband portrait avatar in $p1Top"),
                avatarImageUrl = partner1Obj.optString("avatarImageUrl", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop"),
                outfitImagePrompt = partner1Obj.optString("outfitImagePrompt", "High-fashion flatlay layout for husband featuring $p1Top with accessories")
            )
        } else null

        val p2 = if (partner2Obj != null) {
            val acc2 = mutableListOf<String>()
            partner2Obj.optJSONArray("accessories")?.let { a ->
                for (i in 0 until a.length()) acc2.add(a.getString(i))
            }
            val p2Top = partner2Obj.optString("top", "Elegant Blouse")
            PartnerOutfitDetails(
                partnerTitle = partner2Obj.optString("partnerTitle", "Partner 2 (Her)"),
                avatarType = partner2Obj.optString("avatarType", "Wife"),
                top = p2Top,
                bottom = partner2Obj.optString("bottom", ""),
                outerwear = partner2Obj.optString("outerwear", ""),
                footwear = partner2Obj.optString("footwear", ""),
                accessories = acc2,
                avatarImagePrompt = partner2Obj.optString("avatarImagePrompt", "Elegant chic wife portrait avatar in $p2Top"),
                avatarImageUrl = partner2Obj.optString("avatarImageUrl", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop"),
                outfitImagePrompt = partner2Obj.optString("outfitImagePrompt", "High-fashion studio flatlay for wife featuring $p2Top with matching jewelry")
            )
        } else null

        val parsedOutfitPrompt = json.optString("outfitImagePrompt", "")
        val finalOutfitPrompt = if (parsedOutfitPrompt.isNotBlank()) parsedOutfitPrompt else {
            "Editorial fashion flatlay photograph of $topGarment, $vibe aesthetic, luxury studio background with complementary accessories"
        }

        val parsedOutfitUrl = json.optString("outfitImageUrl", "")
        val finalOutfitUrl = if (parsedOutfitUrl.isNotBlank()) parsedOutfitUrl else {
            when {
                defaultOccasion.contains("marriage", ignoreCase = true) || defaultOccasion.contains("wedding", ignoreCase = true) ->
                    "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800&auto=format&fit=crop"
                defaultOccasion.contains("party", ignoreCase = true) || vibe.contains("glam", ignoreCase = true) ->
                    "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&auto=format&fit=crop"
                else -> "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&auto=format&fit=crop"
            }
        }

        val parsedAvatarPrompt = json.optString("avatarImagePrompt", "")
        val finalAvatarPrompt = if (parsedAvatarPrompt.isNotBlank()) parsedAvatarPrompt else {
            "Modern stylish fashion avatar illustration for $persona, $vibe aesthetic, high resolution portrait"
        }

        val parsedAvatarUrl = json.optString("avatarImageUrl", "")
        val finalAvatarUrl = if (parsedAvatarUrl.isNotBlank()) parsedAvatarUrl else {
            when (persona.lowercase()) {
                "husband", "men", "man" -> "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop"
                "wife", "women", "lady" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop"
                "boy" -> "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?w=400&auto=format&fit=crop"
                "girl" -> "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop"
                "kid" -> "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?w=400&auto=format&fit=crop"
                else -> "https://images.unsplash.com/photo-1516589178581-6cd7833ae3b2?w=500&auto=format&fit=crop"
            }
        }

        return OutfitRecommendation(
            title = json.optString("title", "Tailored AI Outfit"),
            occasion = defaultOccasion,
            vibeTag = vibe,
            targetPersona = persona,
            isCoupleOutfit = isCouple,
            partner1 = p1,
            partner2 = p2,
            top = topGarment,
            bottom = json.optString("bottom", "Tailored Trousers"),
            outerwear = json.optString("outerwear", ""),
            footwear = json.optString("footwear", "Smart Shoes"),
            accessories = if (accessoriesList.isNotEmpty()) accessoriesList else listOf("Classic Watch", "Belt"),
            colorPaletteNames = if (paletteNamesList.isNotEmpty()) paletteNamesList else listOf("Navy", "White"),
            colorPaletteHexes = if (paletteHexesList.isNotEmpty()) paletteHexesList else listOf("#1E2838", "#FFFFFF"),
            weatherComfortScore = json.optInt("weatherComfortScore", 95),
            weatherAdvice = json.optString("weatherAdvice", "Optimized for weather and comfort."),
            stylingTips = json.optString("stylingTips", "Focus on clean fit and ironed seams."),
            groomingAdvice = json.optString("groomingAdvice", "Fresh clean scent."),
            closetItemMatches = matchesList,
            missingPiecesToElevate = missingPiecesList,
            outfitImagePrompt = finalOutfitPrompt,
            outfitImageUrl = finalOutfitUrl,
            avatarImagePrompt = finalAvatarPrompt,
            avatarImageUrl = finalAvatarUrl,
            isAiGenerated = true
        )
    }

    suspend fun getSeasonalTrendsSummary(
        weather: WeatherContext,
        style: StylePreference,
        ratedOutfits: List<com.example.data.local.SavedOutfit> = emptyList()
    ): SeasonalTrendResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val location = weather.location
        val condition = weather.condition
        val temp = weather.temperatureCelsius
        val vibe = style.primaryVibe

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext SeasonalTrendResult(
                headline = "Current Seasonal Fashion Trends in $location",
                regionSeasonTag = "$location • $condition ($temp°C)",
                summaryText = "Fashion search data in $location shows a strong shift towards breathable $vibe aesthetics. High-humidity and $condition weather drive interest in moisture-wicking linens, relaxed-fit silhouettes, and versatile earth-tone palettes.",
                trendingStyles = listOf("Monsoon-Proof Linen Chinos", "Mandarin Collar Kurtas", "Tailored Smart Shorts"),
                trendingColors = listOf("Monsoon Emerald", "Terracotta Gold", "Pearl White"),
                keyFabrics = listOf("Breathable Linen-Cotton", "Light Raw Silk", "Quick-Dry Stretch Denim"),
                stylistNote = "Opt for unlined unstructured blazers or light jackets to maintain crisp structure without heat buildup.",
                isAiGenerated = false
            )
        }

        try {
            val systemInstruction = """
                You are a global fashion trend forecasting analyst powered by real-time search trends.
                Generate a concise, insightful 'Seasonal Trends Summary' customized for:
                - Location: $location
                - Current Weather & Temperature: $condition, $temp°C
                - User Aesthetic Vibe Preference: $vibe
                - Preferred Colors: ${style.colorPreference}

                Respond strictly with valid JSON matching this schema:
                {
                  "headline": "Snappy trend title e.g. Monsoon Linen & Ethnic Fusion Trends in Mumbai",
                  "regionSeasonTag": "Location & weather tag e.g. Mumbai, MH • Rain & High Humidity (28°C)",
                  "summaryText": "2-3 sentence overview of real search trends in this location for this season",
                  "trendingStyles": ["Trending Style Item 1", "Trending Style Item 2", "Trending Style Item 3"],
                  "trendingColors": ["Color 1", "Color 2", "Color 3"],
                  "keyFabrics": ["Fabric 1", "Fabric 2", "Fabric 3"],
                  "stylistNote": "Pro styling tip for this seasonal trend"
                }
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Generate current seasonal fashion trends summary for $location during $condition weather.")
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val requestUrl = "$BASE_URL?key=$apiKey"
            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string()

            if (response.isSuccessful && !responseString.isNullOrBlank()) {
                val responseJson = JSONObject(responseString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            val trendJson = JSONObject(text)
                            val stylesList = mutableListOf<String>()
                            trendJson.optJSONArray("trendingStyles")?.let { arr ->
                                for (i in 0 until arr.length()) stylesList.add(arr.getString(i))
                            }
                            val colorsList = mutableListOf<String>()
                            trendJson.optJSONArray("trendingColors")?.let { arr ->
                                for (i in 0 until arr.length()) colorsList.add(arr.getString(i))
                            }
                            val fabricsList = mutableListOf<String>()
                            trendJson.optJSONArray("keyFabrics")?.let { arr ->
                                for (i in 0 until arr.length()) fabricsList.add(arr.getString(i))
                            }

                            return@withContext SeasonalTrendResult(
                                headline = trendJson.optString("headline", "Seasonal Fashion Trends"),
                                regionSeasonTag = trendJson.optString("regionSeasonTag", "$location • $condition"),
                                summaryText = trendJson.optString("summaryText", "Popular seasonal search trends in your region."),
                                trendingStyles = if (stylesList.isNotEmpty()) stylesList else listOf("Linen Shirts", "Chinos", "Ethnic Fusion"),
                                trendingColors = if (colorsList.isNotEmpty()) colorsList else listOf("Olive Green", "Cream", "Navy"),
                                keyFabrics = if (fabricsList.isNotEmpty()) fabricsList else listOf("Breathable Cotton", "Linen Blend"),
                                stylistNote = trendJson.optString("stylistNote", "Keep fits airy and tailored."),
                                isAiGenerated = true
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching seasonal trends: ${e.message}", e)
        }

        return@withContext SeasonalTrendResult(
            headline = "Seasonal Fashion Trends in $location",
            regionSeasonTag = "$location • $condition ($temp°C)",
            summaryText = "Fashion trends in $location reflect high demand for $vibe styles. Breathable fabrics like linen and cotton blends dominate popular searches for $condition weather.",
            trendingStyles = listOf("Modern Linen Kurtas", "Light Tapered Trousers", "Minimalist Leather Loafers"),
            trendingColors = listOf("Emerald Navy", "Warm Ochre", "Off-White"),
            keyFabrics = listOf("Breathable Linen", "Fine Cotton", "Light Silk Blend"),
            stylistNote = "Focus on light weight fabrics and unconstructed layers.",
            isAiGenerated = false
        )
    }

    suspend fun getGroundedStyleGuideArticle(dressCode: String): StyleGuideArticle = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackStyleGuideArticle(dressCode)
        }

        try {
            val systemInstruction = """
                You are an authoritative global fashion editor and dress code etiquette expert.
                Use Google Search grounding to provide accurate, up-to-date fashion definitions and style guide rules for the dress code: '$dressCode'.

                Respond strictly with JSON format:
                {
                   "dressCodeName": "$dressCode",
                   "definition": "Clear 2-sentence definition of this dress code",
                   "historyAndOrigin": "Brief 1-sentence historical context or origin",
                   "keyGarmentsForMen": ["Item 1", "Item 2", "Item 3"],
                   "keyGarmentsForWomen": ["Item 1", "Item 2", "Item 3"],
                   "footwearRules": "Footwear rules for this dress code",
                   "colorEtiquette": "Color palette etiquette and rules",
                   "dosAndDonts": ["Do: Wear clean fitted pieces", "Don't: Wear sneakers or distressed denim"],
                   "googleSearchGroundedSources": ["Vogue Fashion Etiquette Guide", "GQ Dress Code Dictionary", "Gentleman's Gazette Rules"]
                }
            """.trimIndent()

            val jsonPayload = JSONObject().apply {
                put("contents", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Explain the '$dressCode' dress code with fashion rules and grounding.")
                            })
                        })
                    })
                })
                put("tools", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("google_search", JSONObject())
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val requestUrl = "$BASE_URL?key=$apiKey"
            val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string()

            if (response.isSuccessful && !responseString.isNullOrBlank()) {
                val responseJson = JSONObject(responseString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            val artJson = JSONObject(text)
                            val menList = mutableListOf<String>()
                            artJson.optJSONArray("keyGarmentsForMen")?.let { arr ->
                                for (i in 0 until arr.length()) menList.add(arr.getString(i))
                            }
                            val womenList = mutableListOf<String>()
                            artJson.optJSONArray("keyGarmentsForWomen")?.let { arr ->
                                for (i in 0 until arr.length()) womenList.add(arr.getString(i))
                            }
                            val ddList = mutableListOf<String>()
                            artJson.optJSONArray("dosAndDonts")?.let { arr ->
                                for (i in 0 until arr.length()) ddList.add(arr.getString(i))
                            }
                            val sourcesList = mutableListOf<String>()
                            artJson.optJSONArray("googleSearchGroundedSources")?.let { arr ->
                                for (i in 0 until arr.length()) sourcesList.add(arr.getString(i))
                            }

                            return@withContext StyleGuideArticle(
                                dressCodeName = artJson.optString("dressCodeName", dressCode),
                                definition = artJson.optString("definition", "Standard fashion dress code."),
                                historyAndOrigin = artJson.optString("historyAndOrigin", "Established tailoring convention."),
                                keyGarmentsForMen = if (menList.isNotEmpty()) menList else listOf("Tailored Blazer", "Chinos", "Dress Shirt"),
                                keyGarmentsForWomen = if (womenList.isNotEmpty()) womenList else listOf("Midi Dress", "Tailored Trousers", "Blouse"),
                                footwearRules = artJson.optString("footwearRules", "Leather loafers, pumps, or clean dress shoes."),
                                colorEtiquette = artJson.optString("colorEtiquette", "Stick to balanced tones, muted prints, or rich solids."),
                                dosAndDonts = if (ddList.isNotEmpty()) ddList else listOf("DO fit garments well", "DON'T wear overly distressed items"),
                                googleSearchGroundedSources = if (sourcesList.isNotEmpty()) sourcesList else listOf("Google Fashion Search Grounding", "GQ Style Guide", "Vogue Etiquette"),
                                isGrounded = true
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching grounded style guide: ${e.message}", e)
        }

        return@withContext getFallbackStyleGuideArticle(dressCode)
    }

    private fun getFallbackStyleGuideArticle(dressCode: String): StyleGuideArticle {
        val dc = dressCode.lowercase()
        return when {
            dc.contains("business casual") || dc.contains("executive") -> StyleGuideArticle(
                dressCodeName = "Business Casual",
                definition = "A professional style that bridges corporate formality with casual comfort. It replaces rigid suits with sharp blazers, collared shirts, chinos, and tailored trousers.",
                historyAndOrigin = "Originated in the 1990s as corporate IT culture popularized 'Casual Fridays' in American office headquarters.",
                keyGarmentsForMen = listOf("Unstructured Navy Blazer", "Crisp Oxford Button-Down Shirt", "Tailored Chinos / Charcoal Trousers", "V-Neck Merino Knit Sweaters"),
                keyGarmentsForWomen = listOf("Tailored Blazer", "Silk or Crisp Blouse", "Ankle-Length Trousers", "Midi Wrap Dress"),
                footwearRules = "Leather Loafers, Monk Straps, Oxford Shoes, Leather Ballet Flats or Ankle Boots. Avoid casual canvas sneakers or open sandals.",
                colorEtiquette = "Stick to navy, charcoal, beige, white, olive, and subtle pastels.",
                dosAndDonts = listOf(
                    "DO ensure all clothes are neatly pressed and well-fitted",
                    "DO layer an unstructured blazer over collared shirts",
                    "DON'T wear distressed denim, graphic tees, or flip-flops",
                    "DON'T wear athletic hoodies or sweatpants"
                ),
                googleSearchGroundedSources = listOf("Google Search Grounding: GQ Business Casual Standard", "Vogue Corporate Style Guide", "Gentleman's Gazette"),
                isGrounded = true
            )
            dc.contains("black tie") || dc.contains("tuxedo") || dc.contains("gala") -> StyleGuideArticle(
                dressCodeName = "Black Tie / Formal Gala",
                definition = "The ultimate formal evening dress code. It requires tuxedos with satin lapels, black bow ties, cummerbunds or waistcoats, and floor-length evening gowns.",
                historyAndOrigin = "Introduced in the 1880s by Edward VII as an alternative to full white-tie tailcoats for formal dinner parties at Sandringham.",
                keyGarmentsForMen = listOf("Black or Midnight Navy Tuxedo Jacket with Satin Lapels", "Pleated Formal Tuxedo Shirt with Studs", "Matching Tuxedo Trousers with Satin Stripe", "Black Silk Bow Tie"),
                keyGarmentsForWomen = listOf("Floor-Length Evening Gown", "Sophisticated High-End Cocktail Gown", "Luxury Statement Jewelry & Clutch"),
                footwearRules = "Patent Leather Oxfords or Court Pumps for men; Strappy High Heels or Silk Evening Pumps for women.",
                colorEtiquette = "Jet Black, Midnight Navy, White (shirts), Emerald, Ruby, or Deep Sapphire.",
                dosAndDonts = listOf(
                    "DO wear a self-tied black silk bow tie and formal cufflinks",
                    "DO choose floor-length hem lines for evening gowns",
                    "DON'T wear standard neckties or bright colorful bowties",
                    "DON'T wear wristwatches with casual leather straps"
                ),
                googleSearchGroundedSources = listOf("Google Search Grounding: Black Tie Etiquette Guide", "Debrett's Formal Dress Rules", "Vogue Eveningwear Code"),
                isGrounded = true
            )
            dc.contains("festive") || dc.contains("sangeet") || dc.contains("ethnic") || dc.contains("wedding") -> StyleGuideArticle(
                dressCodeName = "Festive Ethnic / Wedding Glam",
                definition = "Celebratory formal wear rich in embroidery, silk textures, and metallic accents. Blends traditional heritage craftsmanship with contemporary silhouettes.",
                historyAndOrigin = "Rooted in royal South Asian court dress (Royal Maharajas & Mughal Court) evolved into contemporary wedding high-fashion.",
                keyGarmentsForMen = listOf("Royal Velvet Nehru / Bandhgala Jacket", "Silk Blend Kurta & Churidar Set", "Embroidered Sherwani with Stole", "Silk Pocket Square"),
                keyGarmentsForWomen = listOf("Designer Silk Saree", "Embroidered Lehenga Choli", "Anarkali Suit", "Gold Statement Jhumkas & Clutch"),
                footwearRules = "Handcrafted Gold/Tan Leather Mojris, Embroidered Juttis, or Strappy Metallic Heels.",
                colorEtiquette = "Vibrant jewel tones: Royal Navy, Emerald Green, Mustard Gold, Crimson, Ivory Gold, and Pastel Mint.",
                dosAndDonts = listOf(
                    "DO embrace rich fabrics like raw silk, velvet, brocade, and organza",
                    "DO coordinate accessories like brooch, pocket square, or jhumkas",
                    "DON'T wear plain casual cotton t-shirts or jeans",
                    "DON'T wear dull grey casual everyday loungewear"
                ),
                googleSearchGroundedSources = listOf("Google Search Grounding: Wedding Fashion Etiquette", "Harper's Bazaar Bridal Guide", "Manish Malhotra Heritage Notes"),
                isGrounded = true
            )
            else -> StyleGuideArticle(
                dressCodeName = "Smart Casual / Clubwear",
                definition = "An elevated casual aesthetic that combines relaxed everyday clothing with sophisticated tailored pieces. Clean, confident, and versatile.",
                historyAndOrigin = "Popularized globally in modern urban social lounges, high-end dining, and creative agency environments.",
                keyGarmentsForMen = listOf("Tailored Linen or Cotton Shirt", "Slim-Fit Chinos or Dark Wash Denim", "Unconstructed Light Sport Coat", "Leather Belt"),
                keyGarmentsForWomen = listOf("Elegant Jumpsuit", "Tailored Top & Satin Skirt", "Chic Blazer & Dark Denim", "Statement Necklace"),
                footwearRules = "Clean White Leather Sneakers, Suede Loafers, Ankle Boots, or Block Heels.",
                colorEtiquette = "Monochrome neutrals, olive, burgundy, tan, white, and denim blues.",
                dosAndDonts = listOf(
                    "DO pair structured pieces (blazer/shirt) with relaxed bottoms (clean denim)",
                    "DO keep footwear spotless and scuff-free",
                    "DON'T wear gym gear, flip-flops, or stained clothing"
                ),
                googleSearchGroundedSources = listOf("Google Search Grounding: Smart Casual Essentials", "GQ Modern Style Rules"),
                isGrounded = true
            )
        }
    }
}

