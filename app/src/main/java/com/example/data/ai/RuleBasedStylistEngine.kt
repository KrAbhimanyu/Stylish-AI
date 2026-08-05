package com.example.data.ai

import com.example.data.local.WardrobeItem

object RuleBasedStylistEngine {

    fun generate5Recommendations(
        occasion: String,
        userQuery: String,
        weather: WeatherContext,
        style: StylePreference,
        wardrobe: List<WardrobeItem>,
        targetPersona: String = "Couple",
        genderSelection: String = "Male"
    ): List<OutfitRecommendation> {
        val q = userQuery.lowercase()
        val occ = occasion.lowercase()

        val isMaleSelected = genderSelection.equals("Male", ignoreCase = true)
        val isFemaleSelected = genderSelection.equals("Female", ignoreCase = true)

        val mentionsBoyOrMale = q.contains("boy") || q.contains("son") || q.contains("husband") || q.contains("men") || q.contains("man") || q.contains("male") || q.contains("guy") || q.contains("gentleman")
        val mentionsGirlOrFemale = q.contains("girl") || q.contains("daughter") || q.contains("wife") || q.contains("women") || q.contains("woman") || q.contains("female") || q.contains("lady")
        val mentionsCouple = q.contains("couple") || q.contains("both") || q.contains("husband and wife") || q.contains("wife and husband") || q.contains("we are going") || q.contains("we are couple")

        val isCouple = when {
            mentionsCouple -> true
            isMaleSelected -> false
            isFemaleSelected -> false
            mentionsBoyOrMale && !mentionsGirlOrFemale -> false
            mentionsGirlOrFemale && !mentionsBoyOrMale -> false
            else -> targetPersona.contains("couple", ignoreCase = true)
        }

        val persona = when {
            isCouple -> "Couple"
            isMaleSelected -> when {
                q.contains("boy") || q.contains("son") -> "Boy"
                q.contains("kid") || q.contains("toddler") -> "Kid"
                else -> "Husband"
            }
            isFemaleSelected -> when {
                q.contains("girl") || q.contains("daughter") -> "Girl"
                q.contains("kid") || q.contains("toddler") -> "Kid"
                else -> "Wife"
            }
            q.contains("boy") || q.contains("son") -> "Boy"
            q.contains("girl") || q.contains("daughter") -> "Girl"
            q.contains("kid") || q.contains("toddler") || q.contains("child") -> "Kid"
            q.contains("wife") || q.contains("women") || q.contains("lady") -> "Wife"
            q.contains("husband") || q.contains("men") || q.contains("man") -> "Husband"
            targetPersona.isNotBlank() && !targetPersona.equals("Couple", ignoreCase = true) -> targetPersona
            else -> if (mentionsBoyOrMale) "Boy" else "Men"
        }

        val temp = weather.temperatureCelsius

        val rawList = if (isCouple) {
            generateCouple5Recommendations(occ, q, temp)
        } else {
            generateIndividual5Recommendations(persona, occ, q, temp)
        }

        return rawList.mapIndexed { index, rec -> enrichWithImageAssets(rec, index) }
    }

    fun enrichWithImageAssets(recommendation: OutfitRecommendation, index: Int = 0): OutfitRecommendation {
        val persona = recommendation.targetPersona.ifBlank { "Men" }
        val vibe = recommendation.vibeTag
        val occ = recommendation.occasion

        // Clean & Fix Option Title Number
        val rawTitle = recommendation.title
            .replace(Regex("^Option\\s*\\d+\\s*[:\\-]?\\s*", RegexOption.IGNORE_CASE), "")
            .trim()
        val fixedTitle = "Option ${index + 1}: $rawTitle"

        val pLower = persona.lowercase()
        val titleAndTop = (recommendation.title + " " + recommendation.vibeTag + " " + recommendation.top).lowercase()

        val isBoy = pLower.contains("boy") || pLower.contains("son") || titleAndTop.contains("boy")
        val isGirl = pLower.contains("girl") || pLower.contains("daughter") || titleAndTop.contains("girl")
        val isMale = isBoy || pLower.contains("husband") || pLower.contains("men") || pLower.contains("man") || pLower.contains("groom") || titleAndTop.contains("boy") || titleAndTop.contains("men") || titleAndTop.contains("husband")
        val isFemale = isGirl || pLower.contains("wife") || pLower.contains("women") || pLower.contains("lady") || pLower.contains("bride") || titleAndTop.contains("girl") || titleAndTop.contains("wife") || titleAndTop.contains("women")

        // Dedicated Male / Boy Outfit Image Pools
        val maleWeddingOutfitImages = listOf(
            "https://images.unsplash.com/photo-1597983073493-88cd35cfa3d0?w=800&auto=format&fit=crop", // Royal Silk Kurta
            "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&auto=format&fit=crop", // Midnight Navy Tuxedo
            "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=800&auto=format&fit=crop", // Tailored Royal Suit
            "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=800&auto=format&fit=crop", // Traditional Bandhgala
            "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=800&auto=format&fit=crop"  // Dapper Suit
        )

        val malePartyOutfitImages = listOf(
            "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=800&auto=format&fit=crop", // Charcoal Glam Blazer
            "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=800&auto=format&fit=crop", // Riviera Linen Blazer
            "https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=800&auto=format&fit=crop", // Urban Streetwear Shacket
            "https://images.unsplash.com/photo-1485230895905-ec40ba36b9bc?w=800&auto=format&fit=crop", // Evening Trench Coat
            "https://images.unsplash.com/photo-1492707892479-7bc8d5a4ee93?w=800&auto=format&fit=crop"  // Minimalist Slate Fit
        )

        val maleCasualOutfitImages = listOf(
            "https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=800&auto=format&fit=crop", // Boy Smart Casual Fit
            "https://images.unsplash.com/photo-1525507119028-ed4c629a60a3?w=800&auto=format&fit=crop", // Oxford Shirt & Chinos
            "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=800&auto=format&fit=crop", // Urban Denim & Tee
            "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?w=800&auto=format&fit=crop", // Stylish Boy Casual
            "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=800&auto=format&fit=crop"  // Warm Knit & Trousers
        )

        // Dedicated Female / Girl Outfit Image Pools
        val femaleWeddingOutfitImages = listOf(
            "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800&auto=format&fit=crop", // Royal Emerald Lehenga
            "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800&auto=format&fit=crop", // Champagne Rose Gold Saree
            "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&auto=format&fit=crop", // Mint Green Organza Outfit
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop", // Designer Festive Gown
            "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800&auto=format&fit=crop"  // Silk Saree Heritage
        )

        val femalePartyOutfitImages = listOf(
            "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?w=800&auto=format&fit=crop"
        )

        val femaleCasualOutfitImages = listOf(
            "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800&auto=format&fit=crop"
        )

        val isWedding = occ.contains("marriage", ignoreCase = true) || occ.contains("wedding", ignoreCase = true) || occ.contains("sangeet", ignoreCase = true) || occ.contains("reception", ignoreCase = true)
        val isParty = occ.contains("party", ignoreCase = true) || vibe.contains("glam", ignoreCase = true) || occ.contains("dinner", ignoreCase = true)

        val selectedOutfitPool = when {
            isMale -> when {
                isWedding -> maleWeddingOutfitImages
                isParty -> malePartyOutfitImages
                else -> maleCasualOutfitImages
            }
            isFemale -> when {
                isWedding -> femaleWeddingOutfitImages
                isParty -> femalePartyOutfitImages
                else -> femaleCasualOutfitImages
            }
            else -> when {
                isWedding -> femaleWeddingOutfitImages
                isParty -> malePartyOutfitImages
                else -> maleCasualOutfitImages
            }
        }

        val outfitPrompt = if (recommendation.outfitImagePrompt.isNotBlank()) recommendation.outfitImagePrompt else {
            "High-fashion editorial layout photograph of ${recommendation.top} and ${recommendation.bottom}, $vibe style with complementary accessories"
        }

        val outfitUrl = selectedOutfitPool[index % selectedOutfitPool.size]

        // 5 Distinct Avatar Image Pools for Each Persona
        val husbandAvatars = listOf(
            "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=400&auto=format&fit=crop"
        )

        val wifeAvatars = listOf(
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&auto=format&fit=crop"
        )

        val boyAvatars = listOf(
            "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1543610892-0b1f7e6d8ac1?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1485546246426-74dc88dec4d9?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=400&auto=format&fit=crop"
        )

        val girlAvatars = listOf(
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1516627145497-ae6968895b74?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop"
        )

        val kidAvatars = listOf(
            "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1503944583220-79d8926ad5e2?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1543610892-0b1f7e6d8ac1?w=400&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1516627145497-ae6968895b74?w=400&auto=format&fit=crop"
        )

        val avatarPrompt = if (recommendation.avatarImagePrompt.isNotBlank()) recommendation.avatarImagePrompt else {
            "Minimalist modern fashion portrait illustration of $persona in $vibe style"
        }

        val avatarUrl = when {
            isBoy -> boyAvatars[index % boyAvatars.size]
            isGirl -> girlAvatars[index % girlAvatars.size]
            pLower.contains("husband") || pLower.contains("men") || pLower.contains("man") -> husbandAvatars[index % husbandAvatars.size]
            pLower.contains("wife") || pLower.contains("women") || pLower.contains("lady") -> wifeAvatars[index % wifeAvatars.size]
            pLower.contains("kid") -> kidAvatars[index % kidAvatars.size]
            isMale -> husbandAvatars[index % husbandAvatars.size]
            isFemale -> wifeAvatars[index % wifeAvatars.size]
            else -> husbandAvatars[index % husbandAvatars.size]
        }

        val p1Enriched = recommendation.partner1?.let { p ->
            p.copy(
                avatarImagePrompt = if (p.avatarImagePrompt.isNotBlank()) p.avatarImagePrompt else "Dapper portrait avatar for ${p.partnerTitle} in ${p.top}",
                avatarImageUrl = husbandAvatars[index % husbandAvatars.size],
                outfitImagePrompt = if (p.outfitImagePrompt.isNotBlank()) p.outfitImagePrompt else "High fashion studio layout for ${p.partnerTitle} outfit"
            )
        }

        val p2Enriched = recommendation.partner2?.let { p ->
            p.copy(
                avatarImagePrompt = if (p.avatarImagePrompt.isNotBlank()) p.avatarImagePrompt else "Elegant portrait avatar for ${p.partnerTitle} in ${p.top}",
                avatarImageUrl = wifeAvatars[index % wifeAvatars.size],
                outfitImagePrompt = if (p.outfitImagePrompt.isNotBlank()) p.outfitImagePrompt else "High fashion studio layout for ${p.partnerTitle} outfit"
            )
        }

        return recommendation.copy(
            title = fixedTitle,
            outfitImagePrompt = outfitPrompt,
            outfitImageUrl = outfitUrl,
            avatarImagePrompt = avatarPrompt,
            avatarImageUrl = avatarUrl,
            partner1 = p1Enriched,
            partner2 = p2Enriched
        )
    }

    private fun generateCouple5Recommendations(occ: String, q: String, temp: Int): List<OutfitRecommendation> {
        val isWedding = occ.contains("marriage") || occ.contains("wedding") || q.contains("marriage") || q.contains("sangeet") || q.contains("festival")
        val isParty = occ.contains("party") || q.contains("party") || q.contains("club") || q.contains("night")

        if (isWedding) {
            return listOf(
                OutfitRecommendation(
                    title = "Option 1: Royal Emerald & Velvet Harmony",
                    occasion = "Marriage / Wedding Ceremony",
                    vibeTag = "Festive Grandeur Couple",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband / Groom",
                        avatarType = "Husband",
                        top = "Deep Emerald Raw Silk Kurta with Antique Zari Threading",
                        bottom = "Pearl Cream Churidar Pyjama",
                        outerwear = "Royal Velvet Navy Blue Nehru Jacket",
                        footwear = "Handcrafted Tan Leather Mojris with Gold Accents",
                        accessories = listOf("Embossed Brass Brooch", "Rose Gold Watch"),
                        stylingNotes = "Keep the top button fastened and coordinate brooch with her gold jewelry."
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife / Bride",
                        avatarType = "Wife",
                        top = "Heavy Zardozi Embroidered Silk Blouse",
                        bottom = "Royal Emerald & Crimson Kalidar Lehanga",
                        outerwear = "Sheer Tissue Net Dupatta with Scalloped Borders",
                        footwear = "Embellished Block Heel Juttis",
                        accessories = listOf("Kundan Necklace Set", "Matching Maang Tikka", "Gold Bangles"),
                        stylingNotes = "Drape dupatta gracefully over right shoulder to mirror his emerald lapel."
                    ),
                    colorPaletteNames = listOf("Emerald Green", "Royal Navy", "Champagne Gold"),
                    colorPaletteHexes = listOf("#0B3C26", "#1B2A4A", "#D4AF37"),
                    weatherAdvice = "Pure raw silks and velvet coats provide comfortable elegance in ${temp}°C celebratory evening air.",
                    stylingTips = "Couple Color Harmony: The husband's emerald kurta matches her lehenga borders, creating unified photos.",
                    groomingAdvice = "Husband: Beard trim & woody cologne. Wife: Dewy makeup with warm rose fragrance.",
                    missingPiecesToElevate = listOf(
                        MissingPieceSuggestion("Matching Emerald Silk Pocket Square", "Accessory", "Harmonizes husband's jacket directly with wife's outfit."),
                        MissingPieceSuggestion("Velvet Potli Bag with Pearl Tassels", "Accessory", "Elevates her lehenga with royal heritage accents.")
                    )
                ),
                OutfitRecommendation(
                    title = "Option 2: Champagne & Rose Gold Pastel Co-ords",
                    occasion = "Sangeet / Reception Night",
                    vibeTag = "Contemporary Pastel Glam",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Partner A (Him)",
                        avatarType = "Husband",
                        top = "Champagne Gold Self-Design Sherwani / Bandhgala",
                        bottom = "Fitted Tapered Cream Trousers",
                        footwear = "Ivory Leather Slip-on Mojris",
                        accessories = listOf("Pearl Strand Lapel Chain", "Sleek Gold Cufflinks")
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Partner B (Her)",
                        avatarType = "Wife",
                        top = "Sequin-Dusted Dust Rose Crop Top Blouse",
                        bottom = "Champagne Gold Shimmer Georgette Saree / Skirt",
                        footwear = "Champagne Metallic Strappy Stilettos",
                        accessories = listOf("Rose Gold Diamond Choker", "Crystal Clutch")
                    ),
                    colorPaletteNames = listOf("Champagne Gold", "Blush Rose", "Ivory Cream"),
                    colorPaletteHexes = listOf("#E5D3B3", "#DEA5A4", "#FFFFF0"),
                    weatherAdvice = "Lightweight shimmer georgettes and cotton-silk bandhgalas allow easy movement for sangeet dance performances.",
                    stylingTips = "Rose gold metallic accessories tie both partners together into a dreamy modern palette.",
                    missingPiecesToElevate = listOf(
                        MissingPieceSuggestion("Rose Gold Sequin Clutch", "Accessory", "Adds shimmer reflecting event stage lights.")
                    )
                ),
                OutfitRecommendation(
                    title = "Option 3: Midnight Velvet & Sequined Noir",
                    occasion = "Wedding Reception Gala",
                    vibeTag = "Sophisticated Night Luxury",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband",
                        avatarType = "Husband",
                        top = "Crisp White Italian Tuxedo Shirt",
                        bottom = "Tapered Black Tuxedo Trousers with Satin Stripe",
                        outerwear = "Midnight Black Velvet Tuxedo Jacket with Peak Satin Lapel",
                        footwear = "Black Patent Leather Oxfords",
                        accessories = listOf("Black Silk Bowtie", "Silver Cufflinks")
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife",
                        avatarType = "Wife",
                        top = "Corset Bodice Sequined Midnight Gown",
                        bottom = "Flowing Floor-Length Mermaid Skirt",
                        footwear = "Silver Crystal Heels",
                        accessories = listOf("Diamond Drop Earrings", "Silver Satin Clutch")
                    ),
                    colorPaletteNames = listOf("Midnight Black", "Satin Noir", "Diamond Silver"),
                    colorPaletteHexes = listOf("#0A0A0C", "#1E1E24", "#E0E0E0"),
                    weatherAdvice = "Structured tuxedos and velvet evening gowns keep both warm in late night ${temp}°C venue receptions.",
                    stylingTips = "Black-tie elegance with monochromatic high contrast for stunning couple portraits.",
                    missingPiecesToElevate = listOf(
                        MissingPieceSuggestion("Silver Satin Pocket Square", "Accessory", "Links husband's tuxedo to wife's silver diamonds.")
                    )
                ),
                OutfitRecommendation(
                    title = "Option 4: Vibrant Mustard & Maroon Festive Fusion",
                    occasion = "Haldi / Mehendi Celebration",
                    vibeTag = "Vibrant Cultural Festivity",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband",
                        avatarType = "Husband",
                        top = "Mustard Yellow Mirrorwork Angrakha Kurta",
                        bottom = "White Linen Dhoti Pants / Pyjama",
                        footwear = "Tan Braided Leather Juttis"
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife",
                        avatarType = "Wife",
                        top = "Mirror-Embellished Mustard Yellow Anarkali",
                        bottom = "Flowing Maroon Sharara Pants",
                        footwear = "Hand-Painted Floral Juttis",
                        accessories = listOf("Gota Patti Jewelry Set", "Floral Bangles")
                    ),
                    colorPaletteNames = listOf("Mustard Yellow", "Rich Maroon", "Fresh White"),
                    colorPaletteHexes = listOf("#FFDB58", "#800020", "#FFFFFF"),
                    weatherAdvice = "Breathable 100% cotton-linen blends stay dry and cool during daytime Haldi festivities.",
                    stylingTips = "Playful mirrorwork on both outfits creates joyful visual energy perfect for festive photos.",
                    missingPiecesToElevate = listOf(
                        MissingPieceSuggestion("Braided Leather Kolhapuri Chappals", "Footwear", "Authentic traditional footwear for comfortable day events.")
                    )
                ),
                OutfitRecommendation(
                    title = "Option 5: Ivory & Mint Green Botanical Chic",
                    occasion = "Daytime Garden Wedding",
                    vibeTag = "Serene Outdoor Luxury",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband",
                        avatarType = "Husband",
                        top = "Mint Green Chikankari Embroidered Kurta",
                        bottom = "Slim Ivory Silk Pyjama",
                        footwear = "Cream Leather Loafers"
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife",
                        avatarType = "Wife",
                        top = "Mint Green Floral Hand-Printed Organza Saree",
                        bottom = "Ivory Satin Petticoat Skirt",
                        footwear = "Nude Ankle-Strap Heels",
                        accessories = listOf("Pearl Choker", "Pastel Handbag")
                    ),
                    colorPaletteNames = listOf("Mint Green", "Ivory White", "Pastel Peach"),
                    colorPaletteHexes = listOf("#98FF98", "#FFFFF0", "#FFDAB9"),
                    weatherAdvice = "Lightweight organza and breezy chikankari weaves reflect sunlight in ${temp}°C outdoor gardens.",
                    stylingTips = "Soft pastel tones create an airy, romantic vibe ideal for sunlit outdoor events.",
                    missingPiecesToElevate = listOf(
                        MissingPieceSuggestion("Hand-Painted Pearl Clutch", "Accessory", "Complements the soft mint floral motifs seamlessly.")
                    )
                )
            )
        } else {
            // General / Party Couple Outfits
            return listOf(
                OutfitRecommendation(
                    title = "Option 1: Modern Glam Party Pair",
                    occasion = "Cocktail Party & Dancing",
                    vibeTag = "Sleek High-Street Glamour",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Partner A (Him / Husband)",
                        avatarType = "Husband",
                        top = "Dark Charcoal Satin Button-Down Shirt",
                        bottom = "Slim Tapered Jet Black Chinos",
                        outerwear = "Unstructured Italian Navy Blazer",
                        footwear = "Black Leather Chelsea Boots",
                        accessories = listOf("Silver Minimalist Chain", "Leather Belt")
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Partner B (Her / Wife)",
                        avatarType = "Wife",
                        top = "Metallic Shimmer Satin Wrap Top",
                        bottom = "High-Waisted Leather Look Trousers / Midi Skirt",
                        footwear = "Black Ankle Strap Stilettos",
                        accessories = listOf("Silver Hoop Earrings", "Glitter Clutch")
                    ),
                    colorPaletteNames = listOf("Charcoal Black", "Metallic Silver", "Midnight Navy"),
                    colorPaletteHexes = listOf("#121212", "#C0C0C0", "#1B2A4A"),
                    weatherAdvice = "Satin fabrics block chilly night winds in ${temp}°C while maintaining effortless high-fashion sheen.",
                    stylingTips = "Silver metallic accents on her jewelry match his neck chain for a synchronized night look.",
                    missingPiecesToElevate = listOf(
                        MissingPieceSuggestion("Distressed Vintage Denim Jacket", "Outerwear", "Casual layer to drape over shoulders when traveling between venues.")
                    )
                ),
                OutfitRecommendation(
                    title = "Option 2: Casual Chic Sunday Brunch Pair",
                    occasion = "Weekend Outing / Brunch",
                    vibeTag = "Effortless Riviera Elegance",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband",
                        avatarType = "Husband",
                        top = "Pastel Blue Linen Mandarin Collar Shirt",
                        bottom = "Beige Tapered Chino Shorts / Pants",
                        footwear = "White Minimalist Leather Sneakers"
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife",
                        avatarType = "Wife",
                        top = "White Eyelet Cotton Sundress / Linen Co-ord",
                        bottom = "Breezy A-line Skirt",
                        footwear = "Woven Espadrille Wedges",
                        accessories = listOf("Straw Tote Bag", "Tortoiseshell Sunglasses")
                    ),
                    colorPaletteNames = listOf("Pastel Blue", "Beige Sand", "Crisp White"),
                    colorPaletteHexes = listOf("#AEC6CF", "#F5F5DC", "#FFFFFF"),
                    weatherAdvice = "Linen and eyelet cotton weaves allow maximum ventilation in ${temp}°C sunshine.",
                    stylingTips = "Roll up shirt sleeves twice and pair with oversized sunglasses for relaxed cafe style."
                ),
                OutfitRecommendation(
                    title = "Option 3: Urban Streetwear Edge Pair",
                    occasion = "Music Concert / Night Festival",
                    vibeTag = "Urban High-Street Trend",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband",
                        avatarType = "Husband",
                        top = "Oversized Graphic Heavyweight Tee",
                        bottom = "Cargo Jogger Pants with Utility Straps",
                        outerwear = "Washed Indigo Denim Trucker Jacket",
                        footwear = "High-Top Retro Sneakers"
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife",
                        avatarType = "Wife",
                        top = "Cropped Ribbed Tank Top",
                        bottom = "High-Waisted Wide Leg Denim Jeans",
                        outerwear = "Oversized Flannel Shacket",
                        footwear = "Chunky Platform Leather Boots"
                    ),
                    colorPaletteNames = listOf("Washed Denim", "Olive Cargo", "Vintage Off-White"),
                    colorPaletteHexes = listOf("#4D6B82", "#556B2F", "#FAF0E6"),
                    weatherAdvice = "Layered cotton shackets keep comfortable during late night open-air music festivals."
                ),
                OutfitRecommendation(
                    title = "Option 4: Royal Velvet Dinner Date",
                    occasion = "Romantic Candlelight Dinner",
                    vibeTag = "Rich Deep Tone Luxury",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband",
                        avatarType = "Husband",
                        top = "Burgundy Wine Fitted Knit Sweater / Shirt",
                        bottom = "Dark Charcoal Tailored Dress Trousers",
                        footwear = "Tan Leather Monk Strap Shoes"
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife",
                        avatarType = "Wife",
                        top = "Burgundy Velvet Bodycon Dress",
                        footwear = "Nude Pointed Toe Pumps",
                        accessories = listOf("Gold Layered Necklace", "Quilted Leather Shoulder Bag")
                    ),
                    colorPaletteNames = listOf("Burgundy Wine", "Charcoal Gray", "Warm Gold"),
                    colorPaletteHexes = listOf("#800020", "#36454F", "#FFD700"),
                    weatherAdvice = "Soft knits and rich velvet maintain cozy warmth during intimate evening dinner dates."
                ),
                OutfitRecommendation(
                    title = "Option 5: Minimalist Monochrome Pair",
                    occasion = "Art Gallery / Evening Soiree",
                    vibeTag = "Minimalist Architectural Fit",
                    targetPersona = "Couple",
                    isCoupleOutfit = true,
                    partner1 = PartnerOutfitDetails(
                        partnerTitle = "Husband",
                        avatarType = "Husband",
                        top = "Black Crew Neck Fine Wool Tee / Knit",
                        bottom = "Slate Gray Pleated Trousers",
                        footwear = "Black Minimal Leather Loafers"
                    ),
                    partner2 = PartnerOutfitDetails(
                        partnerTitle = "Wife",
                        avatarType = "Wife",
                        top = "Architectural Asymmetric Black Blouse",
                        bottom = "Slate Gray Tailored Palazzo Pants",
                        footwear = "Black Square-Toe Mules",
                        accessories = listOf("Sculptural Gold Cuff", "Minimal Leather Pouch")
                    ),
                    colorPaletteNames = listOf("Slate Gray", "Pure Black", "Architectural White"),
                    colorPaletteHexes = listOf("#708090", "#000000", "#F8F8FF"),
                    weatherAdvice = "Pleated trousers and fine knits offer breathable sophistication for gallery strolls."
                )
            )
        }
    }

    private fun generateIndividual5Recommendations(
        persona: String,
        occ: String,
        q: String,
        temp: Int
    ): List<OutfitRecommendation> {
        val isFemale = persona.equals("Girl", ignoreCase = true) || persona.equals("Wife", ignoreCase = true) || persona.equals("Women", ignoreCase = true)

        if (isFemale) {
            val topPrefix = when (persona) {
                "Girl" -> "Girl's Charming "
                "Wife" -> "Wife's Elegant "
                else -> "Women's "
            }
            return listOf(
                OutfitRecommendation(
                    title = "Option 1: $topPrefix Royal Anarkali & Silk Festivity",
                    occasion = occ.ifBlank { "Festival / Celebration" },
                    vibeTag = "$persona Royal Ethnic",
                    targetPersona = persona,
                    isCoupleOutfit = false,
                    top = "$topPrefix Zardozi Embroidered Silk Anarkali / Kurti in Royal Blue",
                    bottom = "Matching Silk Churidar & Flared Skirt Base",
                    outerwear = "Sheer Tissue Net Dupatta with Scalloped Gold Borders",
                    footwear = "Embellished Block Heel Juttis",
                    accessories = listOf("Kundan Jhumka Earrings", "Pearl Bangle Set"),
                    colorPaletteNames = listOf("Royal Blue", "Pearl Cream", "Gold"),
                    colorPaletteHexes = listOf("#4169E1", "#FFFFF0", "#FFD700"),
                    weatherAdvice = "Breezy silk-georgette dupatta offers elegant movement in ${temp}°C celebratory air.",
                    stylingTips = "Drape the dupatta over one shoulder and add gold jhumkas to elevate the traditional charm."
                ),
                OutfitRecommendation(
                    title = "Option 2: $topPrefix Pastel Silk Smart Casual",
                    occasion = occ.ifBlank { "Party & Outing" },
                    vibeTag = "$persona Smart Casual",
                    targetPersona = persona,
                    isCoupleOutfit = false,
                    top = "$topPrefix Pastel Sage Silk Blend Tunic / Blouse",
                    bottom = "Cream High-Waisted Wide Leg Trousers",
                    outerwear = "Lightweight Soft Linen Shrug",
                    footwear = "Chic Leather Block Heels / Mules",
                    accessories = listOf("Delicate Gold Pendant", "Pastel Handbag"),
                    colorPaletteNames = listOf("Pastel Sage", "Cream Ivory", "Rose Gold"),
                    colorPaletteHexes = listOf("#9CAF88", "#FFFFF0", "#B76E79"),
                    weatherAdvice = "Breathable tunic fabric permits maximum comfort for afternoon outings.",
                    stylingTips = "Pair with minimalist rose gold accessories for a soft, polished finish."
                ),
                OutfitRecommendation(
                    title = "Option 3: $topPrefix High-Street Chic Layer",
                    occasion = occ.ifBlank { "Night Party / Concert" },
                    vibeTag = "$persona Urban Chic",
                    targetPersona = persona,
                    isCoupleOutfit = false,
                    top = "$topPrefix Metallic Shimmer Wrap Top",
                    bottom = "High-Waisted Dark Denim Trousers",
                    outerwear = "Cropped Washed Blue Denim Jacket",
                    footwear = "Ankle Strap Leather Boots / Stilettos",
                    accessories = listOf("Silver Hoop Earrings", "Crossbody Leather Bag"),
                    colorPaletteNames = listOf("Metallic Silver", "Midnight Navy", "Jet Black"),
                    colorPaletteHexes = listOf("#C0C0C0", "#1B2A4A", "#000000"),
                    weatherAdvice = "Cropped denim jacket provides cool night-time protection."
                ),
                OutfitRecommendation(
                    title = "Option 4: $topPrefix Executive Silk Pantsuit",
                    occasion = occ.ifBlank { "Formal Gathering / Dinner" },
                    vibeTag = "$persona Formal Elegance",
                    targetPersona = persona,
                    isCoupleOutfit = false,
                    top = "$topPrefix Tailored Silk Button-Down Blouse",
                    bottom = "Charcoal Gray Tailored Straight Pants",
                    outerwear = "Single-Breasted Navy Blue Formal Blazer",
                    footwear = "Pointed Toe Leather Pumps",
                    accessories = listOf("Sleek Leather Handbag", "Formal Wristwatch"),
                    colorPaletteNames = listOf("Navy Blue", "Pure White", "Charcoal Gray"),
                    colorPaletteHexes = listOf("#1E2838", "#FFFFFF", "#36454F"),
                    weatherAdvice = "Crisp silk-cotton inner layer provides structured comfort indoors."
                ),
                OutfitRecommendation(
                    title = "Option 5: $topPrefix Breezy Summer Floral Maxi",
                    occasion = occ.ifBlank { "Beach / Vacation / Weekend" },
                    vibeTag = "$persona Vacation Casual",
                    targetPersona = persona,
                    isCoupleOutfit = false,
                    top = "$topPrefix Tropical Floral Printed Chiffon Maxi Dress",
                    bottom = "Breezy Tiered Flowy Skirt Base",
                    footwear = "Woven Leather Strap Sandals",
                    accessories = listOf("Wide-Brim Straw Hat", "Tortoiseshell Sunglasses"),
                    colorPaletteNames = listOf("Tropical Sage", "Beige Sand", "Sun Gold"),
                    colorPaletteHexes = listOf("#8FBC8F", "#F5F5DC", "#FFD700"),
                    weatherAdvice = "Flowy chiffon maxi keeps body cool during warm sunlit vacations."
                )
            )
        }

        val topPrefix = when (persona) {
            "Boy" -> "Young Men's / Boy's "
            "Kid" -> "Kid's Playful "
            "Husband" -> "Husband's Dapper "
            else -> "Men's "
        }

        return listOf(
            OutfitRecommendation(
                title = "Option 1: $topPrefix Heritage Festival Look",
                occasion = occ.ifBlank { "Festival / Celebration" },
                vibeTag = "$persona Royal Ethnic",
                targetPersona = persona,
                isCoupleOutfit = false,
                top = "$topPrefix Embroidered Silk Kurta in Royal Blue",
                bottom = "Cream Tailored Churidar / Trousers",
                outerwear = "Gold Zari Woven Nehru Waistcoat",
                footwear = "Handcrafted Leather Mojris",
                accessories = listOf("Embossed Lapel Pin", "Classic Watch / Bracelet"),
                colorPaletteNames = listOf("Royal Blue", "Pearl Cream", "Gold"),
                colorPaletteHexes = listOf("#4169E1", "#FFFFF0", "#FFD700"),
                weatherAdvice = "Breathable silk-cotton blend ensures ease of movement in ${temp}°C conditions.",
                stylingTips = "Pair with clean polished footwear to accentuate festive brightness."
            ),
            OutfitRecommendation(
                title = "Option 2: $topPrefix Contemporary Smart Casual",
                occasion = occ.ifBlank { "Party & Outing" },
                vibeTag = "$persona Smart Casual",
                targetPersona = persona,
                isCoupleOutfit = false,
                top = "$topPrefix Crisp Linen Cotton Shirt in Pastel Sage",
                bottom = "Dark Navy Slim Fit Chinos",
                outerwear = "Lightweight Unstructured Cardigan",
                footwear = "Minimalist Clean White Sneakers",
                accessories = listOf("Leather Belt", "UV Sunglasses"),
                colorPaletteNames = listOf("Pastel Sage", "Navy Blue", "Crisp White"),
                colorPaletteHexes = listOf("#9CAF88", "#000080", "#FFFFFF"),
                weatherAdvice = "Linen fabric permits maximum airflow for outdoor comfort.",
                stylingTips = "Roll sleeves once for an easygoing relaxed posture."
            ),
            OutfitRecommendation(
                title = "Option 3: $topPrefix High-Street Urban Layer",
                occasion = occ.ifBlank { "Night Party / Concert" },
                vibeTag = "$persona Urban Streetwear",
                targetPersona = persona,
                isCoupleOutfit = false,
                top = "$topPrefix Graphic Cotton Heavyweight Tee",
                bottom = "Distressed Black Denim Pants",
                outerwear = "Vintage Washed Blue Denim Trucker Jacket",
                footwear = "High-Top Retro Leather Sneakers",
                accessories = listOf("Stainless Steel Chain", "Crossbody Bag"),
                colorPaletteNames = listOf("Washed Blue", "Jet Black", "Graphic White"),
                colorPaletteHexes = listOf("#4682B4", "#000000", "#FFFFFF"),
                weatherAdvice = "Denim outer layer offers durable protection against night winds."
            ),
            OutfitRecommendation(
                title = "Option 4: $topPrefix Executive Formal Polish",
                occasion = occ.ifBlank { "Formal Gathering / Dinner" },
                vibeTag = "$persona Formal Elegance",
                targetPersona = persona,
                isCoupleOutfit = false,
                top = "$topPrefix Tailored Egyptian Cotton White Shirt",
                bottom = "Charcoal Gray Dress Trousers",
                outerwear = "Navy Blue Single-Breasted Blazer",
                footwear = "Cognac Leather Oxford Shoes",
                accessories = listOf("Matching Leather Belt", "Formal Watch"),
                colorPaletteNames = listOf("Navy Blue", "Pure White", "Charcoal Gray"),
                colorPaletteHexes = listOf("#1E2838", "#FFFFFF", "#36454F"),
                weatherAdvice = "Crisp cotton inner layer provides structured comfort indoors."
            ),
            OutfitRecommendation(
                title = "Option 5: $topPrefix Breezy Summer Resort Fit",
                occasion = occ.ifBlank { "Beach / Vacation / Weekend" },
                vibeTag = "$persona Vacation Casual",
                targetPersona = persona,
                isCoupleOutfit = false,
                top = "$topPrefix Tropical Leaf Print Resort Shirt",
                bottom = "Beige Linen Drawstring Shorts / Chinos",
                footwear = "Woven Leather Slip-On Loafers",
                accessories = listOf("Straw Hat", "Tortoiseshell Sunglasses"),
                colorPaletteNames = listOf("Tropical Sage", "Beige Sand", "Sun Gold"),
                colorPaletteHexes = listOf("#8FBC8F", "#F5F5DC", "#FFD700"),
                weatherAdvice = "Unstructured resort shirt keeps body cool during warm daytime strolls."
            )
        )
    }

    fun generateRecommendation(
        occasion: String,
        userQuery: String,
        weather: WeatherContext,
        style: StylePreference,
        wardrobe: List<WardrobeItem>
    ): OutfitRecommendation {
        return generate5Recommendations(occasion, userQuery, weather, style, wardrobe).first()
    }
}

