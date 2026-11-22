package com.anix.studybuddy.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anix.studybuddy.alarm.AlarmScheduler
import com.anix.studybuddy.data.AppDatabase
import kotlinx.coroutines.*

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            // Re-schedule all enabled reminders from DB
            val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            scope.launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val reminders = db.reminderDao().getEnabledReminders()
                    val now = System.currentTimeMillis()
                    for (reminder in reminders) {
                        if (reminder.triggerMillis > now) {
                            AlarmScheduler.schedule(context, reminder)
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
