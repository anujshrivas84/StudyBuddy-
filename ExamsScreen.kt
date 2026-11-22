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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.anix.studybuddy.model.Exam
import com.anix.studybuddy.model.ExamUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ExamsScreen(
    examUiList: List<ExamUi>,
    onDelete: (Exam) -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val activeExams = examUiList.filter { it.exam.isActive }
    val holdExams = examUiList.filter { !it.exam.isActive }

    Column(
        Modifier.fillMaxSize().background(Black)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("🗓 Exam Schedule", color = NeonGreen,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold)
        Text("December 2026 — All exams",
            color = OnSurfaceDim, style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))

        // Priority exams section
        Text("⚡ ACTIVE (Priority)", color = NeonGreen,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))

        activeExams.forEach { eu ->
            ExamDetailCard(eu, fmt, onDelete)
            Spacer(Modifier.height(10.dp))
        }

        if (holdExams.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏸ ON HOLD", color = OnSurfaceDim,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(8.dp))
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = Surface3)
                ) {
                    Text("Auto-activates after Dec 15",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = OnSurfaceDim,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            holdExams.forEach { eu ->
                ExamDetailCard(eu, fmt, onDelete, isHeld = true)
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun ExamDetailCard(
    eu: ExamUi,
    fmt: DateTimeFormatter,
    onDelete: (Exam) -> Unit,
    isHeld: Boolean = false
) {
    val urgencyColor = when {
        isHeld -> OnSurfaceDim
        eu.daysLeft <= 3 -> ErrorRed
        eu.daysLeft <= 7 -> WarnAmber
        else -> NeonGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHeld) Surface1 else Surface2
        ),
        border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Countdown box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(urgencyColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${eu.daysLeft}",
                        color = urgencyColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("days", color = urgencyColor.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(eu.exam.subject, color = if (isHeld) OnSurfaceDim else OnSurface,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge)
                Text(eu.date.format(fmt),
                    color = urgencyColor.copy(0.8f),
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isHeld) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = urgencyColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isHeld) "On hold" else "Priority ${eu.exam.priority}",
                        color = urgencyColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = { onDelete(eu.exam) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete",
                    tint = ErrorRed.copy(0.5f), modifier = Modifier.size(20.dp))
            }
        }
    }
}
