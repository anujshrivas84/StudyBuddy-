package com.anix.studybuddy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anix.studybuddy.alarm.AlarmScheduler
import com.anix.studybuddy.alarm.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", 0)
        val title = intent.getStringExtra("title") ?: "Study Reminder"
        val subject = intent.getStringExtra("subject") ?: ""
        val snoozeMinutes = intent.getIntExtra("snooze_minutes", 10)

        when (intent.action) {
            "SNOOZE" -> {
                // Stop current alarm service
                context.stopService(Intent(context, AlarmService::class.java))
                // Schedule snooze
                AlarmScheduler.scheduleSnooze(context, reminderId, title, subject, snoozeMinutes)
            }
            "DISMISS" -> {
                context.stopService(Intent(context, AlarmService::class.java))
            }
            else -> {
                // Fire the alarm
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra("reminder_id", reminderId)
                    putExtra("title", title)
                    putExtra("subject", subject)
                    putExtra("snooze_minutes", snoozeMinutes)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
