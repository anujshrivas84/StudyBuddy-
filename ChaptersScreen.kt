package com.anix.studybuddy.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.anix.studybuddy.model.Chapter
import com.anix.studybuddy.model.Exam

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChaptersScreen(
    chapters: List<Chapter>,
    exams: List<Exam>,
    onAddChapter: (Chapter) -> Unit,
    onDeleteChapter: (Chapter) -> Unit,
    onToggleTopic: (Chapter, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }

    // Group chapters by subject
    val bySubject = chapters.groupBy { it.subject }
    val subjects = exams.map { it.subject }

    Box(Modifier.fillMaxSize().background(Black)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("📚 Chapters & Topics", color = NeonGreen,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Track your study progress per topic",
                color = OnSurfaceDim, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))

            // Subject filter chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSubject == null,
                        onClick = { selectedSubject = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonGreen,
                            selectedLabelColor = Black,
                            containerColor = Surface2,
                            labelColor = OnSurfaceDim
                        )
                    )
                }
                items(subjects) { sub ->
                    FilterChip(
                        selected = selectedSubject == sub,
                        onClick = { selectedSubject = if (selectedSubject == sub) null else sub },
                        label = { Text(sub.take(12)) },
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

            val filteredSubjects = if (selectedSubject != null) listOf(selectedSubject!!) else subjects
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredSubjects.forEach { subject ->
                    val subChapters = bySubject[subject] ?: emptyList()
                    item {
                        Text(subject, color = NeonGreen,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp))
                    }
                    if (subChapters.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Surface1),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("No chapters yet",
                                    color = OnSurfaceDim,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp))
                            }
                        }
                    } else {
                        items(subChapters, key = { it.id }) { chapter ->
                            ChapterCard(chapter, onDeleteChapter, onToggleTopic)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = NeonGreen,
            contentColor = Black
        ) { Icon(Icons.Default.Add, "Add Chapter") }
    }

    if (showDialog) {
        AddChapterDialog(
            exams = exams,
            onDismiss = { showDialog = false },
            onConfirm = { onAddChapter(it); showDialog = false }
        )
    }
}

@Composable
fun ChapterCard(
    chapter: Chapter,
    onDelete: (Chapter) -> Unit,
    onToggleTopic: (Chapter, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val topics = chapter.topics.split(",").filter { it.isNotBlank() }
    val completed = chapter.completedTopics.split(",").filter { it.isNotBlank() }.toSet()
    val progress = if (topics.isNotEmpty()) completed.size.toFloat() / topics.size else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.isCompleted) Surface3 else Surface2
        ),
        border = BorderStroke(1.dp, if (chapter.isCompleted) NeonGreen.copy(0.5f) else Surface3)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (chapter.isCompleted) Icons.Default.CheckCircle else Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = if (chapter.isCompleted) NeonGreen else OnSurfaceDim,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(chapter.name, color = OnSurface,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge)
                    if (topics.isNotEmpty()) {
                        Text("${completed.size}/${topics.size} topics done",
                            color = OnSurfaceDim,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (topics.isNotEmpty()) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null, tint = OnSurfaceDim
                        )
                    }
                }
                IconButton(onClick = { onDelete(chapter) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(0.6f))
                }
            }

            if (topics.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = NeonGreen,
                    trackColor = Surface3
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 10.dp)) {
                    topics.forEach { topic ->
                        val isDone = completed.contains(topic)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleTopic(chapter, topic) }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        ) {
                            Icon(
                                if (isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (isDone) NeonGreen else OnSurfaceDim,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(topic,
                                color = if (isDone) OnSurfaceDim else OnSurface,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChapterDialog(
    exams: List<Exam>,
    onDismiss: () -> Unit,
    onConfirm: (Chapter) -> Unit
) {
    var chapterName by remember { mutableStateOf("") }
    var topicsText by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(exams.firstOrNull()?.subject ?: "") }
    var showMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        title = { Text("Add Chapter", color = NeonGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = showMenu, onExpandedChange = { showMenu = it }) {
                    OutlinedTextField(
                        value = selectedSubject, onValueChange = {},
                        readOnly = true, label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showMenu) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                            unfocusedTextColor = OnSurface, focusedTextColor = OnSurface,
                            unfocusedBorderColor = Surface3
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false },
                        containerColor = Surface3) {
                        exams.forEach { exam ->
                            DropdownMenuItem(
                                text = { Text(exam.subject, color = OnSurface) },
                                onClick = { selectedSubject = exam.subject; showMenu = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = chapterName, onValueChange = { chapterName = it },
                    label = { Text("Chapter Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen, focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface, unfocusedBorderColor = Surface3
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = topicsText, onValueChange = { topicsText = it },
                    label = { Text("Topics (comma separated)") },
                    placeholder = { Text("Topic 1, Topic 2, Topic 3", color = OnSurfaceDim) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen, focusedLabelColor = NeonGreen,
                        cursorColor = NeonGreen, focusedTextColor = OnSurface,
                        unfocusedTextColor = OnSurface, unfocusedBorderColor = Surface3
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chapterName.isBlank()) return@Button
                    onConfirm(Chapter(
                        examId = 0,
                        subject = selectedSubject,
                        name = chapterName,
                        topics = topicsText.split(",")
                            .map { it.trim() }.filter { it.isNotBlank() }
                            .joinToString(",")
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Black)
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = OnSurfaceDim) } }
    )
}
