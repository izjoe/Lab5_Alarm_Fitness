package com.example.track.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.track.data.StepRepository
import com.example.track.model.Badge
import com.example.track.wear.WearSyncManager
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StepBackgroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: StepRepository
    private lateinit var wearSyncManager: WearSyncManager
    private var simulationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        repository = StepRepository.from(this)
        wearSyncManager = WearSyncManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startSimulation()
        return START_STICKY
    }

    private fun startSimulation() {
        if (simulationJob?.isActive == true) return
        // This service is the sole step producer, so notification observation cannot double count.
        simulationJob = serviceScope.launch {
            while (isActive) {
                delay(Random.nextLong(3_000, 5_001))
                val addedSteps = Random.nextInt(20, 151)
                val progress = repository.recordSimulatedSteps(addedSteps)
                Log.d(
                    TAG,
                    "Added $addedSteps steps. Total today: ${progress.steps}"
                )
                wearSyncManager.syncProgress(progress, Badge.unlockedFor(progress.steps))
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.example.track.START_BACKGROUND_TRACKING"
        const val ACTION_STOP = "com.example.track.STOP_BACKGROUND_TRACKING"
        private const val TAG = "StepBackgroundService"
    }
}
