package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OutfitDao {
    // Wardrobe Items
    @Query("SELECT * FROM wardrobe_items ORDER BY id DESC")
    fun getAllWardrobeItems(): Flow<List<WardrobeItem>>

    @Query("SELECT * FROM wardrobe_items WHERE category = :category ORDER BY id DESC")
    fun getWardrobeItemsByCategory(category: String): Flow<List<WardrobeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWardrobeItem(item: WardrobeItem): Long

    @Query("DELETE FROM wardrobe_items WHERE id = :id")
    suspend fun deleteWardrobeItem(id: Int)

    // Calendar Events
    @Query("SELECT * FROM calendar_events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEvent(id: Int)

    // Saved Outfits
    @Query("SELECT * FROM saved_outfits ORDER BY timestamp DESC")
    fun getAllSavedOutfits(): Flow<List<SavedOutfit>>

    @Query("SELECT * FROM saved_outfits ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSavedOutfitSync(): SavedOutfit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedOutfit(outfit: SavedOutfit): Long

    @Query("DELETE FROM saved_outfits WHERE id = :id")
    suspend fun deleteSavedOutfit(id: Int)

    @Update
    suspend fun updateSavedOutfit(outfit: SavedOutfit)

    @Query("UPDATE saved_outfits SET userRating = :rating WHERE id = :id")
    suspend fun updateOutfitRating(id: Int, rating: Int)

    // User Style Preferences
    @Query("SELECT * FROM user_style_preferences WHERE id = 1 LIMIT 1")
    fun getUserStylePreference(): Flow<UserStylePreference?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStylePreference(preference: UserStylePreference)

    // Cached AI Recommendations
    @Query("SELECT * FROM cached_recommendations WHERE prompt = :prompt AND occasion = :occasion ORDER BY timestamp DESC LIMIT 1")
    suspend fun getCachedRecommendation(prompt: String, occasion: String): CachedRecommendationEntity?

    @Query("SELECT * FROM cached_recommendations ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestCachedRecommendation(): CachedRecommendationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedRecommendation(cached: CachedRecommendationEntity): Long

    @Query("DELETE FROM cached_recommendations WHERE timestamp < :cutoffTime")
    suspend fun clearOldCache(cutoffTime: Long)
}

