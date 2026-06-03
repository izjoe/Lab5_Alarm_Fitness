package com.example.track

import com.example.track.model.Badge
import com.example.track.model.FitnessMetrics
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun calculations_follow_lab_formulas() {
        val result = FitnessMetrics.fromSteps(8_000)

        assertEquals(8_000, result.steps)
        assertEquals(320.0, result.calories, 0.001)
        assertEquals(80, result.points)
    }

    @Test
    fun badges_unlock_at_their_milestones() {
        assertEquals(
            listOf(Badge.BEGINNER, Badge.EXPLORER, Badge.CHAMPION),
            Badge.unlockedFor(10_000)
        )
        assertEquals(emptyList<Badge>(), Badge.unlockedFor(999))
    }
}
