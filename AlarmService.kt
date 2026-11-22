package com.anix.studybuddy.alarm

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.anix.studybuddy.MainActivity
import com.anix.studybuddy.R
import com.anix.studybuddy.receiver.AlarmReceiver

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "anix_alarm_channel"
        const val NOTIFICATION_ID = 1001
    }

    private var vibrator: Vibrator? = null
    private var ringtone: android.media.Ringtone? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reminderId = intent?.getIntExtra("reminder_id", 0) ?: 0
        val title = intent?.getStringExtra("title") ?: "Study Reminder"
        val subject = intent?.getStringExtra("subject") ?: ""
        val snoozeMinutes = intent?.getIntExtra("snooze_minutes", 10) ?: 10

        startForeground(NOTIFICATION_ID, buildNotification(reminderId, title, subject, snoozeMinutes))
        playAlarm()
        return START_NOT_STICKY
    }

    private fun playAlarm() {
        // Vibration pattern
        @Suppress("DEPRECATION")
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 500, 500, 500, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }

        // Ringtone
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(this, uri)
            ringtone?.play()
        } catch (_: Exception) {}
    }

    private fun buildNotification(reminderId: Int, title: String, subject: String, snoozeMinutes: Int): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = PendingIntent.getBroadcast(
            this, reminderId + 20_000,
            Intent(this, AlarmReceiver::class.java).apply {
                action = "SNOOZE"
                putExtra("reminder_id", reminderId)
                putExtra("title", title)
                putExtra("subject", subject)
                putExtra("snooze_minutes", snoozeMinutes)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getBroadcast(
            this, reminderId + 30_000,
            Intent(this, AlarmReceiver::class.java).apply {
                action = "DISMISS"
                putExtra("reminder_id", reminderId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("📚 $title")
            .setContentText(if (subject.isNotEmpty()) "Subject: $subject" else "Time to study!")
            .setFullScreenIntent(openIntent, true)
            .setContentIntent(openIntent)
            .addAction(0, "Snooze ${snoozeMinutes}m", snoozeIntent)
            .addAction(0, "Dismiss", dismissIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            CHANNEL_ID, "Study Alarms", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Anix Study Buddy alarms"
            setSound(soundUri, audioAttr)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 500, 500)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    override fun onDestroy() {
        ringtone?.stop()
        vibrator?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
