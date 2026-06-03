package com.example.recap

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountingForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var countingJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(0))
        countingJob?.cancel()
        countingJob = serviceScope.launch {
            Log.d(TAG, "Foreground service started")
            for (count in 1..MAX_COUNT) {
                delay(STEP_DELAY_MILLIS)
                Log.d(TAG, "Foreground service progress: $count/$MAX_COUNT")
                notificationManager.notify(NOTIFICATION_ID, buildNotification(count))
            }
            Log.d(TAG, "Foreground service completed")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        countingJob?.cancel()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Foreground counting task",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows progress for the Lab 5 foreground service"
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(count: Int): Notification {
        val text = if (count == 0) {
            "Counting in progress..."
        } else {
            "Counting in progress... $count/$MAX_COUNT"
        }
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service Running")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(MAX_COUNT, count, false)
            .build()
    }

    companion object {
        private const val TAG = "CountingForeground"
        private const val CHANNEL_ID = "counting_foreground_channel"
        private const val NOTIFICATION_ID = 1001
        private const val MAX_COUNT = 10
        private const val STEP_DELAY_MILLIS = 750L
    }
}
