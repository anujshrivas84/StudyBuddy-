package com.anix.studybuddy.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anix.studybuddy.model.Reminder
import com.anix.studybuddy.receiver.AlarmReceiver

object AlarmScheduler {

    fun schedule(context: Context, reminder: Reminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("title", reminder.title)
            putExtra("subject", reminder.subject)
            putExtra("snooze_minutes", reminder.snoozeMinutes)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(reminder.triggerMillis, pi), pi)
            }
        } else {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(reminder.triggerMillis, pi), pi)
        }
    }

    fun cancel(context: Context, reminderId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }

    fun scheduleSnooze(context: Context, reminderId: Int, title: String,
                       subject: String, snoozeMinutes: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = System.currentTimeMillis() + snoozeMinutes * 60_000L
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("title", title)
            putExtra("subject", subject)
            putExtra("snooze_minutes", snoozeMinutes)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            reminderId + 10_000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, pi), pi)
        }
    }
}
