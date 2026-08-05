package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.Nightlife
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OccasionOption(
    val title: String,
    val icon: ImageVector,
    val defaultPrompt: String
)

val defaultOccasions = listOf(
    OccasionOption("Marriage / Wedding", Icons.Default.Celebration, "I'm going to a grand marriage ceremony. Suggest a stunning, festive outfit."),
    OccasionOption("Party / Clubbing", Icons.Default.Nightlife, "I want an edgy, stylish outfit for an evening party or club night."),
    OccasionOption("Casual Outing", Icons.Default.Diversity1, "Suggest a breezy, comfortable casual outfit for a day out with friends."),
    OccasionOption("Business / Pitch", Icons.Default.Work, "Suggest a sharp, executive formal look for an important business presentation."),
    OccasionOption("Date Night", Icons.Default.Favorite, "I'm going on a romantic date night. Suggest an impressive, sophisticated outfit."),
    OccasionOption("Gym / Activewear", Icons.Default.FitnessCenter, "Suggest a high-performance activewear outfit for workout."),
    OccasionOption("Vacation / Beach", Icons.Default.BeachAccess, "Suggest a relaxed, stylish resort wear outfit for vacation.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OccasionChipSelector(
    selectedOccasion: String,
    onOccasionSelected: (OccasionOption) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Select Occasion / Event",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultOccasions.forEach { option ->
                val isSelected = selectedOccasion.contains(option.title.split(" ").first(), ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { onOccasionSelected(option) },
                    label = {
                        Text(
                            text = option.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Check else option.icon,
                            contentDescription = option.title,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("occasion_chip_${option.title.lowercase().replace(" ", "_")}")
                )
            }
        }
    }
}
