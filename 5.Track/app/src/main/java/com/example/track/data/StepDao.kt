package com.example.track.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert
    suspend fun insertStep(step: StepEntity)

    @Query("SELECT COALESCE(SUM(steps), 0) FROM steps WHERE timestamp >= :startOfDay")
    suspend fun getTodaySteps(startOfDay: Long): Int

    @Query("SELECT COALESCE(SUM(steps), 0) FROM steps WHERE timestamp >= :startOfDay")
    fun observeTodaySteps(startOfDay: Long): Flow<Int>

    @Query("SELECT * FROM steps ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestStepEntry(): StepEntity?
}
