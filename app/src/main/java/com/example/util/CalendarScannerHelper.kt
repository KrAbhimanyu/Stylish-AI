package com.example.util

import android.content.Context
import android.provider.CalendarContract
import com.example.data.local.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CalendarScannerHelper {

    fun scanDeviceCalendarForDressCodes(context: Context): List<CalendarEvent> {
        val detectedEvents = mutableListOf<CalendarEvent>()
        
        try {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART
            )

            val selection = "${CalendarContract.Events.DTSTART} >= ?"
            val selectionArgs = arrayOf(System.currentTimeMillis().toString())
            val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use {
                val titleIdx = it.getColumnIndex(CalendarContract.Events.TITLE)
                val descIdx = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val locIdx = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                val dateIdx = it.getColumnIndex(CalendarContract.Events.DTSTART)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                while (it.moveToNext() && detectedEvents.size < 8) {
                    val title = if (titleIdx >= 0) it.getString(titleIdx) ?: "Upcoming Event" else "Upcoming Event"
                    val description = if (descIdx >= 0) it.getString(descIdx) ?: "" else ""
                    val location = if (locIdx >= 0) it.getString(locIdx) ?: "Venue" else "Venue"
                    val startTime = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis()

                    val dressCode = extractDressCodeFromDescription(title, description)
                    val occasionType = categorizeOccasion(title, description)

                    detectedEvents.add(
                        CalendarEvent(
                            title = title,
                            occasionType = occasionType,
                            date = dateFormat.format(Date(startTime)),
                            time = timeFormat.format(Date(startTime)),
                            location = location,
                            dressCode = dressCode
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback sample calendar events with rich dress codes if calendar is empty or permissions freshly granted
        if (detectedEvents.isEmpty()) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            detectedEvents.addAll(
                listOf(
                    CalendarEvent(
                        title = "Royal Sangeet & Mehendi Celebration",
                        occasionType = "Marriage / Wedding",
                        date = dateFormat.format(Date(now + 86400000L * 3)),
                        time = "18:30",
                        location = "Taj Palace Grand Ballroom",
                        dressCode = "Festive Ethnic Silk / Royal Kurta or Lehenga"
                    ),
                    CalendarEvent(
                        title = "Annual Executive Leadership Summit",
                        occasionType = "Business / Meeting",
                        date = dateFormat.format(Date(now + 86400000L * 7)),
                        time = "09:00",
                        location = "ITC Maurya Conference Center",
                        dressCode = "Business Formal Tuxedo & Italian Blazer"
                    ),
                    CalendarEvent(
                        title = "Sunset Rooftop Cocktail Party",
                        occasionType = "Party / Clubbing",
                        date = dateFormat.format(Date(now + 86400000L * 10)),
                        time = "20:00",
                        location = "Skyline Lounge & Terrace",
                        dressCode = "Glamorous Evening Dress Code & Smart Jackets"
                    )
                )
            )
        }

        return detectedEvents
    }

    private fun categorizeOccasion(title: String, description: String): String {
        val text = "$title $description".lowercase()
        return when {
            text.contains("wedding") || text.contains("sangeet") || text.contains("marriage") -> "Marriage / Wedding"
            text.contains("business") || text.contains("summit") || text.contains("meeting") || text.contains("conference") -> "Business / Meeting"
            text.contains("party") || text.contains("cocktail") || text.contains("club") -> "Party / Clubbing"
            text.contains("date") || text.contains("dinner") -> "Date Night"
            text.contains("gym") || text.contains("workout") -> "Gym / Fitness"
            else -> "Casual Outing"
        }
    }

    private fun extractDressCodeFromDescription(title: String, description: String): String {
        val text = "$title $description".lowercase()
        val regexPatterns = listOf(
            Regex("dress\\s*code[:\\s]+([a-zA-Trigger-9\\s,/-]+)", RegexOption.IGNORE_CASE),
            Regex("attire[:\\s]+([a-zA-Trigger-9\\s,/-]+)", RegexOption.IGNORE_CASE),
            Regex("wear[:\\s]+([a-zA-Trigger-9\\s,/-]+)", RegexOption.IGNORE_CASE)
        )

        for (regex in regexPatterns) {
            val match = regex.find(description)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].take(30).trim()
            }
        }

        return when {
            text.contains("wedding") || text.contains("sangeet") || text.contains("marriage") -> "Festive Traditional / Ethnic Silk"
            text.contains("business") || text.contains("summit") || text.contains("meeting") -> "Corporate Business Formal"
            text.contains("party") || text.contains("cocktail") || text.contains("club") -> "Glam Nightlife / High Street Chic"
            text.contains("beach") || text.contains("resort") || text.contains("vacation") -> "Tropical Resort Casual"
            else -> "Smart Casual & Tailored"
        }
    }

    fun pushOutfitToCalendar(
        context: Context,
        eventTitle: String,
        outfitTitle: String,
        topItem: String,
        bottomItem: String,
        footwearItem: String,
        accessoriesItem: String,
        stylingTips: String,
        eventDate: String = "",
        location: String = "Venue"
    ): Boolean {
        return try {
            val description = """
                👔 AI Outfit Suggestion: $outfitTitle
                ----------------------------------------
                👕 Top: $topItem
                👖 Bottom: $bottomItem
                👟 Footwear: $footwearItem
                ✨ Accessories: $accessoriesItem
                💡 Styling Tip: $stylingTips
            """.trimIndent()

            val intent = android.content.Intent(android.content.Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Outfit for: $eventTitle ($outfitTitle)")
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
