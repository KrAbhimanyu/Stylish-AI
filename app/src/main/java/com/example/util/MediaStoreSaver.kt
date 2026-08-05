package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

object MediaStoreSaver {
    suspend fun saveOutfitImageToGallery(
        context: Context,
        imageSource: String,
        title: String,
        fallbackBitmap: Bitmap? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmapToSave: Bitmap? = when {
                fallbackBitmap != null -> fallbackBitmap
                imageSource.startsWith("http://") || imageSource.startsWith("https://") -> {
                    val url = URL(imageSource)
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                }
                imageSource.isNotBlank() && File(imageSource).exists() -> {
                    BitmapFactory.decodeFile(imageSource)
                }
                else -> null
            } ?: return@withContext false

            val sanitizeTitle = title.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val filename = "AI_Outfit_${sanitizeTitle}_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AI_Outfit_Stylist")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false

            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmapToSave?.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
