package com.example.data.local

import kotlinx.coroutines.flow.Flow

class OutfitRepository(private val dao: OutfitDao) {

    val allWardrobeItems: Flow<List<WardrobeItem>> = dao.getAllWardrobeItems()
    val allEvents: Flow<List<CalendarEvent>> = dao.getAllEvents()
    val allSavedOutfits: Flow<List<SavedOutfit>> = dao.getAllSavedOutfits()
    val userStylePreference: Flow<UserStylePreference?> = dao.getUserStylePreference()

    fun getWardrobeItemsByCategory(category: String): Flow<List<WardrobeItem>> =
        dao.getWardrobeItemsByCategory(category)

    suspend fun addWardrobeItem(item: WardrobeItem): Long = dao.insertWardrobeItem(item)

    suspend fun deleteWardrobeItem(id: Int) = dao.deleteWardrobeItem(id)

    suspend fun addEvent(event: CalendarEvent): Long = dao.insertEvent(event)

    suspend fun deleteEvent(id: Int) = dao.deleteEvent(id)

    suspend fun saveOutfit(outfit: SavedOutfit): Long = dao.insertSavedOutfit(outfit)

    suspend fun getLatestSavedOutfitSync(): SavedOutfit? = dao.getLatestSavedOutfitSync()

    suspend fun deleteSavedOutfit(id: Int) = dao.deleteSavedOutfit(id)

    suspend fun updateSavedOutfit(outfit: SavedOutfit) = dao.updateSavedOutfit(outfit)

    suspend fun updateOutfitRating(id: Int, rating: Int) = dao.updateOutfitRating(id, rating)

    suspend fun saveUserStylePreference(preference: UserStylePreference) =
        dao.insertOrUpdateStylePreference(preference)

    // Offline AI Recommendation Caching Layer
    suspend fun cacheRecommendations(
        prompt: String,
        occasion: String,
        persona: String,
        gender: String,
        recommendations: List<com.example.data.ai.OutfitRecommendation>
    ) {
        if (recommendations.isEmpty()) return
        val jsonArray = org.json.JSONArray()
        recommendations.forEach { rec ->
            val obj = org.json.JSONObject().apply {
                put("id", rec.id)
                put("title", rec.title)
                put("occasion", rec.occasion)
                put("vibeTag", rec.vibeTag)
                put("targetPersona", rec.targetPersona)
                put("isCoupleOutfit", rec.isCoupleOutfit)
                put("top", rec.top)
                put("bottom", rec.bottom)
                put("outerwear", rec.outerwear)
                put("footwear", rec.footwear)
                put("accessories", org.json.JSONArray(rec.accessories))
                put("weatherAdvice", rec.weatherAdvice)
                put("stylingTips", rec.stylingTips)
                put("groomingAdvice", rec.groomingAdvice)
                put("outfitImagePrompt", rec.outfitImagePrompt)
                put("userRating", rec.userRating)
            }
            jsonArray.put(obj)
        }

        val cachedEntity = CachedRecommendationEntity(
            prompt = prompt,
            occasion = occasion,
            persona = persona,
            gender = gender,
            recommendationsJson = jsonArray.toString(),
            timestamp = System.currentTimeMillis()
        )
        dao.insertCachedRecommendation(cachedEntity)
    }

    suspend fun getCachedRecommendations(
        prompt: String,
        occasion: String
    ): List<com.example.data.ai.OutfitRecommendation>? {
        val cached = dao.getCachedRecommendation(prompt, occasion) ?: dao.getLatestCachedRecommendation()
        return cached?.let { parseCachedJson(it.recommendationsJson) }
    }

    private fun parseCachedJson(jsonStr: String): List<com.example.data.ai.OutfitRecommendation> {
        val result = mutableListOf<com.example.data.ai.OutfitRecommendation>()
        try {
            val jsonArray = org.json.JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val accList = mutableListOf<String>()
                val accArray = obj.optJSONArray("accessories")
                if (accArray != null) {
                    for (j in 0 until accArray.length()) {
                        accList.add(accArray.getString(j))
                    }
                }

                result.add(
                    com.example.data.ai.OutfitRecommendation(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", "Cached Outfit Option"),
                        occasion = obj.optString("occasion", "General"),
                        vibeTag = obj.optString("vibeTag", "Cached Vibe"),
                        targetPersona = obj.optString("targetPersona", "General"),
                        isCoupleOutfit = obj.optBoolean("isCoupleOutfit", false),
                        top = obj.optString("top", ""),
                        bottom = obj.optString("bottom", ""),
                        outerwear = obj.optString("outerwear", ""),
                        footwear = obj.optString("footwear", ""),
                        accessories = accList,
                        weatherAdvice = obj.optString("weatherAdvice", ""),
                        stylingTips = obj.optString("stylingTips", ""),
                        groomingAdvice = obj.optString("groomingAdvice", ""),
                        outfitImagePrompt = obj.optString("outfitImagePrompt", ""),
                        userRating = obj.optInt("userRating", 0),
                        isAiGenerated = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}

