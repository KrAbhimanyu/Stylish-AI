package com.example.util

import android.content.Context
import android.graphics.*
import android.os.Environment
import com.example.data.ai.OutfitRecommendation
import com.example.data.local.SavedOutfit
import java.io.File
import java.io.FileOutputStream

object OutfitImageExporter {

    fun generateOutfitCardImage(context: Context, rec: OutfitRecommendation): String {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Paints
        val bgPaint = Paint().apply {
            color = Color.parseColor("#12141C")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Outer Card Container with soft gradient effect
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1E2230")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val cardRect = RectF(40f, 40f, (width - 40).toFloat(), (height - 40).toFloat())
        canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)

        // Card Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#343A4F")
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, borderPaint)

        // Header Accent Strip
        val accentPaint = Paint().apply {
            color = Color.parseColor("#6366F1") // Indigo Accent
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(80f, 80f, 420f, 130f), 20f, 20f, accentPaint)

        // Header Text
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("OUTFIT LOOKBOOK", 100f, 115f, headerTextPaint)

        // Vibe Badge
        val vibeBgPaint = Paint().apply {
            color = Color.parseColor("#2E354A")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(440f, 80f, 750f, 130f), 20f, 20f, vibeBgPaint)

        val vibeTextPaint = Paint().apply {
            color = Color.parseColor("#A5B4FC")
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(rec.vibeTag.uppercase(), 460f, 114f, vibeTextPaint)

        // Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 52f
            isFakeBoldText = true
            isAntiAlias = true
        }
        
        // Truncate title if too long
        val displayTitle = if (rec.title.length > 32) rec.title.take(30) + "..." else rec.title
        canvas.drawText(displayTitle, 80f, 210f, titlePaint)

        // Occasion Subtitle
        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 30f
            isAntiAlias = true
        }
        canvas.drawText("Occasion: ${rec.occasion} • Comfort Score: ${rec.weatherComfortScore}%", 80f, 255f, subtitlePaint)

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#2E354D")
            strokeWidth = 3f
        }
        canvas.drawLine(80f, 290f, (width - 80).toFloat(), 290f, dividerPaint)

        // Visual Avatar Representation Section
        var currentY = 340f
        val avatarBgPaint = Paint().apply {
            color = Color.parseColor("#172033")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val avatarRect = RectF(80f,currentY, (width - 80).toFloat(), currentY + 110f)
        canvas.drawRoundRect(avatarRect, 24f, 24f, avatarBgPaint)

        val avatarBorder = Paint().apply {
            color = Color.parseColor("#3B82F6")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(avatarRect, 24f, 24f, avatarBorder)

        if (rec.isCoupleOutfit) {
            // Draw Groom Avatar Circle
            val p1CirclePaint = Paint().apply { color = Color.parseColor("#2563EB"); style = Paint.Style.FILL; isAntiAlias = true }
            canvas.drawCircle(130f, currentY + 55f, 35f, p1CirclePaint)
            val p1TextPaint = Paint().apply { color = Color.WHITE; textSize = 26f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText("👨", 112f, currentY + 65f, p1TextPaint)

            val p1LabelPaint = Paint().apply { color = Color.WHITE; textSize = 26f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText("Husband Look", 180f, currentY + 48f, p1LabelPaint)
            val p1SubLabelPaint = Paint().apply { color = Color.parseColor("#93C5FD"); textSize = 20f; isAntiAlias = true }
            canvas.drawText("Style Coordinated", 180f, currentY + 78f, p1SubLabelPaint)

            // Draw Bride Avatar Circle
            val p2CirclePaint = Paint().apply { color = Color.parseColor("#D946EF"); style = Paint.Style.FILL; isAntiAlias = true }
            canvas.drawCircle(580f, currentY + 55f, 35f, p2CirclePaint)
            canvas.drawText("👩", 562f, currentY + 65f, p1TextPaint)

            canvas.drawText("Wife Look", 630f, currentY + 48f, p1LabelPaint)
            val p2SubLabelPaint = Paint().apply { color = Color.parseColor("#F0ABFC"); textSize = 20f; isAntiAlias = true }
            canvas.drawText("Style Coordinated", 630f, currentY + 78f, p2SubLabelPaint)
        } else {
            val emoji = when (rec.targetPersona) {
                "Boy" -> "👦"
                "Girl" -> "👧"
                "Kid" -> "👶"
                "Wife" -> "👩"
                "Husband" -> "👨"
                else -> "✨"
            }
            val circlePaint = Paint().apply { color = Color.parseColor("#2563EB"); style = Paint.Style.FILL; isAntiAlias = true }
            canvas.drawCircle(130f, currentY + 55f, 35f, circlePaint)
            val p1TextPaint = Paint().apply { color = Color.WHITE; textSize = 26f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText(emoji, 112f, currentY + 65f, p1TextPaint)

            val p1LabelPaint = Paint().apply { color = Color.WHITE; textSize = 28f; isFakeBoldText = true; isAntiAlias = true }
            canvas.drawText("AI Stylist Avatar • ${rec.targetPersona}", 180f, currentY + 50f, p1LabelPaint)
            val p1SubLabelPaint = Paint().apply { color = Color.parseColor("#93C5FD"); textSize = 22f; isAntiAlias = true }
            canvas.drawText("Customized specifically for your preferences", 180f, currentY + 82f, p1SubLabelPaint)
        }

        currentY += 135f

        // Section: Outfit Components
        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#818CF8")
            textSize = 28f
            isFakeBoldText = true
            letterSpacing = 0.08f
            isAntiAlias = true
        }
        canvas.drawText("GARMENT BREAKDOWN", 80f, currentY, sectionTitlePaint)
        currentY += 45f

        val itemLabelPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 26f
            isAntiAlias = true
        }

        val itemValPaint = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            isFakeBoldText = true
            isAntiAlias = true
        }

        fun drawComponentRow(label: String, valText: String) {
            if (valText.isBlank()) return
            canvas.drawText(label, 80f, currentY, itemLabelPaint)
            currentY += 38f
            val truncatedVal = if (valText.length > 42) valText.take(40) + "..." else valText
            canvas.drawText(truncatedVal, 80f, currentY, itemValPaint)
            currentY += 55f
        }

        if (rec.isCoupleOutfit && rec.partner1 != null && rec.partner2 != null) {
            canvas.drawText("PARTNER 1: ${rec.partner1.partnerTitle.uppercase()}", 80f, currentY, sectionTitlePaint)
            currentY += 35f
            drawComponentRow("TOP / ETHNIC WEAR", rec.partner1.top)
            drawComponentRow("BOTTOM / TROUSERS", rec.partner1.bottom)
            if (rec.partner1.outerwear.isNotBlank()) drawComponentRow("OUTERWEAR", rec.partner1.outerwear)
            drawComponentRow("FOOTWEAR", rec.partner1.footwear)

            canvas.drawText("PARTNER 2: ${rec.partner2.partnerTitle.uppercase()}", 80f, currentY, sectionTitlePaint)
            currentY += 35f
            drawComponentRow("TOP / LEHENGA / DRESS", rec.partner2.top)
            drawComponentRow("BOTTOM / SKIRT", rec.partner2.bottom)
            if (rec.partner2.outerwear.isNotBlank()) drawComponentRow("DUPATTA / LAYER", rec.partner2.outerwear)
            drawComponentRow("FOOTWEAR", rec.partner2.footwear)
        } else {
            drawComponentRow("TOP / UPPERWEAR", rec.top)
            drawComponentRow("BOTTOM / TROUSERS", rec.bottom)
            if (rec.outerwear.isNotBlank()) {
                drawComponentRow("OUTERWEAR / LAYER", rec.outerwear)
            }
            drawComponentRow("FOOTWEAR", rec.footwear)
            if (rec.accessories.isNotEmpty()) {
                drawComponentRow("ACCESSORIES", rec.accessories.joinToString(", "))
            }
        }

        currentY += 10f
        canvas.drawLine(80f, currentY, (width - 80).toFloat(), currentY, dividerPaint)
        currentY += 40f

        // Color Palette Swatches
        if (rec.colorPaletteHexes.isNotEmpty()) {
            canvas.drawText("COLOR PALETTE SWATCHES", 80f, currentY, sectionTitlePaint)
            currentY += 45f

            var swatchX = 80f
            rec.colorPaletteHexes.take(5).forEachIndexed { idx, hexStr ->
                val swatchPaint = Paint().apply {
                    color = try {
                        Color.parseColor(hexStr)
                    } catch (e: Exception) {
                        Color.parseColor("#6366F1")
                    }
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }

                // Draw circle swatch
                canvas.drawCircle(swatchX + 35f, currentY + 35f, 35f, swatchPaint)

                // Draw border for swatch
                val swatchBorder = Paint().apply {
                    color = Color.parseColor("#475569")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    isAntiAlias = true
                }
                canvas.drawCircle(swatchX + 35f, currentY + 35f, 35f, swatchBorder)

                swatchX += 100f
            }

            if (rec.colorPaletteNames.isNotEmpty()) {
                val paletteNamesText = rec.colorPaletteNames.joinToString(" • ")
                val pTextPaint = Paint().apply {
                    color = Color.parseColor("#CBD5E1")
                    textSize = 24f
                    isAntiAlias = true
                }
                canvas.drawText(paletteNamesText, 80f, currentY + 115f, pTextPaint)
                currentY += 135f
            } else {
                currentY += 90f
            }
        }

        currentY += 10f
        canvas.drawLine(80f, currentY, (width - 80).toFloat(), currentY, dividerPaint)
        currentY += 40f

        // Weather Comfort Box
        if (rec.weatherAdvice.isNotBlank()) {
            val boxPaint = Paint().apply {
                color = Color.parseColor("#172033")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val boxRect = RectF(80f, currentY, (width - 80).toFloat(), currentY + 130f)
            canvas.drawRoundRect(boxRect, 24f, 24f, boxPaint)

            val boxBorder = Paint().apply {
                color = Color.parseColor("#2563EB")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(boxRect, 24f, 24f, boxBorder)

            val adviceHeaderPaint = Paint().apply {
                color = Color.parseColor("#60A5FA")
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("WEATHER COMFORT ADVISORY", 105f, currentY + 45f, adviceHeaderPaint)

            val adviceBodyPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                textSize = 22f
                isAntiAlias = true
            }
            val shortAdvice = if (rec.weatherAdvice.length > 65) rec.weatherAdvice.take(62) + "..." else rec.weatherAdvice
            canvas.drawText(shortAdvice, 105f, currentY + 88f, adviceBodyPaint)

            currentY += 160f
        }

        // Footer / Watermark
        val footerBg = Paint().apply {
            color = Color.parseColor("#181B26")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val footerRect = RectF(40f, (height - 120).toFloat(), (width - 40).toFloat(), (height - 40).toFloat())
        canvas.drawRoundRect(footerRect, 32f, 32f, footerBg)

        val footerTextPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 24f
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText("✨ GENERATED BY AI OUTFIT STYLIST • LOCAL GALLERY IMAGE", 80f, (height - 70).toFloat(), footerTextPaint)

        // Save bitmap to file
        return saveBitmapToFile(context, bitmap, "outfit_${System.currentTimeMillis()}.png")
    }

    fun generateImageFromSavedOutfit(context: Context, outfit: SavedOutfit): String {
        val dummyRec = OutfitRecommendation(
            title = outfit.title,
            occasion = outfit.occasion,
            top = outfit.topItem,
            bottom = outfit.bottomItem,
            outerwear = outfit.outerwearItem,
            footwear = outfit.footwearItem,
            accessories = if (outfit.accessoryItems.isNotBlank()) outfit.accessoryItems.split(",").map { it.trim() } else emptyList(),
            vibeTag = outfit.occasion,
            colorPaletteNames = if (outfit.colorPalette.isNotBlank()) outfit.colorPalette.split("+").map { it.trim() } else listOf("Neutral"),
            colorPaletteHexes = listOf("#3B82F6", "#10B981", "#F59E0B", "#EF4444"),
            weatherAdvice = outfit.weatherComfortReason,
            stylingTips = outfit.stylingTips,
            weatherComfortScore = 95
        )
        return generateOutfitCardImage(context, dummyRec)
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): String {
        val storageDir = File(context.filesDir, "saved_outfits")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val file = File(storageDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }
}
