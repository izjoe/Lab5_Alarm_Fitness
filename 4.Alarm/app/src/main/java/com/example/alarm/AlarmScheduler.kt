package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {
    const val EXTRA_ALARM_ID = "alarm_id"
    private const val ACTION_ALARM = "com.example.alarm.ACTION_ALARM"

    fun scheduleAlarm(context: Context, alarm: AlarmItem) {
        if (!alarm.isEnabled) return
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = pendingIntent(context, alarm.id)
        val triggerAt = calculateNextTriggerTime(alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, alarmId))
    }

    fun rescheduleEnabledAlarms(context: Context) {
        AlarmStorage(context).getAlarms()
            .filter(AlarmItem::isEnabled)
            .forEach { scheduleAlarm(context, it) }
    }

    fun calculateNextTriggerTime(alarm: AlarmItem, nowMillis: Long = System.currentTimeMillis()): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val candidate = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (alarm.repeatDays.isEmpty()) {
            if (candidate.timeInMillis <= nowMillis) {
                candidate.add(Calendar.DATE, 1)
            }
            return candidate.timeInMillis
        }

        for (offset in 0..7) {
            candidate.timeInMillis = nowMillis
            candidate.set(Calendar.HOUR_OF_DAY, alarm.hour)
            candidate.set(Calendar.MINUTE, alarm.minute)
            candidate.set(Calendar.SECOND, 0)
            candidate.set(Calendar.MILLISECOND, 0)
            candidate.add(Calendar.DATE, offset)

            if (candidate.dayNumber() in alarm.repeatDays && candidate.timeInMillis > nowMillis) {
                return candidate.timeInMillis
            }
        }

        return candidate.timeInMillis
    }

    private fun pendingIntent(context: Context, alarmId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "$ACTION_ALARM.$alarmId"
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun Calendar.dayNumber(): Int = when (get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        else -> 7
    }
}
