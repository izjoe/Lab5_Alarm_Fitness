package com.example.track.data

import android.content.Context
import com.example.track.model.FitnessMetrics
import com.example.track.model.FitnessResult
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StepRepository(private val dao: StepDao) {
    suspend fun recordSimulatedSteps(stepDelta: Int): FitnessResult {
        val entryMetrics = FitnessMetrics.fromSteps(stepDelta)
        dao.insertStep(
            StepEntity(
                timestamp = System.currentTimeMillis(),
                steps = entryMetrics.steps,
                calories = entryMetrics.calories,
                points = entryMetrics.points
            )
        )
        return getTodayProgress()
    }

    suspend fun getTodayProgress(): FitnessResult {
        return FitnessMetrics.fromSteps(dao.getTodaySteps(startOfToday()))
    }

    fun observeTodayProgress(): Flow<FitnessResult> {
        return dao.observeTodaySteps(startOfToday()).map(FitnessMetrics::fromSteps)
    }

    suspend fun getLatestStepEntry(): StepEntity? = dao.getLatestStepEntry()

    private fun startOfToday(): Long = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }

    companion object {
        fun from(context: Context): StepRepository {
            return StepRepository(FitnessDatabase.getInstance(context).stepDao())
        }
    }
}
