package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wardrobe_items")
data class WardrobeItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Top, Bottom, Dress/Traditional, Outerwear, Footwear, Accessory
    val subcategory: String,
    val color: String,
    val hexColor: String = "#1E2838",
    val fabric: String = "Cotton",
    val formality: String = "Casual", // Casual, Smart Casual, Formal, Festive/Traditional
    val season: String = "All", // All, Summer, Winter, Monsoon
    val isFavorite: Boolean = false
)

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val occasionType: String, // Marriage, Party, Casual Outing, Business, Date Night, Gym, Festival
    val date: String,
    val time: String = "19:00",
    val location: String = "Local Venue",
    val dressCode: String = "Smart Casual",
    val notes: String = ""
)

@Entity(tableName = "saved_outfits")
data class SavedOutfit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val occasion: String,
    val weatherCondition: String,
    val temperature: Int,
    val topItem: String,
    val bottomItem: String,
    val outerwearItem: String = "",
    val footwearItem: String,
    val accessoryItems: String = "",
    val colorPalette: String = "",
    val stylingTips: String = "",
    val weatherComfortReason: String = "",
    val missingPiecesSummary: String = "",
    val imagePath: String = "",
    val userRating: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = true
)

@Entity(tableName = "user_style_preferences")
data class UserStylePreference(
    @PrimaryKey val id: Int = 1,
    val topSize: String = "L",
    val bottomSize: String = "32",
    val shoeSize: String = "UK 10",
    val primaryAestheticVibe: String = "Ethnic Chic & Modern",
    val colorPreferences: String = "Rich & Warm Neutrals",
    val preferredDressCodes: String = "Festive Ethnic, Smart Casual, Formal",
    val preferredFit: String = "Tailored Regular"
)

@Entity(tableName = "cached_recommendations")
data class CachedRecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val occasion: String,
    val persona: String,
    val gender: String,
    val recommendationsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

