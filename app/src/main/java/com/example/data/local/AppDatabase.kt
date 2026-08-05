package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [WardrobeItem::class, CalendarEvent::class, SavedOutfit::class, UserStylePreference::class, CachedRecommendationEntity::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun outfitDao(): OutfitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "outfit_stylist_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.outfitDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: OutfitDao) {
                // Initial Wardrobe Items
                val sampleItems = listOf(
                    WardrobeItem(name = "Royal Navy Velvet Nehru Jacket", category = "Outerwear", subcategory = "Ethnic Layering", color = "Navy Blue", hexColor = "#1B2A4A", fabric = "Velvet", formality = "Festive/Traditional"),
                    WardrobeItem(name = "Silk Blend Cream Kurta & Churidar", category = "Dress/Traditional", subcategory = "Kurta Set", color = "Pearl Cream", hexColor = "#F5F2EB", fabric = "Silk", formality = "Festive/Traditional"),
                    WardrobeItem(name = "Classic Italian Black Tuxedo Blazer", category = "Outerwear", subcategory = "Blazer", color = "Jet Black", hexColor = "#121212", fabric = "Wool Blend", formality = "Formal"),
                    WardrobeItem(name = "Crisp Oxford White Shirt", category = "Top", subcategory = "Button Down", color = "White", hexColor = "#FFFFFF", fabric = "Cotton", formality = "Smart Casual"),
                    WardrobeItem(name = "Tailored Charcoal Chinos", category = "Bottom", subcategory = "Trousers", color = "Charcoal Gray", hexColor = "#36454F", fabric = "Cotton Stretch", formality = "Smart Casual"),
                    WardrobeItem(name = "Breezy Sage Green Linen Shirt", category = "Top", subcategory = "Linen Shirt", color = "Sage Green", hexColor = "#8A9A86", fabric = "Pure Linen", formality = "Casual"),
                    WardrobeItem(name = "Dark Wash Tapered Denim Jeans", category = "Bottom", subcategory = "Jeans", color = "Indigo Navy", hexColor = "#1C2833", fabric = "Denim", formality = "Casual"),
                    WardrobeItem(name = "Embroidered Maroon Sherwani", category = "Dress/Traditional", subcategory = "Wedding Wear", color = "Maroon", hexColor = "#800020", fabric = "Raw Silk", formality = "Festive/Traditional"),
                    WardrobeItem(name = "Tan Leather Double Monk Strap Shoes", category = "Footwear", subcategory = "Dress Shoes", color = "Cognac Tan", hexColor = "#935116", fabric = "Genuine Leather", formality = "Formal"),
                    WardrobeItem(name = "Minimalist White Leather Sneakers", category = "Footwear", subcategory = "Sneakers", color = "Pure White", hexColor = "#FBFBFB", fabric = "Leather", formality = "Casual"),
                    WardrobeItem(name = "Handcrafted Leather Mojris / Juttis", category = "Footwear", subcategory = "Traditional", color = "Gold & Tan", hexColor = "#D4AF37", fabric = "Leather/Embroidery", formality = "Festive/Traditional"),
                    WardrobeItem(name = "Rose Gold Chronograph Watch", category = "Accessory", subcategory = "Watch", color = "Rose Gold", hexColor = "#B76E79", fabric = "Metal", formality = "All"),
                    WardrobeItem(name = "Textured Silk Pocket Square & Brooch", category = "Accessory", subcategory = "Jewelry/Accent", color = "Gold & Burgundy", hexColor = "#800020", fabric = "Silk/Brass", formality = "Festive/Traditional")
                )

                for (item in sampleItems) {
                    dao.insertWardrobeItem(item)
                }

                // Initial Calendar Events
                val sampleEvents = listOf(
                    CalendarEvent(
                        title = "Rohan & Priya's Grand Wedding Sangeet",
                        occasionType = "Marriage",
                        date = "2026-08-15",
                        time = "19:30",
                        location = "The Grand Palace Ballroom",
                        dressCode = "Festive Glam / Ethnic Chic",
                        notes = "Indoor air-conditioned hall, evening dance night"
                    ),
                    CalendarEvent(
                        title = "Summer Rooftop Cocktail Party",
                        occasionType = "Party",
                        date = "2026-08-08",
                        time = "20:00",
                        location = "Skyline Lounge & Terrace",
                        dressCode = "Smart Casual / Clubwear",
                        notes = "Open rooftop breeze, warm 26°C night"
                    ),
                    CalendarEvent(
                        title = "Q3 Strategy Pitch with Investors",
                        occasionType = "Business",
                        date = "2026-08-12",
                        time = "11:00",
                        location = "Tech Tower Conference Room",
                        dressCode = "Business Professional",
                        notes = "Crisp formal tailored look required"
                    )
                )

                for (event in sampleEvents) {
                    dao.insertEvent(event)
                }

                // Sample Saved Outfit
                val sampleOutfit = SavedOutfit(
                    title = "Royal Velvet Sangeet Ensemble",
                    occasion = "Marriage",
                    weatherCondition = "Clear Evening",
                    temperature = 22,
                    topItem = "Silk Blend Cream Kurta",
                    bottomItem = "Slim Churidar Trousers",
                    outerwearItem = "Royal Navy Velvet Nehru Jacket",
                    footwearItem = "Handcrafted Gold Mojris",
                    accessoryItems = "Rose Gold Watch & Pocket Square",
                    colorPalette = "Navy + Pearl White + Champagne Gold",
                    stylingTips = "Drape a contrasting pocket square in the jacket chest pocket for royal flair.",
                    weatherComfortReason = "Velvet layer keeps you comfortable in 22°C evening AC, while breathable silk inner allows easy dancing."
                )
                dao.insertSavedOutfit(sampleOutfit)

                // Initial User Style Preference
                val defaultPreference = UserStylePreference(
                    id = 1,
                    topSize = "L",
                    bottomSize = "32",
                    shoeSize = "UK 10",
                    primaryAestheticVibe = "Ethnic Chic & Modern",
                    colorPreferences = "Rich & Warm Neutrals",
                    preferredDressCodes = "Festive Ethnic, Smart Casual, Formal",
                    preferredFit = "Tailored Regular"
                )
                dao.insertOrUpdateStylePreference(defaultPreference)
            }
        }
    }
}
