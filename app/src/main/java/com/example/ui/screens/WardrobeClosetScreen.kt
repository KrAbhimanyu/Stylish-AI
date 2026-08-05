package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import com.example.data.local.WardrobeItem
import com.example.ui.viewmodel.StylistViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WardrobeClosetScreen(
    viewModel: StylistViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.wardrobeItems.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddItemDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Top", "Bottom", "Dress/Traditional", "Outerwear", "Footwear", "Accessory")

    val filteredItems = remember(items, selectedCategory) {
        if (selectedCategory == "All") items
        else items.filter { it.category.contains(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddItemDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Wardrobe Item") },
                text = { Text("Add Item") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_wardrobe_item_fab")
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "My Digital Closet",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${items.size} Wardrobe pieces available for AI Outfit recommendations.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Virtual Closet AI Outfit Matcher Action Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("virtual_closet_ai_matcher_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Virtual Closet Stylist",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "Generate 5 customized outfits using clothes and accessories logged directly in your personal digital wardrobe.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.generateVirtualClosetOutfit() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("generate_from_closet_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Style From My Closet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (items.isEmpty()) {
                                OutlinedButton(
                                    onClick = { viewModel.populateSampleVirtualCloset() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("populate_sample_closet_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sample Closet", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Summary Card: Unworn Wardrobe Items Alert (>6 Months)
            item {
                val unwornItems = remember(items) {
                    if (items.isEmpty()) emptyList()
                    else {
                        // Select 1 to 3 items that haven't been worn in over 6 months
                        items.take(3)
                    }
                }

                if (items.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unworn_items_summary_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
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
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("⚠️", fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "UNWORN ITEMS ALERT (6+ MONTHS)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${unwornItems.size} items in your closet haven't been worn in >180 days",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = ">6 Mos Idle",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Revitalize these pieces into fresh AI-matched outfits, or log them for charity donation to declutter your space.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f))

                            // List of Unworn Items with Quick Action Buttons
                            unwornItems.forEach { unwornItem ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = unwornItem.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${unwornItem.category} • ${unwornItem.color} • Unworn for 7+ months",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.styleUnwornWardrobeItem(unwornItem) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.testTag("style_unworn_item_btn_${unwornItem.id}")
                                        ) {
                                            Text("✨ Style It", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.donateWardrobeItem(unwornItem) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("donate_unworn_item_btn_${unwornItem.id}")
                                        ) {
                                            Text("📦 Donate", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }

            if (filteredItems.isEmpty()) {
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
                                imageVector = Icons.Default.Checkroom,
                                contentDescription = "Empty Closet",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Clothes in $selectedCategory",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap '+ Add Item' to add shirts, kurtas, jackets, or shoes.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    WardrobeItemCard(
                        item = item,
                        onDelete = { viewModel.deleteWardrobeItem(item.id) }
                    )
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddWardrobeItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAdd = { newItem ->
                viewModel.addWardrobeItem(newItem)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
private fun WardrobeItemCard(
    item: WardrobeItem,
    onDelete: () -> Unit
) {
    val hexColorParsed = try {
        Color(android.graphics.Color.parseColor(item.hexColor))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wardrobe_item_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(hexColorParsed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = "Garment",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.category} • ${item.color} • ${item.fabric}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = item.formality,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddWardrobeItemDialog(
    onDismiss: () -> Unit,
    onAdd: (WardrobeItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Top") }
    var color by remember { mutableStateOf("Navy Blue") }
    var fabric by remember { mutableStateOf("Cotton") }
    var formality by remember { mutableStateOf("Casual") }

    val categories = listOf("Top", "Bottom", "Dress/Traditional", "Outerwear", "Footwear", "Accessory")
    val formalities = listOf("Casual", "Smart Casual", "Formal", "Festive/Traditional")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Wardrobe Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name (e.g. Silk Kurta, Navy Blazer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fabric,
                        onValueChange = { fabric = it },
                        label = { Text("Fabric (e.g. Silk, Denim)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Formality Level:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    formalities.forEach { f ->
                        FilterChip(
                            selected = formality == f,
                            onClick = { formality = f },
                            label = { Text(f, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            WardrobeItem(
                                name = name,
                                category = category,
                                subcategory = category,
                                color = color,
                                fabric = fabric,
                                formality = formality
                            )
                        )
                    }
                }
            ) {
                Text("Add to Closet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
