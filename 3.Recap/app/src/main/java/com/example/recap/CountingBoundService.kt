package com.example.recap

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountingBoundService : Service() {
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var countingJob: Job? = null

    @Volatile
    private var currentCount = 0

    inner class LocalBinder : Binder() {
        fun getService(): CountingBoundService = this@CountingBoundService
    }

    override fun onBind(intent: Intent?): IBinder {
        if (countingJob?.isActive != true) {
            startCounting()
        }
        return binder
    }

    fun getCurrentCount(): Int = currentCount

    private fun startCounting() {
        currentCount = 0
        countingJob = serviceScope.launch {
            Log.d(TAG, "Bound service counting started")
            for (count in 1..MAX_COUNT) {
                delay(STEP_DELAY_MILLIS)
                currentCount = count
                Log.d(TAG, "Bound service progress: $count/$MAX_COUNT")
            }
        }
    }

    override fun onDestroy() {
        countingJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "CountingBoundService"
        private const val MAX_COUNT = 100
        private const val STEP_DELAY_MILLIS = 500L
    }
}
