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
import com.anix.studybuddy.model.TimetableSlot

val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    slots: List<TimetableSlot>,
    exams: List<Exam>,
    onAddSlot: (TimetableSlot) -> Unit,
    onDeleteSlot: (TimetableSlot) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(1) } // 1=Mon

    Box(Modifier.fillMaxSize().background(Black)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("📅 Weekly Timetable", color = NeonGreen,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Day selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DAYS.size) { idx ->
                    val dayNum = idx + 1
                    val isSelected = selectedDay == dayNum
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDay = dayNum },
                        label = { Text(DAYS[idx], fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen,
                            selectedLabelColor = Black,
                            containerColor = Surface2,
                            labelColor = OnSurfaceDim
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val daySlots = slots.filter { it.dayOfWeek == selectedDay }
                .sortedWith(compareBy({ it.startHour }, { it.startMinute }))

            if (daySlots.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventNote, contentDescription = null,
                            tint = OnSurfaceDim, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No slots for ${DAYS[selectedDay - 1]}",
                            color = OnSurfaceDim, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daySlots, key = { it.id }) { slot ->
                        SlotCard(slot, onDeleteSlot)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = NeonGreen,
            contentColor = Black
        ) { Icon(Icons.Default.Add, "Add Slot") }
    }

    if (showDialog) {
        AddSlotDialog(
            exams = exams,
            preselectedDay = selectedDay,
            onDismiss = { showDialog = false },
            onConfirm = { onAddSlot(it); showDialog = false }
        )
    }
}

@Composable
fun SlotCard(slot: TimetableSlot, onDelete: (TimetableSlot) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Text(
                    "${slot.startHour.toString().padStart(2,'0')}:${slot.startMinute.toString().padStart(2,'0')}",
                    color = NeonGreen,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("│", color = Surface3)
                Text(
                    "${slot.endHour.toString().padStart(2,'0')}:${slot.endMinute.toString().padStart(2,'0')}",
                    color = OnSurfaceDim,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(slot.subject, color = OnSurface,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f))
            IconButton(onClick = { onDelete(slot) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSlotDialog(
    exams: List<Exam>,
    preselectedDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (TimetableSlot) -> Unit
) {
    var subject by remember { mutableStateOf(exams.firstOrNull()?.subject ?: "") }
    var dayOfWeek by remember { mutableStateOf(preselectedDay) }
    var startH by remember { mutableStateOf(9) }
    var startM by remember { mutableStateOf(0) }
    var endH by remember { mutableStateOf(11) }
    var endM by remember { mutableStateOf(0) }
    var showSubMenu by remember { mutableStateOf(false) }
    var showDayMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        title = { Text("Add Study Slot", color = NeonGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Subject picker
                ExposedDropdownMenuBox(expanded = showSubMenu, onExpandedChange = { showSubMenu = it }) {
                    OutlinedTextField(
                        value = subject, onValueChange = {}, readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showSubMenu) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = showSubMenu, onDismissRequest = { showSubMenu = false },
                        containerColor = Surface3) {
                        exams.forEach { DropdownMenuItem(text = { Text(it.subject, color = OnSurface) },
                            onClick = { subject = it.subject; showSubMenu = false }) }
                    }
                }
                // Day picker
                ExposedDropdownMenuBox(expanded = showDayMenu, onExpandedChange = { showDayMenu = it }) {
                    OutlinedTextField(
                        value = DAYS[dayOfWeek - 1], onValueChange = {}, readOnly = true,
                        label = { Text("Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showDayMenu) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = showDayMenu, onDismissRequest = { showDayMenu = false },
                        containerColor = Surface3) {
                        DAYS.forEachIndexed { i, d -> DropdownMenuItem(
                            text = { Text(d, color = OnSurface) },
                            onClick = { dayOfWeek = i + 1; showDayMenu = false }) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startH.toString(),
                        onValueChange = { startH = it.toIntOrNull()?.coerceIn(0,23) ?: startH },
                        label = { Text("Start H") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3),
                        modifier = Modifier.weight(1f))
                    OutlinedTextField(value = startM.toString(),
                        onValueChange = { startM = it.toIntOrNull()?.coerceIn(0,59) ?: startM },
                        label = { Text("Start M") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3),
                        modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = endH.toString(),
                        onValueChange = { endH = it.toIntOrNull()?.coerceIn(0,23) ?: endH },
                        label = { Text("End H") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3),
                        modifier = Modifier.weight(1f))
                    OutlinedTextField(value = endM.toString(),
                        onValueChange = { endM = it.toIntOrNull()?.coerceIn(0,59) ?: endM },
                        label = { Text("End M") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3),
                        modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(TimetableSlot(subject = subject, dayOfWeek = dayOfWeek,
                    startHour = startH, startMinute = startM, endHour = endH, endMinute = endM))
            }, colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Black)) {
                Text("Add Slot")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceDim) } }
    )
}
