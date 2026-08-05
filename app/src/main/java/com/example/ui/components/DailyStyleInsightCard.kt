package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.StylePreference
import java.util.Calendar

@Composable
fun DailyStyleInsightCard(
    stylePreference: StylePreference?,
    onApplyTipToOutfit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tipIndex by remember { mutableIntStateOf(0) }

    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }
    val seasonName = remember(currentMonth) {
        when (currentMonth) {
            in 2..4 -> "Spring / Warm Sun"
            in 5..7 -> "Summer & High Humidity"
            in 8..9 -> "Monsoon / Transitional"
            else -> "Winter / Cool Breeze"
        }
    }

    val preferredVibe = stylePreference?.primaryVibe ?: "Ethnic Chic & Modern"
    val preferredFit = stylePreference?.preferredFit ?: "Tailored Regular"
    val preferredPalette = stylePreference?.colorPreference ?: "Rich & Warm Neutrals"

    val dailyTips = remember(preferredVibe, preferredFit, preferredPalette, seasonName) {
        listOf(
            "Season Focus ($seasonName): Opt for lightweight, moisture-wicking natural linens and unlined blazers. For your $preferredVibe preference, pair warm neutral tones with a subtle rose-gold timepiece.",
            "Fit Pro-Tip ($preferredFit): Structured shoulders on soft Italian linen jackets create a sharper silhouette without trapping heat. Pair with tapered $preferredFit trousers.",
            "Color Harmony ($preferredPalette): Combine $preferredPalette with crisp ivory accents to accentuate warm skin tones during $seasonName evening events.",
            "Footwear Insight: Hand-burnished tan monk straps or breathable woven loafers complement both traditional festive kurtas and tailored trousers seamlessly."
        )
    }

    val currentTip = dailyTips[tipIndex % dailyTips.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_style_insight_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DAILY STYLE INSIGHT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$seasonName • $preferredVibe",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                IconButton(
                    onClick = { tipIndex++ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Next Tip",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentTip,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onApplyTipToOutfit(currentTip) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("apply_daily_insight_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Tip to Outfit Generator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
