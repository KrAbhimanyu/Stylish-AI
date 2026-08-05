package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.data.local.CalendarEvent
import com.example.ui.components.TripPackingListDialog
import com.example.ui.viewmodel.StylistViewModel
import com.example.util.OutfitNotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarEventsScreen(
    viewModel: StylistViewModel,
    onNavigateToStylistWithEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.calendarEvents.collectAsState()
    val savedOutfits by viewModel.savedOutfits.collectAsState()
    val currentPackingList by viewModel.currentPackingList.collectAsState()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var showPackingDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val calendarPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.scanAndSyncCalendar(context)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddEventDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Event") },
                text = { Text("New Event") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_event_fab")
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Calendar Occasions",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Plan outfits in advance for your upcoming marriages, parties, and outings.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedButton(
                        onClick = {
                            calendarPermissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("scan_system_calendar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan & Import System Calendar Dress Codes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (events.isEmpty()) {
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
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "No Events",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Upcoming Events Saved",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap '+ New Event' to add weddings, parties, or meetings.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(events, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onStyleMe = {
                            viewModel.updateOccasion(event.occasionType)
                            viewModel.generateRecommendation(
                                prompt = "Suggest outfit for ${event.title} (${event.dressCode}) at ${event.location}",
                                occasion = event.occasionType
                            )
                            onNavigateToStylistWithEvent(event)
                        },
                        onDelete = { viewModel.deleteCalendarEvent(event.id) },
                        onGeneratePackingList = { showPackingDialog = true }
                    )
                }
            }
        }
    }

    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onAdd = { newEvent ->
                viewModel.addCalendarEvent(newEvent)
                showAddEventDialog = false
            }
        )
    }

    if (showPackingDialog) {
        TripPackingListDialog(
            savedOutfits = savedOutfits,
            packingList = currentPackingList,
            viewModel = viewModel,
            onDismiss = { showPackingDialog = false }
        )
    }
}

@Composable
private fun EventCard(
    event: CalendarEvent,
    onStyleMe: () -> Unit,
    onDelete: () -> Unit,
    onGeneratePackingList: () -> Unit
) {
    val context = LocalContext.current
    var isReminderSent by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            text = event.occasionType.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val posted = OutfitNotificationHelper.triggerEventOutfitReminder(context, event)
                            isReminderSent = true
                            if (posted) {
                                Toast.makeText(context, "🔔 Outfit reminder notification posted!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Notification posted or channel created for ${event.title}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("notification_reminder_button_${event.id}")
                    ) {
                        Icon(
                            imageVector = if (isReminderSent) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Set Outfit Notification Reminder",
                            tint = if (isReminderSent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Event",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.date} at ${event.time}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.location,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (event.dressCode.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Dress Code: ${event.dressCode}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val posted = OutfitNotificationHelper.triggerEventOutfitReminder(context, event)
                        isReminderSent = true
                        Toast.makeText(context, "🔔 Reminder notification scheduled for ${event.title}!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("schedule_reminder_btn_${event.id}"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remind", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onGeneratePackingList,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pack_list_btn_${event.id}"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardTravel,
                        contentDescription = "Pack List",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pack List", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        OutfitNotificationHelper.triggerEventOutfitReminder(context, event)
                        onStyleMe()
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("style_me_for_event_button"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Style Me",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Style", fontSize = 11.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEventDialog(
    onDismiss: () -> Unit,
    onAdd: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var occasionType by remember { mutableStateOf("Marriage / Wedding") }
    var date by remember { mutableStateOf("2026-08-20") }
    var time by remember { mutableStateOf("19:00") }
    var location by remember { mutableStateOf("") }
    var dressCode by remember { mutableStateOf("") }

    val occasions = listOf("Marriage / Wedding", "Party / Clubbing", "Business / Meeting", "Casual Outing", "Date Night", "Festival")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Calendar Occasion", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Name (e.g. Sangeet Ceremony)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Occasion Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    occasions.forEach { occ ->
                        FilterChip(
                            selected = occasionType == occ,
                            onClick = { occasionType = occ },
                            label = { Text(occ, fontSize = 11.sp) }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Venue") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dressCode,
                    onValueChange = { dressCode = it },
                    label = { Text("Dress Code (e.g., Festive Ethnic Glam)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            CalendarEvent(
                                title = title,
                                occasionType = occasionType,
                                date = date,
                                time = time,
                                location = location.ifBlank { "Local Venue" },
                                dressCode = dressCode.ifBlank { "Smart Casual" }
                            )
                        )
                    }
                }
            ) {
                Text("Add Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
