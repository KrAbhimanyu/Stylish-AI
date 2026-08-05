package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.WeatherContext

@Composable
fun WeatherContextBar(
    weather: WeatherContext,
    onUpdateWeather: (WeatherContext) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val weatherGradient = when {
        weather.temperatureCelsius >= 30 -> Brush.horizontalGradient(listOf(Color(0xFF802011), Color(0xFFC86D51)))
        weather.condition.lowercase().contains("rain") -> Brush.horizontalGradient(listOf(Color(0xFF1B2A4A), Color(0xFF3B5998)))
        weather.temperatureCelsius < 18 -> Brush.horizontalGradient(listOf(Color(0xFF283850), Color(0xFF4A6B82)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF1E2838), Color(0xFF2E3B4E)))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { showDialog = true }
            .testTag("weather_context_bar"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(weatherGradient)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when {
                            weather.condition.lowercase().contains("rain") -> Icons.Outlined.WaterDrop
                            weather.temperatureCelsius < 15 -> Icons.Outlined.AcUnit
                            else -> Icons.Default.WbSunny
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Weather Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${weather.temperatureCelsius}°C",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = weather.condition,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = weather.location,
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Weather",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Change",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        WeatherEditDialog(
            currentWeather = weather,
            onDismiss = { showDialog = false },
            onSave = { updated ->
                onUpdateWeather(updated)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeatherEditDialog(
    currentWeather: WeatherContext,
    onDismiss: () -> Unit,
    onSave: (WeatherContext) -> Unit
) {
    var temp by remember { mutableStateOf(currentWeather.temperatureCelsius.toString()) }
    var condition by remember { mutableStateOf(currentWeather.condition) }
    var location by remember { mutableStateOf(currentWeather.location) }

    val presetConditions = listOf("Sunny & Clear", "Pleasant & Clear", "Cool Evening", "Rainy / Damp", "Hot & Humid", "Crisp Cold")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Current Weather", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = temp,
                    onValueChange = { temp = it },
                    label = { Text("Temperature (°C)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / City") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Condition Preset:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    presetConditions.forEach { cond ->
                        FilterChip(
                            selected = condition == cond,
                            onClick = { condition = cond },
                            label = { Text(cond, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tempInt = temp.toIntOrNull() ?: 24
                    onSave(currentWeather.copy(temperatureCelsius = tempInt, condition = condition, location = location))
                }
            ) {
                Text("Save Weather Context")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
