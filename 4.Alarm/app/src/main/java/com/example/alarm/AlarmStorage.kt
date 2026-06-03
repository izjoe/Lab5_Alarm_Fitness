package com.example.alarm

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class AlarmStorage(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getAlarms(): List<AlarmItem> {
        val json = preferences.getString(ALARMS_KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    add(array.getJSONObject(index).toAlarmItem())
                }
            }
        }.getOrDefault(emptyList()).sortedWith(compareBy(AlarmItem::hour, AlarmItem::minute))
    }

    fun nextId(): Int = (getAlarms().maxOfOrNull(AlarmItem::id) ?: 0) + 1

    fun getAlarm(id: Int): AlarmItem? = getAlarms().firstOrNull { it.id == id }

    fun saveAlarm(alarm: AlarmItem) {
        val alarms = getAlarms().filterNot { it.id == alarm.id } + alarm
        saveAlarms(alarms)
    }

    fun deleteAlarm(alarmId: Int) {
        saveAlarms(getAlarms().filterNot { it.id == alarmId })
    }

    private fun saveAlarms(alarms: List<AlarmItem>) {
        val array = JSONArray()
        alarms.forEach { alarm ->
            array.put(
                JSONObject()
                    .put("id", alarm.id)
                    .put("hour", alarm.hour)
                    .put("minute", alarm.minute)
                    .put("label", alarm.label)
                    .put("repeatDays", JSONArray(alarm.repeatDays.sorted()))
                    .put("isEnabled", alarm.isEnabled)
            )
        }
        preferences.edit().putString(ALARMS_KEY, array.toString()).apply()
    }

    private fun JSONObject.toAlarmItem(): AlarmItem {
        val dayArray = optJSONArray("repeatDays") ?: JSONArray()
        val days = buildSet {
            for (index in 0 until dayArray.length()) {
                add(dayArray.getInt(index))
            }
        }
        return AlarmItem(
            id = getInt("id"),
            hour = getInt("hour"),
            minute = getInt("minute"),
            label = optString("label", DEFAULT_LABEL),
            repeatDays = days,
            isEnabled = optBoolean("isEnabled", true)
        )
    }

    companion object {
        const val DEFAULT_LABEL = "A New Alarm!"
        private const val PREFERENCES_NAME = "alarm_preferences"
        private const val ALARMS_KEY = "saved_alarms"
    }
}
