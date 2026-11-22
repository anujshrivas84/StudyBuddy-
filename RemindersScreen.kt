package com.anix.studybuddy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.anix.studybuddy.model.Exam
import com.anix.studybuddy.model.Reminder
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    reminders: List<Reminder>,
    exams: List<Exam>,
    onAdd: (Reminder) -> Unit,
    onToggle: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Black)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("⏰ Study Reminders", color = NeonGreen,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Alarms work offline and after restart",
                color = OnSurfaceDim,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))

            if (reminders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AlarmOff, contentDescription = null,
                            tint = OnSurfaceDim, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No reminders yet", color = OnSurfaceDim,
                            style = MaterialTheme.typography.bodyLarge)
                        Text("Tap + to add your first alarm",
                            color = OnSurfaceDim,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(reminder, onToggle, onDelete)
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = NeonGreen,
            contentColor = Black
        ) { Icon(Icons.Default.Add, "Add Reminder") }
    }

    if (showDialog) {
        AddReminderDialog(exams, onDismiss = { showDialog = false }, onConfirm = { onAdd(it); showDialog = false })
    }
}

@Composable
fun ReminderCard(reminder: Reminder, onToggle: (Reminder) -> Unit, onDelete: (Reminder) -> Unit) {
    val time = Instant.ofEpochMilli(reminder.triggerMillis)
        .atZone(ZoneId.systemDefault())
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled) Surface2 else Surface1
        ),
        border = BorderStroke(1.dp, if (reminder.isEnabled) NeonGreen.copy(0.3f) else Surface3)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(reminder.title, color = OnSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge)
                if (reminder.subject.isNotBlank()) {
                    Text(reminder.subject, color = NeonGreen,
                        style = MaterialTheme.typography.bodySmall)
                }
                Text(time.format(fmt), color = OnSurfaceDim,
                    style = MaterialTheme.typography.bodySmall)
                Text("Snooze: ${reminder.snoozeMinutes}m",
                    color = OnSurfaceDim,
                    style = MaterialTheme.typography.labelSmall)
            }
            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = { onToggle(reminder) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Black,
                    checkedTrackColor = NeonGreen
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { onDelete(reminder) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete",
                    tint = ErrorRed.copy(alpha = 0.7f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    exams: List<Exam>,
    onDismiss: () -> Unit,
    onConfirm: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }
    var snooze by remember { mutableStateOf(10) }
    var showSubjectMenu by remember { mutableStateOf(false) }

    // Date state
    val today = LocalDate.now()
    var selDate by remember { mutableStateOf(today) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        title = { Text("New Reminder", color = NeonGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen, focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface, unfocusedBorderColor = Surface3
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Subject picker
                ExposedDropdownMenuBox(
                    expanded = showSubjectMenu,
                    onExpandedChange = { showSubjectMenu = it }
                ) {
                    OutlinedTextField(
                        value = subject, onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showSubjectMenu) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showSubjectMenu,
                        onDismissRequest = { showSubjectMenu = false },
                        containerColor = Surface3
                    ) {
                        exams.forEach { exam ->
                            DropdownMenuItem(
                                text = { Text(exam.subject, color = OnSurface) },
                                onClick = { subject = exam.subject; showSubjectMenu = false }
                            )
                        }
                    }
                }

                // Time row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour.toString(), onValueChange = { hour = it.toIntOrNull()?.coerceIn(0,23) ?: hour },
                        label = { Text("Hour (0-23)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minute.toString(), onValueChange = { minute = it.toIntOrNull()?.coerceIn(0,59) ?: minute },
                        label = { Text("Min (0-59)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Days from now
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = snooze.toString(), onValueChange = { snooze = it.toIntOrNull()?.coerceIn(1,60) ?: snooze },
                        label = { Text("Snooze (min)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Alarm fires today at $hour:${minute.toString().padStart(2,'0')}",
                    color = NeonGreen, style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val triggerTime = LocalDateTime.of(today, LocalTime.of(hour, minute))
                    val millis = triggerTime
                        .atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                    onConfirm(Reminder(title = title, subject = subject,
                        triggerMillis = millis, snoozeMinutes = snooze))
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Black)
            ) { Text("Add Alarm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceDim) }
        }
    )
}
