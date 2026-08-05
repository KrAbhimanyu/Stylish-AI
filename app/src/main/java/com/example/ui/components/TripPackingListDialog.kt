package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ai.PackingList
import com.example.data.local.SavedOutfit
import com.example.ui.viewmodel.StylistViewModel
import com.example.util.OutfitShareUtil

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TripPackingListDialog(
    savedOutfits: List<SavedOutfit>,
    packingList: PackingList?,
    viewModel: StylistViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tripTitleInput by remember { mutableStateOf("Upcoming Trip Packing List") }
    var selectedOutfitIds by remember {
        mutableStateOf(savedOutfits.map { it.id }.toSet())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .testTag("trip_packing_list_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardTravel,
                                contentDescription = "Trip Packing List",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Trip Packing List",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (packingList != null) "${packingList.packedItemCount} / ${packingList.totalItemCount} packed" else "Extract items from saved outfits",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (packingList == null) {
                    // STEP 1: Select Outfits for Trip
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = tripTitleInput,
                            onValueChange = { tripTitleInput = it },
                            label = { Text("Trip / Event Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Saved Outfits (${selectedOutfitIds.size}/${savedOutfits.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            TextButton(
                                onClick = {
                                    selectedOutfitIds = if (selectedOutfitIds.size == savedOutfits.size) emptySet() else savedOutfits.map { it.id }.toSet()
                                }
                            ) {
                                Text(if (selectedOutfitIds.size == savedOutfits.size) "Deselect All" else "Select All", fontSize = 12.sp)
                            }
                        }

                        if (savedOutfits.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved outfits found. Save outfits to Lookbook first!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(savedOutfits, key = { it.id }) { outfit ->
                                    val isSelected = selectedOutfitIds.contains(outfit.id)
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedOutfitIds = if (isSelected) {
                                                    selectedOutfitIds - outfit.id
                                                } else {
                                                    selectedOutfitIds + outfit.id
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { checked ->
                                                    selectedOutfitIds = if (checked) selectedOutfitIds + outfit.id else selectedOutfitIds - outfit.id
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(outfit.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                Text("Occasion: ${outfit.occasion} • ${outfit.topItem}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val selectedList = savedOutfits.filter { selectedOutfitIds.contains(it.id) }
                                viewModel.generatePackingList(selectedList, tripTitleInput)
                            },
                            enabled = selectedOutfitIds.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_packing_list_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Packing List from Outfits", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // STEP 2: Display Generated Categorized Packing List
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Trip Title & Progress Indicator
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = packingList.tripTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (packingList.packedItemCount == packingList.totalItemCount && packingList.totalItemCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "${packingList.packedItemCount} / ${packingList.totalItemCount} Packed",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            val progress = if (packingList.totalItemCount > 0) packingList.packedItemCount.toFloat() / packingList.totalItemCount.toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        // Categorized Packing Items List
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(packingList.categories) { category ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = category.categoryName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        category.items.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { viewModel.togglePackingItem(item.id) },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = item.isPacked,
                                                    onCheckedChange = { viewModel.togglePackingItem(item.id) },
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.name,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (item.isPacked) FontWeight.Normal else FontWeight.SemiBold,
                                                        textDecoration = if (item.isPacked) TextDecoration.LineThrough else TextDecoration.None,
                                                        color = if (item.isPacked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (item.sourceOutfitTitle.isNotBlank()) {
                                                        Text(
                                                            text = "Source: ${item.sourceOutfitTitle}",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Packing Tips Card
                            if (packingList.packingTips.isNotEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Smart Packing Tips:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                            }
                                            packingList.packingTips.forEach { tip ->
                                                Text("• $tip", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.clearPackingList() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Re-select", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    OutfitShareUtil.sharePackingList(context, packingList)
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("share_packing_list_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share / Copy List", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
