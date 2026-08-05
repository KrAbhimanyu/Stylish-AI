package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 24.dp,
    activeColor: Color = Color(0xFFFFB800),
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Row(
        modifier = modifier.testTag("star_rating_bar"),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { starIndex ->
            val isFilled = starIndex <= rating
            Icon(
                imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = "Rate $starIndex stars",
                tint = if (isFilled) activeColor else inactiveColor,
                modifier = Modifier
                    .size(starSize)
                    .clickable { onRatingSelected(starIndex) }
                    .padding(1.dp)
            )
        }
    }
}
