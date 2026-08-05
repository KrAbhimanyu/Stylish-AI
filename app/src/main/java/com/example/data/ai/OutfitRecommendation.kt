package com.example.data.ai

import com.example.data.local.WardrobeItem

data class WeatherContext(
    val temperatureCelsius: Int = 24,
    val condition: String = "Sunny & Clear", // Sunny, Rainy, Cold/Chilly, Hot & Humid, Windy, Mild
    val location: String = "Mumbai, MH",
    val humidityPercent: Int = 60
)

data class StylePreference(
    val topSize: String = "L",
    val bottomSize: String = "32",
    val shoeSize: String = "UK 10",
    val primaryVibe: String = "Ethnic Chic & Modern", // Minimalist, Streetwear, Ethnic Chic, Formal Elegance, Bohemian, Preppy, Casual Cool
    val colorPreference: String = "Rich & Warm Neutrals", // Pastels, Neutrals, Jewel Tones, Monochrome, Vibrant
    val preferredDressCodes: String = "Festive Ethnic, Smart Casual, Formal",
    val preferredFit: String = "Tailored Regular",
    val comfortPriority: String = "Balanced Style & Comfort"
)

data class MissingPieceSuggestion(
    val itemName: String,
    val category: String,
    val reasonToElevate: String,
    val trendTag: String = "Trending Upgrade",
    val searchQuery: String = ""
)

data class PartnerOutfitDetails(
    val partnerTitle: String = "Partner",
    val avatarType: String = "Adult", // Husband, Wife, Boy, Girl, Kid, Partner
    val top: String = "",
    val bottom: String = "",
    val outerwear: String = "",
    val footwear: String = "",
    val accessories: List<String> = emptyList(),
    val stylingNotes: String = "",
    val avatarImagePrompt: String = "",
    val avatarImageUrl: String = "",
    val outfitImagePrompt: String = ""
)

data class DominantColorSwatch(
    val name: String,
    val hex: String,
    val role: String, // e.g., Top Garment, Bottom Garment, Footwear, Accessory Accent
    val accessoryMatchingTip: String
)

data class OutfitRecommendation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val occasion: String,
    val vibeTag: String,
    val targetPersona: String = "Couple", // Couple, Husband, Wife, Boy, Girl, Kid, Men, Women
    val isCoupleOutfit: Boolean = false,
    val partner1: PartnerOutfitDetails? = null,
    val partner2: PartnerOutfitDetails? = null,
    val top: String = "",
    val bottom: String = "",
    val outerwear: String = "",
    val footwear: String = "",
    val accessories: List<String> = emptyList(),
    val colorPaletteNames: List<String> = emptyList(),
    val colorPaletteHexes: List<String> = emptyList(),
    val weatherComfortScore: Int = 92,
    val weatherAdvice: String = "",
    val stylingTips: String = "",
    val groomingAdvice: String = "",
    val closetItemMatches: List<String> = emptyList(),
    val missingPiecesToElevate: List<MissingPieceSuggestion> = emptyList(),
    val outfitImagePrompt: String = "",
    val outfitImageUrl: String = "",
    val avatarImagePrompt: String = "",
    val avatarImageUrl: String = "",
    val userRating: Int = 0,
    val isAiGenerated: Boolean = true
) {
    fun getDominantColorSwatches(): List<DominantColorSwatch> {
        val swatches = mutableListOf<DominantColorSwatch>()

        // Fallback default colors if lists are empty
        val hexList = if (colorPaletteHexes.isNotEmpty()) colorPaletteHexes else listOf("#1C2D42", "#C3B091", "#111111", "#D4AF37")
        val nameList = if (colorPaletteNames.isNotEmpty()) colorPaletteNames else listOf("Midnight Navy", "Warm Khaki", "Onyx Black", "Metallic Gold")

        // Top Swatch
        swatches.add(
            DominantColorSwatch(
                name = nameList.getOrElse(0) { "Deep Tone" },
                hex = hexList.getOrElse(0) { "#1C2D42" },
                role = "Primary Top Color",
                accessoryMatchingTip = "Pairs with tan leather straps, silver watch, or crisp white pocket square"
            )
        )

        // Bottom Swatch
        swatches.add(
            DominantColorSwatch(
                name = nameList.getOrElse(1) { "Base Tone" },
                hex = hexList.getOrElse(1) { "#36454F" },
                role = "Base Pant/Skirt Color",
                accessoryMatchingTip = "Complements dark brown belt, Chelsea boots, or woven loafers"
            )
        )

        // Footwear Swatch
        swatches.add(
            DominantColorSwatch(
                name = nameList.getOrElse(2) { "Footwear Tone" },
                hex = hexList.getOrElse(2) { "#8B4513" },
                role = "Shoe/Footwear Color",
                accessoryMatchingTip = "Match your belt or handbag hardware directly with this leather/fabric tone"
            )
        )

        // Accent Accessory Swatch
        swatches.add(
            DominantColorSwatch(
                name = nameList.getOrElse(3) { "Jewelry & Accent" },
                hex = hexList.getOrElse(3) { "#D4AF37" },
                role = "Accessory Accent",
                accessoryMatchingTip = "Ideal for statement sunglasses, gold metallic jewelry, cuff links, or scarves"
            )
        )

        return swatches
    }
}


data class SeasonalTrendResult(
    val headline: String,
    val regionSeasonTag: String,
    val summaryText: String,
    val trendingStyles: List<String> = emptyList(),
    val trendingColors: List<String> = emptyList(),
    val keyFabrics: List<String> = emptyList(),
    val stylistNote: String = "",
    val isAiGenerated: Boolean = true
)

data class PackingItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String, // Tops, Bottoms, Outerwear, Footwear, Accessories
    val sourceOutfitTitle: String = "",
    val isPacked: Boolean = false
)

data class PackingCategoryItems(
    val categoryName: String,
    val items: List<PackingItem> = emptyList()
)

data class PackingList(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tripTitle: String = "Upcoming Trip Packing List",
    val selectedOutfitCount: Int = 0,
    val categories: List<PackingCategoryItems> = emptyList(),
    val totalItemCount: Int = 0,
    val packedItemCount: Int = 0,
    val packingTips: List<String> = emptyList()
)

data class SmartStyleAnalysisResult(
    val timestamp: Long = System.currentTimeMillis(),
    val analyzedOutfitCount: Int = 0,
    val detectedAestheticVibe: String = "",
    val detectedColorPalette: String = "",
    val detectedDressCode: String = "",
    val detectedPreferredFit: String = "",
    val keyInsights: List<String> = emptyList(),
    val recommendationSummary: String = "",
    val isDatabaseUpdated: Boolean = true
)

data class StyleGuideArticle(
    val dressCodeName: String,
    val definition: String,
    val historyAndOrigin: String = "",
    val keyGarmentsForMen: List<String> = emptyList(),
    val keyGarmentsForWomen: List<String> = emptyList(),
    val footwearRules: String = "",
    val colorEtiquette: String = "",
    val dosAndDonts: List<String> = emptyList(),
    val googleSearchGroundedSources: List<String> = emptyList(),
    val isGrounded: Boolean = true
)

