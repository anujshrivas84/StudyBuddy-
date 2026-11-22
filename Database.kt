package com.anix.studybuddy.data

import android.content.Context
import androidx.room.*
import com.anix.studybuddy.model.*
import kotlinx.coroutines.flow.Flow

// ─── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY dateEpochDay ASC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE isActive = 1 ORDER BY priority ASC, dateEpochDay ASC")
    fun getActiveExams(): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    @Query("UPDATE exams SET isActive = 1 WHERE subject = :subject")
    suspend fun activateExam(subject: String)

    @Query("SELECT COUNT(*) FROM exams")
    suspend fun getCount(): Int
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY triggerMillis ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getEnabledReminders(): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE subject = :subject ORDER BY id ASC")
    fun getChaptersBySubject(subject: String): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters ORDER BY subject ASC, id ASC")
    fun getAllChapters(): Flow<List<Chapter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable ORDER BY dayOfWeek ASC, startHour ASC")
    fun getAllSlots(): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable WHERE dayOfWeek = :day ORDER BY startHour ASC")
    fun getSlotsByDay(day: Int): Flow<List<TimetableSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: TimetableSlot): Long

    @Update
    suspend fun updateSlot(slot: TimetableSlot)

    @Delete
    suspend fun deleteSlot(slot: TimetableSlot)
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [Exam::class, Reminder::class, Chapter::class, TimetableSlot::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun examDao(): ExamDao
    abstract fun reminderDao(): ReminderDao
    abstract fun chapterDao(): ChapterDao
    abstract fun timetableDao(): TimetableDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anix_study_db"
                ).build().also { INSTANCE = it }
            }
    }
}
