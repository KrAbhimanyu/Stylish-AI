package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.SavedOutfit
import java.io.File

object OutfitShareUtil {

    fun shareOutfitCardImage(context: Context, outfit: SavedOutfit) {
        try {
            var imagePath = outfit.imagePath
            var imageFile = if (imagePath.isNotBlank()) File(imagePath) else null

            // Generate image if file doesn't exist
            if (imageFile == null || !imageFile.exists()) {
                imagePath = OutfitImageExporter.generateImageFromSavedOutfit(context, outfit)
                imageFile = File(imagePath)
            }

            if (!imageFile.exists()) {
                Toast.makeText(context, "Could not generate outfit image for sharing.", Toast.LENGTH_SHORT).show()
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, imageFile)

            val shareMessage = """
                ✨ AI Outfit Lookbook Recommendation ✨
                Look: ${outfit.title}
                Occasion: ${outfit.occasion}
                Top: ${outfit.topItem}
                Bottom: ${outfit.bottomItem}
                Footwear: ${outfit.footwearItem}
                ${if (outfit.colorPalette.isNotBlank()) "Palette: ${outfit.colorPalette}" else ""}
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Outfit Recommendation: ${outfit.title}")
                putExtra(Intent.EXTRA_TEXT, shareMessage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Outfit Card via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing outfit image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePackingList(context: Context, packingList: com.example.data.ai.PackingList) {
        try {
            val sb = StringBuilder()
            sb.append("🧳 TRIP PACKING LIST: ").append(packingList.tripTitle).append("\n")
            sb.append("Outfits Included: ").append(packingList.selectedOutfitCount).append("\n")
            sb.append("Packing Progress: ").append(packingList.packedItemCount).append("/").append(packingList.totalItemCount).append(" items\n\n")

            packingList.categories.forEach { category ->
                sb.append(category.categoryName.uppercase()).append(":\n")
                category.items.forEach { item ->
                    val status = if (item.isPacked) "[x]" else "[ ]"
                    sb.append("  ").append(status).append(" ").append(item.name)
                    if (item.sourceOutfitTitle.isNotBlank()) {
                        sb.append(" (for ").append(item.sourceOutfitTitle).append(")")
                    }
                    sb.append("\n")
                }
                sb.append("\n")
            }

            if (packingList.packingTips.isNotEmpty()) {
                sb.append("💡 SMART PACKING TIPS:\n")
                packingList.packingTips.forEach { tip ->
                    sb.append("• ").append(tip).append("\n")
                }
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Trip Packing List: ${packingList.tripTitle}")
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }

            val chooser = Intent.createChooser(shareIntent, "Share Trip Packing List via")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Toast.makeText(context, "Packing List prepared for sharing!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing packing list: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
