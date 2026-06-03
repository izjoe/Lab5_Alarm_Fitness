package com.example.recap

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

class BackgroundTaskService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var countingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        countingJob?.cancel()
        countingJob = serviceScope.launch {
            Log.d(TAG, "Background task started")
            for (count in 1..MAX_COUNT) {
                delay(STEP_DELAY_MILLIS)
                Log.d(TAG, "Background task progress: $count/$MAX_COUNT")
            }
            Log.d(TAG, "Background task completed")
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        countingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BackgroundTaskService"
        private const val MAX_COUNT = 10
        private const val STEP_DELAY_MILLIS = 500L
    }
}
