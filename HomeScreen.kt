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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.anix.studybuddy.model.ExamUi
import com.anix.studybuddy.model.TimetableSlot
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    examUiList: List<ExamUi>,
    slots: List<TimetableSlot>
) {
    val today = LocalDate.now()
    val todaySlots = slots.filter { it.dayOfWeek == today.dayOfWeek.value }
    val activeExams = examUiList.filter { it.exam.isActive }
    val nextExam = activeExams.minByOrNull { it.daysLeft }
    val fmt = DateTimeFormatter.ofPattern("dd MMM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Anix Study Buddy",
            style = MaterialTheme.typography.headlineMedium,
            color = NeonGreen,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")),
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceDim
        )

        Spacer(Modifier.height(20.dp))

        // Next exam countdown card
        nextExam?.let { eu ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface2),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Surface2, Surface3)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text("⚡ NEXT EXAM", color = NeonGreen,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(eu.exam.subject,
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface, fontWeight = FontWeight.Bold)
                        Text(eu.date.format(fmt),
                            color = OnSurfaceDim,
                            style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${eu.daysLeft}",
                                style = MaterialTheme.typography.displayMedium,
                                color = NeonGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("days left",
                                color = OnSurfaceDim,
                                modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Priority exams
        Text("🎯 Priority Exams", color = OnSurface,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        activeExams.take(3).forEach { eu ->
            ExamCountdownRow(eu)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        // On-hold exams
        val holdExams = examUiList.filter { !it.exam.isActive }
        if (holdExams.isNotEmpty()) {
            Text("⏸ On Hold (activates after Dec 15)",
                color = OnSurfaceDim,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            holdExams.forEach { eu ->
                HoldExamRow(eu)
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(16.dp))
        }

        // Today's timetable
        Text("📅 Today's Schedule", color = OnSurface,
            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (todaySlots.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface2),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("No study slots scheduled for today.\nAdd slots in the Timetable tab.",
                    modifier = Modifier.padding(16.dp),
                    color = OnSurfaceDim,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            todaySlots.forEach { slot ->
                TimetableCard(slot)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun ExamCountdownRow(eu: ExamUi) {
    val urgencyColor = when {
        eu.daysLeft <= 3 -> ErrorRed
        eu.daysLeft <= 7 -> WarnAmber
        else -> NeonGreen
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(urgencyColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${eu.daysLeft}d",
                    color = urgencyColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(eu.exam.subject, color = OnSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge)
                Text(
                    eu.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    color = OnSurfaceDim,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("P${eu.exam.priority}", color = urgencyColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HoldExamRow(eu: ExamUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Surface1),
        border = BorderStroke(1.dp, Surface3)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PauseCircle, contentDescription = null,
                tint = OnSurfaceDim, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(eu.exam.subject, color = OnSurfaceDim,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f))
            Text(eu.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                color = OnSurfaceDim,
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun TimetableCard(slot: TimetableSlot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NeonGreen)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(slot.subject, color = OnSurface,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${slot.startHour.toString().padStart(2,'0')}:${slot.startMinute.toString().padStart(2,'0')} – ${slot.endHour.toString().padStart(2,'0')}:${slot.endMinute.toString().padStart(2,'0')}",
                    color = OnSurfaceDim, style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
