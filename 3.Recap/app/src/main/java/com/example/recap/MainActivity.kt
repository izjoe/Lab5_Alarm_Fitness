@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.example.recap

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.recap.ui.theme.RecapTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var statusText by mutableStateOf("Ready. Select a task to begin.")
    private var displayedCount by mutableIntStateOf(0)
    private var isBound by mutableStateOf(false)
    private var isAsyncTaskRunning by mutableStateOf(false)

    private var boundService: CountingBoundService? = null
    private var bindRequested = false
    private var boundUpdatesJob: Job? = null
    private var countingAsyncTask: CountingAsyncTask? = null

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            launchForegroundService(canShowNotification = granted)
        }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            boundService = (binder as CountingBoundService.LocalBinder).getService()
            isBound = true
            statusText = "Bound service connected. Count updates use coroutines."
            observeBoundCount()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundUpdatesJob?.cancel()
            boundService = null
            isBound = false
            bindRequested = false
            statusText = "Bound service disconnected."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecapTheme {
                RecapExerciseScreen(
                    status = statusText,
                    count = displayedCount,
                    isBound = isBound,
                    isAsyncTaskRunning = isAsyncTaskRunning,
                    onStartBackgroundTask = ::startBackgroundTask,
                    onStartForegroundService = ::requestAndStartForegroundService,
                    onBindService = ::bindToCountingService,
                    onStopForegroundService = ::stopForegroundService,
                    onGetCurrentCount = ::getBoundCountOnce,
                    onUnbindService = ::unbindCountingService,
                    onRunAsyncTask = ::runAsyncTask
                )
            }
        }
    }

    private fun startBackgroundTask() {
        startService(Intent(this, BackgroundTaskService::class.java))
        statusText = "Background service started. Follow progress in Logcat."
        displayedCount = 0
    }

    private fun requestAndStartForegroundService() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            launchForegroundService()
        }
    }

    private fun launchForegroundService(canShowNotification: Boolean = true) {
        ContextCompat.startForegroundService(
            this,
            Intent(this, CountingForegroundService::class.java)
        )
        statusText = if (canShowNotification) {
            "Foreground service started. Check its progress notification."
        } else {
            "Foreground service started. Notification permission was denied, so its alert may be hidden."
        }
    }

    private fun stopForegroundService() {
        stopService(Intent(this, CountingForegroundService::class.java))
        statusText = "Foreground service stopped."
    }

    private fun bindToCountingService() {
        if (bindRequested) {
            statusText = "Bound service is already connected or connecting."
            return
        }

        bindRequested = bindService(
            Intent(this, CountingBoundService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        statusText = if (bindRequested) {
            "Connecting to bound service..."
        } else {
            "Could not bind to the counting service."
        }
    }

    private fun observeBoundCount() {
        boundUpdatesJob?.cancel()
        boundUpdatesJob = lifecycleScope.launch {
            while (isBound) {
                displayedCount = withContext(Dispatchers.Default) {
                    boundService?.getCurrentCount() ?: 0
                }
                delay(500)
            }
        }
    }

    private fun getBoundCountOnce() {
        if (!isBound) {
            statusText = "Bind to the service before retrieving its count."
            return
        }

        lifecycleScope.launch {
            val count = withContext(Dispatchers.Default) {
                boundService?.getCurrentCount() ?: 0
            }
            displayedCount = count
            statusText = "Coroutine retrieved the current bound count: $count."
        }
    }

    private fun unbindCountingService() {
        if (!bindRequested) {
            statusText = "No bound service connection to close."
            return
        }

        boundUpdatesJob?.cancel()
        unbindService(serviceConnection)
        bindRequested = false
        isBound = false
        boundService = null
        statusText = "Bound service unbound safely."
    }

    private fun runAsyncTask() {
        if (isAsyncTaskRunning) {
            return
        }

        countingAsyncTask = CountingAsyncTask(
            onStart = {
                isAsyncTaskRunning = true
                displayedCount = 0
                statusText = "AsyncTask started (deprecated learning demo)."
            },
            onProgress = { count ->
                displayedCount = count
                statusText = "AsyncTask progress: $count/10"
            },
            onComplete = { result ->
                isAsyncTaskRunning = false
                statusText = result
                countingAsyncTask = null
            },
            onCancelled = {
                isAsyncTaskRunning = false
                countingAsyncTask = null
            }
        ).also { it.execute() }
    }

    override fun onDestroy() {
        countingAsyncTask?.cancel(true)
        boundUpdatesJob?.cancel()
        if (bindRequested) {
            unbindService(serviceConnection)
            bindRequested = false
        }
        super.onDestroy()
    }

    private class CountingAsyncTask(
        private val onStart: () -> Unit,
        private val onProgress: (Int) -> Unit,
        private val onComplete: (String) -> Unit,
        private val onCancelled: () -> Unit
    ) : AsyncTask<Void, Int, String>() {
        override fun onPreExecute() {
            onStart()
        }

        override fun doInBackground(vararg params: Void?): String {
            for (count in 1..10) {
                if (isCancelled) {
                    return "AsyncTask cancelled."
                }
                Thread.sleep(400)
                publishProgress(count)
            }
            return "AsyncTask complete. Count reached 10."
        }

        override fun onProgressUpdate(vararg values: Int?) {
            values.firstOrNull()?.let(onProgress)
        }

        override fun onPostExecute(result: String?) {
            onComplete(result ?: "AsyncTask complete.")
        }

        override fun onCancelled() {
            onCancelled.invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecapExerciseScreen(
    status: String,
    count: Int,
    isBound: Boolean,
    isAsyncTaskRunning: Boolean,
    onStartBackgroundTask: () -> Unit,
    onStartForegroundService: () -> Unit,
    onBindService: () -> Unit,
    onStopForegroundService: () -> Unit,
    onGetCurrentCount: () -> Unit,
    onUnbindService: () -> Unit,
    onRunAsyncTask: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lab 5 Recap Exercise") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Android Services, AsyncTask, and Coroutines",
                style = MaterialTheme.typography.titleMedium
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Status", style = MaterialTheme.typography.labelLarge)
                    Text(text = status)
                    HorizontalDivider()
                    Text(
                        text = "Current count: $count",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Required service demos", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onStartBackgroundTask, modifier = Modifier.fillMaxWidth()) {
                Text("Start Background Task")
            }
            Button(onClick = onStartForegroundService, modifier = Modifier.fillMaxWidth()) {
                Text("Start Foreground Service")
            }
            Button(onClick = onBindService, modifier = Modifier.fillMaxWidth()) {
                Text(if (isBound) "Bound to Service" else "Bind to Service")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(text = "Interaction and async demos", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onGetCurrentCount, modifier = Modifier.weight(1f)) {
                    Text("Get Count")
                }
                OutlinedButton(onClick = onUnbindService, modifier = Modifier.weight(1f)) {
                    Text("Unbind")
                }
            }
            OutlinedButton(onClick = onStopForegroundService, modifier = Modifier.fillMaxWidth()) {
                Text("Stop Foreground Service")
            }
            Button(
                onClick = onRunAsyncTask,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isAsyncTaskRunning
            ) {
                Text(if (isAsyncTaskRunning) "AsyncTask Running..." else "Run AsyncTask Demo")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecapExercisePreview() {
    RecapTheme {
        RecapExerciseScreen(
            status = "Bound service connected. Count updates use coroutines.",
            count = 6,
            isBound = true,
            isAsyncTaskRunning = false,
            onStartBackgroundTask = {},
            onStartForegroundService = {},
            onBindService = {},
            onStopForegroundService = {},
            onGetCurrentCount = {},
            onUnbindService = {},
            onRunAsyncTask = {}
        )
    }
}
