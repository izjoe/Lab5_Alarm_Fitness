package com.example.alarm

data class AlarmItem(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: Set<Int>,
    val isEnabled: Boolean
)

val alarmDayLabels = listOf(
    1 to "MON",
    2 to "TUE",
    3 to "WED",
    4 to "THU",
    5 to "FRI",
    6 to "SAT",
    7 to "SUN"
)

fun AlarmItem.repeatDescription(): String {
    if (repeatDays.isEmpty()) return "Once"
    if (repeatDays.size == alarmDayLabels.size) return "Every day"
    return alarmDayLabels
        .filter { (day, _) -> day in repeatDays }
        .joinToString(" ") { (_, name) -> name }
}
