package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyOutfitWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, DailyOutfitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.outfit_widget_layout)

            // PendingIntent to open Lookbook Gallery
            val galleryIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "lookbook")
            }
            val galleryPendingIntent = PendingIntent.getActivity(
                context,
                101,
                galleryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_gallery, galleryPendingIntent)

            // PendingIntent to open Stylist AI Home
            val stylistIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "stylist")
            }
            val stylistPendingIntent = PendingIntent.getActivity(
                context,
                102,
                stylistIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_stylist, stylistPendingIntent)

            // PendingIntent on root view click -> Open app
            views.setOnClickPendingIntent(R.id.widget_root_container, galleryPendingIntent)

            // Asynchronously fetch latest saved outfit
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val outfit = db.outfitDao().getLatestSavedOutfitSync()

                    if (outfit != null) {
                        views.setTextViewText(R.id.widget_text_title, outfit.title)
                        views.setTextViewText(
                            R.id.widget_text_occasion,
                            "Occasion: ${outfit.occasion} • ${if (outfit.colorPalette.isNotBlank()) outfit.colorPalette else "Aura Style"}"
                        )
                        views.setTextViewText(
                            R.id.widget_text_top,
                            "👕 Top: ${outfit.topItem.ifBlank { "Tailored Top" }}"
                        )
                        views.setTextViewText(
                            R.id.widget_text_bottom,
                            "👖 Bottom: ${outfit.bottomItem.ifBlank { "Fitted Bottom" }}"
                        )
                        views.setTextViewText(
                            R.id.widget_text_footwear,
                            "👟 Shoes: ${outfit.footwearItem.ifBlank { "Matching Footwear" }}"
                        )
                    } else {
                        views.setTextViewText(R.id.widget_text_title, "Royal Velvet Sangeet Look")
                        views.setTextViewText(R.id.widget_text_occasion, "Occasion: Marriage • Ethnic Chic")
                        views.setTextViewText(R.id.widget_text_top, "👕 Top: Silk Blend Cream Kurta")
                        views.setTextViewText(R.id.widget_text_bottom, "👖 Bottom: Slim Churidar Trousers")
                        views.setTextViewText(R.id.widget_text_footwear, "👟 Shoes: Handcrafted Gold Mojris")
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        fun sendWidgetUpdateBroadcast(context: Context) {
            val intent = Intent(context, DailyOutfitWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }
}
