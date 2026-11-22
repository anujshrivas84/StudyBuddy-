package com.anix.studybuddy.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.anix.studybuddy.alarm.AlarmScheduler
import com.anix.studybuddy.data.AppDatabase
import com.anix.studybuddy.data.StudyRepository
import com.anix.studybuddy.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = StudyRepository(db)
    private val ctx: Context get() = getApplication()

    val allExams: StateFlow<List<Exam>> = repo.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<Reminder>> = repo.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allChapters: StateFlow<List<Chapter>> = repo.getAllChapters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSlots: StateFlow<List<TimetableSlot>> = repo.getAllSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Exams enriched with days-left
    val examUiList: StateFlow<List<ExamUi>> = allExams.map { exams ->
        val today = LocalDate.now()
        exams.map { exam ->
            val date = LocalDate.ofEpochDay(exam.dateEpochDay)
            ExamUi(exam, today.until(date, java.time.temporal.ChronoUnit.DAYS), date)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Seed default data on first launch ────────────────────────────────────
    fun seedIfEmpty() {
        viewModelScope.launch {
            if (repo.getExamCount() == 0) {
                val exams = listOf(
                    Exam(subject = "Steno",                    dateEpochDay = LocalDate.of(2026, 12, 8).toEpochDay(),  priority = 1, isActive = true,  colorHex = "#00FF9C"),
                    Exam(subject = "Business Law",             dateEpochDay = LocalDate.of(2026, 12, 10).toEpochDay(), priority = 2, isActive = true,  colorHex = "#00FF9C"),
                    Exam(subject = "CAB",                      dateEpochDay = LocalDate.of(2026, 12, 15).toEpochDay(), priority = 3, isActive = true,  colorHex = "#00FF9C"),
                    Exam(subject = "VAC (Reading Indian Fiction)", dateEpochDay = LocalDate.of(2026, 12, 19).toEpochDay(), priority = 4, isActive = false, colorHex = "#888888"),
                    Exam(subject = "SEC (Museum & Museology)", dateEpochDay = LocalDate.of(2026, 12, 21).toEpochDay(), priority = 5, isActive = false, colorHex = "#888888"),
                    Exam(subject = "Hindi",                    dateEpochDay = LocalDate.of(2026, 12, 22).toEpochDay(), priority = 6, isActive = false, colorHex = "#888888"),
                    Exam(subject = "EVS",                      dateEpochDay = LocalDate.of(2026, 12, 28).toEpochDay(), priority = 7, isActive = false, colorHex = "#888888")
                )
                exams.forEach { repo.insertExam(it) }
            }
        }
    }

    // ── Auto-activate exams after CAB (Dec 15) ────────────────────────────────
    fun checkAndActivateExams() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val cabDate = LocalDate.of(2026, 12, 15)
            if (!today.isBefore(cabDate)) {
                val holdSubjects = listOf("VAC (Reading Indian Fiction)", "SEC (Museum & Museology)", "Hindi", "EVS")
                holdSubjects.forEach { repo.activateExam(it) }
            }
        }
    }

    // ── Exam CRUD ────────────────────────────────────────────────────────────
    fun addExam(exam: Exam) = viewModelScope.launch { repo.insertExam(exam) }
    fun updateExam(exam: Exam) = viewModelScope.launch { repo.updateExam(exam) }
    fun deleteExam(exam: Exam) = viewModelScope.launch { repo.deleteExam(exam) }

    // ── Reminder CRUD ─────────────────────────────────────────────────────────
    fun addReminder(reminder: Reminder) = viewModelScope.launch {
        val id = repo.insertReminder(reminder).toInt()
        AlarmScheduler.schedule(ctx, reminder.copy(id = id))
    }

    fun toggleReminder(reminder: Reminder) = viewModelScope.launch {
        val updated = reminder.copy(isEnabled = !reminder.isEnabled)
        repo.updateReminder(updated)
        if (updated.isEnabled) AlarmScheduler.schedule(ctx, updated)
        else AlarmScheduler.cancel(ctx, updated.id)
    }

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        AlarmScheduler.cancel(ctx, reminder.id)
        repo.deleteReminder(reminder)
    }

    // ── Chapter CRUD ─────────────────────────────────────────────────────────
    fun addChapter(chapter: Chapter) = viewModelScope.launch { repo.insertChapter(chapter) }
    fun updateChapter(chapter: Chapter) = viewModelScope.launch { repo.updateChapter(chapter) }
    fun deleteChapter(chapter: Chapter) = viewModelScope.launch { repo.deleteChapter(chapter) }

    fun toggleTopic(chapter: Chapter, topicName: String) = viewModelScope.launch {
        val completed = chapter.completedTopics.split(",").filter { it.isNotBlank() }.toMutableList()
        if (completed.contains(topicName)) completed.remove(topicName)
        else completed.add(topicName)
        val allTopics = chapter.topics.split(",").filter { it.isNotBlank() }
        val isDone = allTopics.isNotEmpty() && completed.containsAll(allTopics)
        repo.updateChapter(chapter.copy(completedTopics = completed.joinToString(","), isCompleted = isDone))
    }

    // ── Timetable CRUD ───────────────────────────────────────────────────────
    fun addSlot(slot: TimetableSlot) = viewModelScope.launch { repo.insertSlot(slot) }
    fun deleteSlot(slot: TimetableSlot) = viewModelScope.launch { repo.deleteSlot(slot) }
}
