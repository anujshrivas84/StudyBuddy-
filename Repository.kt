package com.anix.studybuddy.data

import com.anix.studybuddy.model.*
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val db: AppDatabase) {

    // ── Exams ────────────────────────────────────────────────────────────────
    fun getAllExams(): Flow<List<Exam>> = db.examDao().getAllExams()
    fun getActiveExams(): Flow<List<Exam>> = db.examDao().getActiveExams()
    suspend fun insertExam(exam: Exam) = db.examDao().insertExam(exam)
    suspend fun updateExam(exam: Exam) = db.examDao().updateExam(exam)
    suspend fun deleteExam(exam: Exam) = db.examDao().deleteExam(exam)
    suspend fun activateExam(subject: String) = db.examDao().activateExam(subject)
    suspend fun getExamCount() = db.examDao().getCount()

    // ── Reminders ────────────────────────────────────────────────────────────
    fun getAllReminders(): Flow<List<Reminder>> = db.reminderDao().getAllReminders()
    suspend fun getEnabledReminders() = db.reminderDao().getEnabledReminders()
    suspend fun insertReminder(reminder: Reminder) = db.reminderDao().insertReminder(reminder)
    suspend fun updateReminder(reminder: Reminder) = db.reminderDao().updateReminder(reminder)
    suspend fun deleteReminder(reminder: Reminder) = db.reminderDao().deleteReminder(reminder)
    suspend fun deleteReminderById(id: Int) = db.reminderDao().deleteById(id)

    // ── Chapters ─────────────────────────────────────────────────────────────
    fun getAllChapters(): Flow<List<Chapter>> = db.chapterDao().getAllChapters()
    fun getChaptersBySubject(subject: String): Flow<List<Chapter>> =
        db.chapterDao().getChaptersBySubject(subject)
    suspend fun insertChapter(chapter: Chapter) = db.chapterDao().insertChapter(chapter)
    suspend fun updateChapter(chapter: Chapter) = db.chapterDao().updateChapter(chapter)
    suspend fun deleteChapter(chapter: Chapter) = db.chapterDao().deleteChapter(chapter)

    // ── Timetable ────────────────────────────────────────────────────────────
    fun getAllSlots(): Flow<List<TimetableSlot>> = db.timetableDao().getAllSlots()
    fun getSlotsByDay(day: Int): Flow<List<TimetableSlot>> = db.timetableDao().getSlotsByDay(day)
    suspend fun insertSlot(slot: TimetableSlot) = db.timetableDao().insertSlot(slot)
    suspend fun updateSlot(slot: TimetableSlot) = db.timetableDao().updateSlot(slot)
    suspend fun deleteSlot(slot: TimetableSlot) = db.timetableDao().deleteSlot(slot)
}
