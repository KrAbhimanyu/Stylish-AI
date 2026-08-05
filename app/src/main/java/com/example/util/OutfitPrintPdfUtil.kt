package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.local.SavedOutfit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OutfitPrintPdfUtil {

    fun exportSavedOutfitToPdf(context: Context, outfit: SavedOutfit) {
        try {
            val htmlContent = buildHtmlDocumentForOutfit(outfit)
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    val printAdapter = webView.createPrintDocumentAdapter("Outfit_${outfit.id}_Lookbook")
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    val jobName = "AI Lookbook - ${outfit.title}"
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                        .setMinMargins(PrintAttributes.Margins.ZERO)
                        .build()

                    printManager.print(jobName, printAdapter, printAttributes)
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            Toast.makeText(context, "📄 Preparing PDF document for printing/export...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "PDF Export error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildHtmlDocumentForOutfit(outfit: SavedOutfit): String {
        val formattedDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 36px; color: #0F172A; background: #FFFFFF; }
                    .header { border-bottom: 3px solid #4F46E5; padding-bottom: 14px; margin-bottom: 24px; }
                    .title { font-size: 26px; font-weight: 800; color: #3730A3; margin: 0; }
                    .subtitle { font-size: 13px; color: #64748B; margin-top: 6px; font-weight: 500; }
                    .card { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; padding: 18px; margin-bottom: 18px; }
                    .section-title { font-size: 13px; font-weight: 800; color: #4F46E5; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; }
                    .item-row { display: flex; justify-content: space-between; font-size: 13px; padding: 8px 0; border-bottom: 1px dashed #CBD5E1; }
                    .item-row:last-child { border-bottom: none; }
                    .label { font-weight: 700; color: #334155; }
                    .val { color: #0F172A; font-weight: 500; }
                    .badge { display: inline-block; background: #EEF2FF; color: #3730A3; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 700; }
                    .footer { text-align: center; font-size: 11px; color: #94A3B8; margin-top: 40px; border-top: 1px solid #E2E8F0; padding-top: 14px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="title">👗 ${outfit.title}</h1>
                    <div class="subtitle">AI Personal Stylist • Lookbook Sheet • Occasion: <span class="badge">${outfit.occasion}</span></div>
                </div>

                <div class="card">
                    <div class="section-title">Garment & Outfit Composition</div>
                    <div class="item-row"><span class="label">Top Piece:</span><span class="val">${outfit.topItem}</span></div>
                    <div class="item-row"><span class="label">Bottom Piece:</span><span class="val">${outfit.bottomItem}</span></div>
                    ${if (outfit.outerwearItem.isNotBlank()) "<div class=\"item-row\"><span class=\"label\">Outerwear Layer:</span><span class=\"val\">${outfit.outerwearItem}</span></div>" else ""}
                    <div class="item-row"><span class="label">Footwear:</span><span class="val">${outfit.footwearItem}</span></div>
                    ${if (outfit.accessories.isNotBlank()) "<div class=\"item-row\"><span class=\"label\">Matching Accessories:</span><span class=\"val\">${outfit.accessories}</span></div>" else ""}
                </div>

                <div class="card">
                    <div class="section-title">Styling Guidelines & Advice</div>
                    <p style="font-size: 13px; line-height: 1.6; color: #334155; margin-bottom: 8px;"><strong>✨ Styling Tip:</strong> ${outfit.stylingTips}</p>
                    ${if (outfit.groomingAdvice.isNotBlank()) "<p style=\"font-size: 13px; line-height: 1.6; color: #334155; margin-bottom: 8px;\"><strong>💈 Grooming Notes:</strong> ${outfit.groomingAdvice}</p>" else ""}
                    ${if (outfit.weatherAdvice.isNotBlank()) "<p style=\"font-size: 13px; line-height: 1.6; color: #334155;\"><strong>🌤️ Weather Advice:</strong> ${outfit.weatherAdvice}</p>" else ""}
                </div>

                <div class="card">
                    <div class="section-title">Color Palette & User Rating</div>
                    <p style="font-size: 13px; margin: 4px 0;"><strong>🎨 Color Palette:</strong> ${outfit.colorPalette}</p>
                    <p style="font-size: 13px; margin: 4px 0;"><strong>⭐ Personal Score:</strong> ${outfit.userRating} / 5 Stars</p>
                </div>

                <div class="footer">
                    Exported from Google AI Studio Personal Stylist • Printed on $formattedDate
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
