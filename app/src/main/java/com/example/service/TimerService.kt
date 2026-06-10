package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private val activeTimers = mutableMapOf<Int, Long>()
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        const val CHANNEL_ID = "TimerServiceChannel"
        const val ACTION_START_TIMER = "ACTION_START_TIMER"
        const val ACTION_STOP_TIMER = "ACTION_STOP_TIMER"
        const val ACTION_DELETE_TIMER = "ACTION_DELETE_TIMER"
        
        const val EXTRA_ROUTINE_ID = "EXTRA_ROUTINE_ID"
        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_NAME = "EXTRA_NAME"
        const val EXTRA_AUDIO_URI = "EXTRA_AUDIO_URI"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_TIMER -> {
                    val id = it.getIntExtra(EXTRA_ROUTINE_ID, -1)
                    val duration = it.getLongExtra(EXTRA_DURATION, 0)
                    val name = it.getStringExtra(EXTRA_NAME) ?: "Timer"
                    val audioUri = it.getStringExtra(EXTRA_AUDIO_URI)
                    
                    if (id != -1 && !activeTimers.containsKey(id)) {
                        startTimer(id, name, duration, audioUri)
                    }
                    
                    startForeground(1, buildNotification("Active Timers: ${activeTimers.size}"))
                }
                ACTION_STOP_TIMER -> {
                    val id = it.getIntExtra(EXTRA_ROUTINE_ID, -1)
                    if (id != -1) {
                        activeTimers.remove(id)
                        updateNotification()
                    }
                }
                ACTION_DELETE_TIMER -> {
                    val id = it.getIntExtra(EXTRA_ROUTINE_ID, -1)
                    if (id != -1) {
                        activeTimers.remove(id)
                        updateNotification()
                    }
                }
            }
        }
        
        if (activeTimers.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        
        return START_STICKY
    }

    private fun startTimer(id: Int, name: String, duration: Long, audioUri: String?) {
        activeTimers[id] = duration
        serviceScope.launch {
            while (activeTimers[id] ?: 0L > 0) {
                delay(1000)
                val remaining = activeTimers[id] ?: continue
                activeTimers[id] = remaining - 1
                updateNotification()
            }
            if (activeTimers.containsKey(id)) {
                // Done
                activeTimers.remove(id)
                updateNotification()
                playRingtone(audioUri)
            }
        }
    }

    private fun playRingtone(uriString: String?) {
        try {
            mediaPlayer?.release()
            val uri = if (uriString != null) {
                Uri.parse(uriString)
            } else {
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            }
            mediaPlayer = MediaPlayer.create(this, uri)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateNotification() {
        if (activeTimers.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val activeCount = activeTimers.size
        // just find first timer to show something
        val firstTimerStr = activeTimers.entries.firstOrNull()?.let { 
            val h = it.value / 3600
            val m = (it.value % 3600) / 60
            val s = it.value % 60
            String.format("Highest priority timer: %02d:%02d:%02d", h, m, s)
        } ?: ""

        notificationManager.notify(1, buildNotification("$activeCount Timer(s) Running", firstTimerStr))
    }

    private fun buildNotification(title: String, content: String = ""): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            // Use standard Android icon if we don't have a custom one
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
