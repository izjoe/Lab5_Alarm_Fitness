package com.example.track.ai

import com.example.track.BuildConfig
import com.example.track.model.Badge
import com.example.track.model.FitnessResult
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class CoachSuggestion(
    val text: String,
    val isConfigured: Boolean = true
)

class GeminiFitnessCoach {
    suspend fun requestSuggestion(
        progress: FitnessResult,
        badges: List<Badge>
    ): CoachSuggestion = withContext(Dispatchers.IO) {
        // Injected at build time for this lab; production apps should call Gemini through a backend.
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext CoachSuggestion(
                text = "Add GEMINI_API_KEY to Gradle properties to receive a personalized quest.",
                isConfigured = false
            )
        }

        runCatching {
            val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 20_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-goog-api-key", apiKey)
            }
            val requestBody = JSONObject().put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", buildPrompt(progress, badges)))
                    )
                )
            )
            connection.outputStream.bufferedWriter().use { it.write(requestBody.toString()) }
            val responseBody = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val details = connection.errorStream?.bufferedReader()?.use { it.readText() }
                error("Gemini request failed (${connection.responseCode}): ${details.orEmpty()}")
            }
            connection.disconnect()
            parseResponse(responseBody)
        }.getOrElse { error ->
            CoachSuggestion("Coach is unavailable right now: ${error.message ?: "network error"}")
        }
    }

    private fun buildPrompt(progress: FitnessResult, badges: List<Badge>): String {
        val badgeText = badges.joinToString { it.title }.ifBlank { "None yet" }
        return """
            You are a friendly fitness coach. Give one short workout suggestion and one
            motivational encouragement message for a quest-themed fitness app.

            Steps: ${progress.steps}
            Calories burned: ${String.format(Locale.US, "%.1f", progress.calories)}
            Points earned: ${progress.points}
            Goal: 10000 steps
            Unlocked badges: $badgeText

            Return exactly:
            Workout suggestion: ...
            Encouragement: ...
        """.trimIndent()
    }

    private fun parseResponse(response: String): CoachSuggestion {
        val text = JSONObject(response)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
        return CoachSuggestion(text.trim())
    }

    companion object {
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    }
}
