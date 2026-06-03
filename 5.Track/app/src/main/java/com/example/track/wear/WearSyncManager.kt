package com.example.track.wear

import android.content.Context
import android.util.Log
import com.example.track.model.Badge
import com.example.track.model.FitnessResult
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WearSyncManager(context: Context) {
    private val appContext = context.applicationContext

    fun syncProgress(progress: FitnessResult, badges: List<Badge>) {
        // A DataItem is persisted and delivered when a paired WearOS device reconnects.
        val request = PutDataMapRequest.create(PATH).run {
            dataMap.putInt(KEY_STEPS, progress.steps)
            dataMap.putDouble(KEY_CALORIES, progress.calories)
            dataMap.putInt(KEY_POINTS, progress.points)
            dataMap.putFloat(KEY_PROGRESS, (progress.steps / 10_000f).coerceAtMost(1f))
            dataMap.putStringArrayList(KEY_BADGES, ArrayList(badges.map { it.title }))
            dataMap.putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            asPutDataRequest().setUrgent()
        }

        try {
            Wearable.getDataClient(appContext).putDataItem(request)
                .addOnFailureListener { error ->
                    Log.w(TAG, "WearOS sync unavailable: ${error.message}")
                }
        } catch (error: RuntimeException) {
            Log.w(TAG, "WearOS Data Layer is not available on this device.", error)
        }
    }

    companion object {
        const val PATH = "/fitness_progress"
        const val KEY_STEPS = "steps"
        const val KEY_CALORIES = "calories"
        const val KEY_POINTS = "points"
        const val KEY_PROGRESS = "progress"
        const val KEY_BADGES = "badges"
        private const val KEY_UPDATED_AT = "updated_at"
        private const val TAG = "WearSyncManager"
    }
}
