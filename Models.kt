package com.anix.studybuddy.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

// ─── Exam Entity ─────────────────────────────────────────────────────────────
@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val dateEpochDay: Long,          // LocalDate.toEpochDay()
    val priority: Int,               // 1 = highest
    val isActive: Boolean = true,    // false = on hold
    val colorHex: String = "#00FF9C"
)

// ─── Reminder / Alarm Entity ──────────────────────────────────────────────────
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val triggerMillis: Long,         // exact epoch millis
    val repeatDays: String = "",     // comma-separated: "MON,WED,FRI"
    val isEnabled: Boolean = true,
    val snoozeMinutes: Int = 10
)

// ─── Chapter / Topic Entity ───────────────────────────────────────────────────
@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: Int,
    val subject: String,
    val name: String,
    val topics: String = "",         // JSON array of topic names
    val completedTopics: String = "",// JSON array of completed topic names
    val isCompleted: Boolean = false
)

// ─── Timetable Slot Entity ────────────────────────────────────────────────────
@Entity(tableName = "timetable")
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val dayOfWeek: Int,              // 1=Mon … 7=Sun
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val color: String = "#00FF9C"
)

// ─── UI-only helpers ──────────────────────────────────────────────────────────
data class ExamUi(
    val exam: Exam,
    val daysLeft: Long,
    val date: LocalDate
)

data class TopicItem(
    val name: String,
    val isCompleted: Boolean
)
