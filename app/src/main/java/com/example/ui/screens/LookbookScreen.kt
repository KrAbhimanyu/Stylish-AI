package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.SavedOutfit
import com.example.ui.components.SideBySideOutfitComparisonDialog
import com.example.ui.components.StarRatingBar
import com.example.ui.components.TripPackingListDialog
import com.example.ui.viewmodel.StylistViewModel
import com.example.util.OutfitShareUtil
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LookbookScreen(
    viewModel: StylistViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedOutfits by viewModel.savedOutfits.collectAsState()
    val currentPackingList by viewModel.currentPackingList.collectAsState()

    var isGridView by remember { mutableStateOf(true) }
    var selectedTagFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOutfitForImageDialog by remember { mutableStateOf<SavedOutfit?>(null) }
    var showPackingListDialog by remember { mutableStateOf(false) }

    var isCompareMode by remember { mutableStateOf(false) }
    var selectedCompareOutfits by remember { mutableStateOf<List<SavedOutfit>>(emptyList()) }
    var showComparisonDialog by remember { mutableStateOf(false) }

    val tagCategories = listOf("All", "Formal", "Casual", "Work", "Party", "Wedding", "Favorites (★)")

    val filteredOutfits = remember(savedOutfits, selectedTagFilter, searchQuery) {
        savedOutfits.filter { outfit ->
            val matchesCategory = when (selectedTagFilter) {
                "All" -> true
                "Formal" -> outfit.occasion.contains("Formal", ignoreCase = true) || outfit.occasion.contains("Business", ignoreCase = true) || outfit.stylingTips.contains("formal", ignoreCase = true)
                "Casual" -> outfit.occasion.contains("Casual", ignoreCase = true) || outfit.occasion.contains("Outing", ignoreCase = true) || outfit.stylingTips.contains("casual", ignoreCase = true)
                "Work" -> outfit.occasion.contains("Work", ignoreCase = true) || outfit.occasion.contains("Business", ignoreCase = true) || outfit.occasion.contains("Meeting", ignoreCase = true)
                "Party" -> outfit.occasion.contains("Party", ignoreCase = true) || outfit.occasion.contains("Clubbing", ignoreCase = true) || outfit.occasion.contains("Festival", ignoreCase = true)
                "Wedding" -> outfit.occasion.contains("Wedding", ignoreCase = true) || outfit.occasion.contains("Marriage", ignoreCase = true) || outfit.occasion.contains("Ethnic", ignoreCase = true)
                "Favorites (★)" -> outfit.userRating >= 4 || outfit.isFavorite
                else -> outfit.occasion.contains(selectedTagFilter, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isBlank() ||
                    outfit.title.contains(searchQuery, ignoreCase = true) ||
                    outfit.occasion.contains(searchQuery, ignoreCase = true) ||
                    outfit.topItem.contains(searchQuery, ignoreCase = true) ||
                    outfit.bottomItem.contains(searchQuery, ignoreCase = true) ||
                    outfit.colorPalette.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("lookbook_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Title
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Lookbook Gallery",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Grid / List Toggle Buttons
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            IconButton(
                                onClick = { isGridView = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Grid View",
                                    tint = if (isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { isGridView = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewList,
                                    contentDescription = "List View",
                                    tint = if (!isGridView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "${savedOutfits.size} saved looks in your local gallery.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Feature Quick Actions Row: Trip Packing List & Compare Outfits & Style Analysis
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { showPackingListDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_packing_list_button"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardTravel,
                            contentDescription = "Packing List Generator",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🧳 Packing List", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isCompareMode = !isCompareMode
                            if (!isCompareMode) selectedCompareOutfits = emptyList()
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("toggle_compare_mode_button"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                        colors = if (isCompareMode) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Compare Side-by-Side",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCompareMode) "Cancel (${selectedCompareOutfits.size}/2)" else "⚡ Compare Looks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.runSmartStyleAnalysis() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("run_style_analysis_lookbook_btn"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Run Style Analysis",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✨ Analysis", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isCompareMode) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Select 2 outfits to view side-by-side comparison (${selectedCompareOutfits.size}/2 selected)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (selectedCompareOutfits.size == 2) {
                                Button(
                                    onClick = { showComparisonDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("launch_side_by_side_comparison_btn")
                                ) {
                                    Text("View Side-by-Side", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search Bar & Tag Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title, top, bottom, palette...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Outfits",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("outfit_search_textfield")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tagCategories.forEach { category ->
                        val isSelected = selectedTagFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTagFilter = category },
                            label = { Text(category, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_tag_$category")
                        )
                    }
                }
            }
        }

        if (filteredOutfits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Empty Gallery",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (savedOutfits.isEmpty()) "No Saved Outfits Yet" else "No Outfits found for '$selectedTagFilter'",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Save AI generated recommendations from the Stylist tab to render local snapshot image cards here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (isGridView) {
            // Grid View (2 items per row)
            val chunked = filteredOutfits.chunked(2)
            items(chunked) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { outfit ->
                        val isSelectedForCompare = selectedCompareOutfits.any { it.id == outfit.id }
                        Box(modifier = Modifier.weight(1f)) {
                            SavedOutfitGridCard(
                                outfit = outfit,
                                onViewImage = {
                                    if (isCompareMode) {
                                        selectedCompareOutfits = if (isSelectedForCompare) {
                                            selectedCompareOutfits.filterNot { it.id == outfit.id }
                                        } else if (selectedCompareOutfits.size < 2) {
                                            selectedCompareOutfits + outfit
                                        } else selectedCompareOutfits
                                    } else {
                                        selectedOutfitForImageDialog = outfit
                                    }
                                },
                                onDelete = { viewModel.deleteSavedOutfit(outfit.id) },
                                onExportImage = { viewModel.exportAndSaveOutfitImage(context, outfit) },
                                onRateOutfit = { rating -> viewModel.rateSavedOutfit(outfit.id, rating) },
                                isCompareMode = isCompareMode,
                                isSelectedForCompare = isSelectedForCompare,
                                onPushToCalendar = { viewModel.pushSavedOutfitToCalendarReminder(context, outfit) }
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // List View
            items(filteredOutfits, key = { it.id }) { outfit ->
                val isSelectedForCompare = selectedCompareOutfits.any { it.id == outfit.id }
                SavedOutfitListCard(
                    outfit = outfit,
                    onViewImage = {
                        if (isCompareMode) {
                            selectedCompareOutfits = if (isSelectedForCompare) {
                                selectedCompareOutfits.filterNot { it.id == outfit.id }
                            } else if (selectedCompareOutfits.size < 2) {
                                selectedCompareOutfits + outfit
                            } else selectedCompareOutfits
                        } else {
                            selectedOutfitForImageDialog = outfit
                        }
                    },
                    onDelete = { viewModel.deleteSavedOutfit(outfit.id) },
                    onExportImage = { viewModel.exportAndSaveOutfitImage(context, outfit) },
                    onRateOutfit = { rating -> viewModel.rateSavedOutfit(outfit.id, rating) },
                    isCompareMode = isCompareMode,
                    isSelectedForCompare = isSelectedForCompare,
                    onPushToCalendar = { viewModel.pushSavedOutfitToCalendarReminder(context, outfit) }
                )
            }
        }
    }

    if (showComparisonDialog && selectedCompareOutfits.size == 2) {
        SideBySideOutfitComparisonDialog(
            outfit1 = selectedCompareOutfits[0],
            outfit2 = selectedCompareOutfits[1],
            onDismiss = { showComparisonDialog = false },
            onPushToCalendar = { outfit ->
                viewModel.pushSavedOutfitToCalendarReminder(context, outfit)
                showComparisonDialog = false
            }
        )
    }

    // Full-Screen Image Lightbox Viewer Dialog
    selectedOutfitForImageDialog?.let { outfit ->
        OutfitImageLightboxDialog(
            outfit = outfit,
            onDismiss = { selectedOutfitForImageDialog = null },
            onGenerateImage = {
                viewModel.exportAndSaveOutfitImage(context, outfit)
            }
        )
    }

    if (showPackingListDialog) {
        TripPackingListDialog(
            savedOutfits = savedOutfits,
            packingList = currentPackingList,
            viewModel = viewModel,
            onDismiss = {
                showPackingListDialog = false
            }
        )
    }
}

@Composable
private fun SavedOutfitGridCard(
    outfit: SavedOutfit,
    onViewImage: () -> Unit,
    onDelete: () -> Unit,
    onExportImage: () -> Unit,
    onRateOutfit: (Int) -> Unit,
    isCompareMode: Boolean = false,
    isSelectedForCompare: Boolean = false,
    onPushToCalendar: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_outfit_grid_card_${outfit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForCompare) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelectedForCompare) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Local Image Thumbnail Header
            val imageFile = remember(outfit.imagePath) { if (outfit.imagePath.isNotBlank()) File(outfit.imagePath) else null }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable { onViewImage() },
                contentAlignment = Alignment.Center
            ) {
                if (imageFile != null && imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = outfit.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image Preview",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Tap to Generate Image Card",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Occasion Tag Overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = outfit.occasion.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                if (isCompareMode) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelectedForCompare) MaterialTheme.colorScheme.tertiary else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelectedForCompare) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Select for comparison",
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp).size(20.dp)
                        )
                    }
                }
            }

            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = outfit.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "• Top: ${outfit.topItem}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = "• Bottom: ${outfit.bottomItem}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Interactive Rating
                StarRatingBar(
                    rating = outfit.userRating,
                    onRatingSelected = onRateOutfit,
                    starSize = 16.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onViewImage,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View Image",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (onPushToCalendar != null) {
                        IconButton(
                            onClick = onPushToCalendar,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("push_calendar_grid_btn_${outfit.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Push to Calendar Reminder",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { OutfitShareUtil.shareOutfitCardImage(context, outfit) },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("share_outfit_grid_btn_${outfit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Outfit",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onExportImage,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Generate Local File",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedOutfitListCard(
    outfit: SavedOutfit,
    onViewImage: () -> Unit,
    onDelete: () -> Unit,
    onExportImage: () -> Unit,
    onRateOutfit: (Int) -> Unit,
    isCompareMode: Boolean = false,
    isSelectedForCompare: Boolean = false,
    onPushToCalendar: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val imageFile = remember(outfit.imagePath) { if (outfit.imagePath.isNotBlank()) File(outfit.imagePath) else null }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_outfit_list_card_${outfit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForCompare) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelectedForCompare) BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = outfit.occasion.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = outfit.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompareMode) {
                        IconButton(onClick = onViewImage) {
                            Icon(
                                imageVector = if (isSelectedForCompare) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Select for comparison",
                                tint = if (isSelectedForCompare) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (onPushToCalendar != null) {
                        IconButton(
                            onClick = onPushToCalendar,
                            modifier = Modifier.testTag("push_calendar_list_btn_${outfit.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Push to Calendar Reminder",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { OutfitShareUtil.shareOutfitCardImage(context, outfit) },
                        modifier = Modifier.testTag("share_outfit_list_btn_${outfit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Outfit",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    IconButton(onClick = onViewImage) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "View Image Card",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Image Snapshot Thumbnail Row
            if (imageFile != null && imageFile.exists()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onViewImage() }
                ) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = outfit.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Full Image", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onExportImage,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Render Local PNG Image Snapshot", fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SavedItemRow("Top", outfit.topItem)
                SavedItemRow("Bottom", outfit.bottomItem)
                if (outfit.outerwearItem.isNotBlank()) SavedItemRow("Outerwear", outfit.outerwearItem)
                SavedItemRow("Footwear", outfit.footwearItem)
                if (outfit.accessoryItems.isNotBlank()) SavedItemRow("Accessories", outfit.accessoryItems)
            }

            if (outfit.colorPalette.isNotBlank()) {
                Text(
                    text = "Palette: ${outfit.colorPalette}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (outfit.stylingTips.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Tips",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = outfit.stylingTips,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (outfit.userRating > 0) "Rating stored in Room DB" else "Rate this outfit:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StarRatingBar(
                    rating = outfit.userRating,
                    onRatingSelected = onRateOutfit,
                    starSize = 18.dp
                )
            }
        }
    }
}

@Composable
private fun SavedItemRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun OutfitImageLightboxDialog(
    outfit: SavedOutfit,
    onDismiss: () -> Unit,
    onGenerateImage: () -> Unit
) {
    val imageFile = remember(outfit.imagePath) { if (outfit.imagePath.isNotBlank()) File(outfit.imagePath) else null }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = outfit.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Saved Local Outfit Card Image",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (imageFile != null && imageFile.exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = outfit.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "File: ${imageFile.name}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "No Image",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Local Image File Not Rendered",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = onGenerateImage,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate PNG File Now", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = LocalContext.current

                    OutlinedButton(
                        onClick = { OutfitShareUtil.shareOutfitCardImage(context, outfit) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_outfit_lightbox_btn_${outfit.id}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onGenerateImage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-render", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
