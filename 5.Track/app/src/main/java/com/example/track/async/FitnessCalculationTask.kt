@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.example.track.async

import android.os.AsyncTask
import com.example.track.model.FitnessMetrics
import com.example.track.model.FitnessResult

// AsyncTask is intentionally retained because the lab explicitly requires its lifecycle demo.
class FitnessCalculationTask(
    private val callback: Callback
) : AsyncTask<Int, Void, FitnessResult>() {
    interface Callback {
        fun onCalculating()
        fun onCalculated(result: FitnessResult)
    }

    override fun onPreExecute() {
        callback.onCalculating()
    }

    override fun doInBackground(vararg steps: Int?): FitnessResult {
        return FitnessMetrics.fromSteps(steps.firstOrNull() ?: 0)
    }

    override fun onPostExecute(result: FitnessResult) {
        callback.onCalculated(result)
    }
}
