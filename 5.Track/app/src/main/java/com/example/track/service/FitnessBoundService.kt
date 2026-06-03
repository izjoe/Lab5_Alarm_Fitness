package com.example.track.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.track.data.StepRepository
import com.example.track.model.FitnessResult

class FitnessBoundService : Service() {
    private val binder = LocalBinder()
    private lateinit var repository: StepRepository

    override fun onCreate() {
        super.onCreate()
        repository = StepRepository.from(this)
    }

    inner class LocalBinder : Binder() {
        fun getService(): FitnessBoundService = this@FitnessBoundService
    }

    // MainActivity invokes this suspend query inside a lifecycle-aware coroutine.
    suspend fun getCurrentProgress(): FitnessResult = repository.getTodayProgress()

    override fun onBind(intent: Intent?): IBinder = binder
}
