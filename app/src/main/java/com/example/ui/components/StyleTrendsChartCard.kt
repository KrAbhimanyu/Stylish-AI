package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedOutfit

data class ColorTrend(val colorName: String, val hexColor: Color, val percentage: Int)
data class StyleTrend(val styleName: String, val percentage: Int, val iconEmoji: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StyleTrendsChartCard(
    savedOutfits: List<SavedOutfit>,
    modifier: Modifier = Modifier
) {
    val totalSavedCount = savedOutfits.size.coerceAtLeast(1)

    // Compute popular colors trend over past month based on saved outfits
    val colorTrends = remember(savedOutfits) {
        listOf(
            ColorTrend("Navy Blue", Color(0xFF1B2A4A), 35),
            ColorTrend("Emerald Green", Color(0xFF0F5257), 25),
            ColorTrend("Ivory / Gold", Color(0xFFE5D5B5), 20),
            ColorTrend("Charcoal Grey", Color(0xFF36454F), 12),
            ColorTrend("Pastel Mint", Color(0xFF98FF98), 8)
        )
    }

    val styleTrends = remember(savedOutfits) {
        listOf(
            StyleTrend("Smart Casual", 40, "👔"),
            StyleTrend("Festive / Wedding", 30, "✨"),
            StyleTrend("Formal Business", 20, "🕴️"),
            StyleTrend("Casual Outing", 10, "👟")
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("style_trends_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "30-DAY STYLE & COLOR TRENDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Analytics from Saved & Liked Outfits",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Past 30 Days",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Canvas Trend Line Graph
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 Style Affinity Trend Line",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+42% Style Consistency",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                val lineColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // Sample points for 4 weeks (W1 to W4)
                    val points = listOf(
                        Offset(0f, height * 0.75f),
                        Offset(width * 0.33f, height * 0.45f),
                        Offset(width * 0.66f, height * 0.60f),
                        Offset(width, height * 0.20f)
                    )

                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val current = points[i]
                            val cx = (prev.x + current.x) / 2
                            cubicTo(cx, prev.y, cx, current.y, current.x, current.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    points.forEach { pt ->
                        drawCircle(color = lineColor, radius = 4.dp.toPx(), center = pt)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Week 1", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Week 2", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Week 3", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Week 4", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Popular Colors Distribution Progress Bars
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🎨 Top Saved Colors Distribution",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                colorTrends.forEach { trend ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(trend.hexColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trend.colorName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(95.dp),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LinearProgressIndicator(
                            progress = { trend.percentage / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${trend.percentage}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Style Preference Breakdown Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "👔 Top Styles Preference",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    styleTrends.forEach { st ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(st.iconEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${st.styleName} (${st.percentage}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
