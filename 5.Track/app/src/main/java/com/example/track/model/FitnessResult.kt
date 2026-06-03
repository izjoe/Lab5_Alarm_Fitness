package com.example.track.model

data class FitnessResult(
    val steps: Int = 0,
    val calories: Double = 0.0,
    val points: Int = 0
)

object FitnessMetrics {
    fun fromSteps(steps: Int): FitnessResult {
        val safeSteps = steps.coerceAtLeast(0)
        return FitnessResult(
            steps = safeSteps,
            calories = safeSteps * 0.04,
            points = safeSteps / 100
        )
    }
}
