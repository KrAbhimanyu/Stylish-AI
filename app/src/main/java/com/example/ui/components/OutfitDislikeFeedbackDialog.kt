package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutfitDislikeFeedbackDialog(
    outfitTitle: String,
    onDismiss: () -> Unit,
    onSubmitFeedback: (reason: String, note: String) -> Unit
) {
    val feedbackReasons = listOf(
        "Too expensive",
        "Wrong color",
        "Not my style",
        "Too formal",
        "Too casual",
        "Inappropriate for weather",
        "Don't own similar pieces"
    )

    var selectedReason by remember { mutableStateOf(feedbackReasons[0]) }
    var customNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("outfit_dislike_feedback_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ThumbDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OUTFIT FEEDBACK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Why is this look not for you?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dislike_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Help Gemini AI learn your taste for \"$outfitTitle\". Select the primary reason:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Reason Picker Chips
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    feedbackReasons.forEach { reason ->
                        val isSelected = selectedReason == reason
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedReason = reason },
                            label = { Text(reason, fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("dislike_reason_chip_$reason")
                        )
                    }
                }

                // Optional Custom Note Input
                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    placeholder = { Text("Optional note (e.g. Prefer linen instead of wool)", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dislike_custom_note_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSubmitFeedback(selectedReason, customNote)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_dislike_feedback_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Submit Feedback", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
