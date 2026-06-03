package com.example.track

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.track.async.FitnessCalculationTask
import com.example.track.model.FitnessResult
import com.example.track.service.FitnessBoundService
import com.example.track.ui.FitnessDashboardScreen
import com.example.track.ui.theme.TrackTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    private val viewModel: FitnessViewModel by viewModels()
    private var boundService: FitnessBoundService? = null
    private var serviceIsBound = false
    private var calculationTask: FitnessCalculationTask? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.startTracking() }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            boundService = (binder as FitnessBoundService.LocalBinder).getService()
            serviceIsBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
            serviceIsBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bindService(
            Intent(this, FitnessBoundService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )

        setContent {
            TrackTheme {
                val progress = viewModel.progress.collectAsStateWithLifecycle().value
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                FitnessDashboardScreen(
                    progress = progress,
                    uiState = uiState,
                    onStartTracking = ::requestNotificationThenStart,
                    onStopTracking = viewModel::stopTracking,
                    onRetrieveProgress = ::retrieveProgressFromBoundService,
                    onRequestCoach = viewModel::requestCoachSuggestion
                )
            }
        }
    }

    private fun requestNotificationThenStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.startTracking()
        }
    }

    private fun retrieveProgressFromBoundService() {
        lifecycleScope.launch {
            val snapshot = withContext(Dispatchers.IO) {
                boundService?.getCurrentProgress() ?: viewModel.loadCurrentProgress()
            }
            calculationTask?.cancel(true)
            calculationTask = FitnessCalculationTask(object : FitnessCalculationTask.Callback {
                override fun onCalculating() {
                    viewModel.onCalculationStarted()
                }

                override fun onCalculated(result: FitnessResult) {
                    viewModel.onCalculationComplete(result)
                }
            }).also { it.execute(snapshot.steps) }
        }
    }

    override fun onDestroy() {
        calculationTask?.cancel(true)
        if (serviceIsBound) {
            unbindService(serviceConnection)
            serviceIsBound = false
        }
        super.onDestroy()
    }
}
