package com.example.track.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.track.MainActivity
import com.example.track.R
import com.example.track.data.StepRepository
import com.example.track.model.FitnessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StepForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: StepRepository
    private lateinit var notificationManager: NotificationManager
    private var observerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        repository = StepRepository.from(this)
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification(FitnessResult()))
        observeProgress()
        return START_STICKY
    }

    private fun observeProgress() {
        if (observerJob?.isActive == true) return
        // Room is the shared source of truth for near-real-time notification updates.
        observerJob = serviceScope.launch {
            repository.observeTodayProgress().collectLatest { progress ->
                notificationManager.notify(NOTIFICATION_ID, buildNotification(progress))
            }
        }
    }

    private fun buildNotification(progress: FitnessResult) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Fitness Quest Active")
            .setContentText("Steps: ${progress.steps} - ${motivation(progress.steps)}")
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()

    private fun motivation(steps: Int): String = when {
        steps >= 10_000 -> "Quest completed. Keep exploring!"
        steps >= 8_000 -> "The summit is close!"
        steps >= 5_000 -> "Halfway to glory!"
        else -> "Keep moving, hero!"
    }

    private fun createNotificationChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Active fitness tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live Fitness Quest progress"
            }
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.track.START_VISIBLE_TRACKING"
        const val ACTION_STOP = "com.example.track.STOP_VISIBLE_TRACKING"
        private const val CHANNEL_ID = "fitness_quest_tracking"
        private const val NOTIFICATION_ID = 42
    }
}
