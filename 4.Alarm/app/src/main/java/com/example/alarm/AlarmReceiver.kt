package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        if (alarmId == -1) return

        val storage = AlarmStorage(context)
        val alarm = storage.getAlarm(alarmId) ?: return
        if (!alarm.isEnabled) return

        AlarmNotifications.show(context, alarm)
        if (alarm.repeatDays.isEmpty()) {
            storage.saveAlarm(alarm.copy(isEnabled = false))
        } else {
            AlarmScheduler.scheduleAlarm(context, alarm)
        }
    }
}
