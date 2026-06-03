package com.example.track

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.track.ai.CoachSuggestion
import com.example.track.ai.GeminiFitnessCoach
import com.example.track.data.StepRepository
import com.example.track.model.Badge
import com.example.track.model.FitnessResult
import com.example.track.service.StepBackgroundService
import com.example.track.service.StepForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val tracking: Boolean = false,
    val calculationMessage: String = "Tap Get Current Progress to run AsyncTask.",
    val coachSuggestion: CoachSuggestion? = null,
    val loadingCoach: Boolean = false
)

class FitnessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StepRepository.from(application)
    private val coach = GeminiFitnessCoach()
    private val mutableUiState = MutableStateFlow(DashboardUiState())

    val uiState: StateFlow<DashboardUiState> = mutableUiState.asStateFlow()
    val progress: StateFlow<FitnessResult> = repository.observeTodayProgress().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FitnessResult()
    )

    fun startTracking() {
        val context = getApplication<Application>()
        context.startService(
            Intent(context, StepBackgroundService::class.java)
                .setAction(StepBackgroundService.ACTION_START)
        )
        ContextCompat.startForegroundService(
            context,
            Intent(context, StepForegroundService::class.java)
                .setAction(StepForegroundService.ACTION_START)
        )
        mutableUiState.value = mutableUiState.value.copy(tracking = true)
    }

    fun stopTracking() {
        val context = getApplication<Application>()
        context.startService(
            Intent(context, StepBackgroundService::class.java)
                .setAction(StepBackgroundService.ACTION_STOP)
        )
        context.startService(
            Intent(context, StepForegroundService::class.java)
                .setAction(StepForegroundService.ACTION_STOP)
        )
        mutableUiState.value = mutableUiState.value.copy(tracking = false)
    }

    suspend fun loadCurrentProgress(): FitnessResult = repository.getTodayProgress()

    fun onCalculationStarted() {
        mutableUiState.value = mutableUiState.value.copy(
            calculationMessage = "AsyncTask is calculating calories and points..."
        )
    }

    fun onCalculationComplete(result: FitnessResult) {
        mutableUiState.value = mutableUiState.value.copy(
            calculationMessage = "AsyncTask result: %.1f kcal and %d points.".format(
                result.calories,
                result.points
            )
        )
    }

    fun requestCoachSuggestion() {
        val current = progress.value
        mutableUiState.value = mutableUiState.value.copy(loadingCoach = true)
        viewModelScope.launch {
            val suggestion = coach.requestSuggestion(current, Badge.unlockedFor(current.steps))
            mutableUiState.value = mutableUiState.value.copy(
                coachSuggestion = suggestion,
                loadingCoach = false
            )
        }
    }
}
