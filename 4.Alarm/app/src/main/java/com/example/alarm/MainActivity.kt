package com.example.alarm

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.ui.theme.AlarmTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var refreshVersion by mutableIntStateOf(0)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AlarmNotifications.createChannel(this)
        val storage = AlarmStorage(this)

        setContent {
            AlarmTheme {
                AlarmApp(
                    storage = storage,
                    refreshVersion = refreshVersion,
                    requestNotificationPermission = ::requestNotificationPermission
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshVersion++
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun AlarmApp(
    storage: AlarmStorage,
    refreshVersion: Int,
    requestNotificationPermission: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    var alarms by remember { mutableStateOf(storage.getAlarms()) }
    var editorAlarmId by rememberSaveable { mutableStateOf<Int?>(null) }
    var isCreating by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(refreshVersion) {
        alarms = storage.getAlarms()
    }

    val editedAlarm = editorAlarmId?.let { id -> alarms.firstOrNull { it.id == id } }
    if (isCreating || editedAlarm != null) {
        BackHandler {
            editorAlarmId = null
            isCreating = false
        }
        AlarmEditorScreen(
            alarm = editedAlarm,
            onBack = {
                editorAlarmId = null
                isCreating = false
            },
            onSave = { hour, minute, label, repeatDays ->
                val savedAlarm = AlarmItem(
                    id = editedAlarm?.id ?: storage.nextId(),
                    hour = hour,
                    minute = minute,
                    label = label.ifBlank { AlarmStorage.DEFAULT_LABEL },
                    repeatDays = repeatDays,
                    isEnabled = editedAlarm?.isEnabled ?: true
                )
                editedAlarm?.let { AlarmScheduler.cancelAlarm(context, it.id) }
                storage.saveAlarm(savedAlarm)
                if (savedAlarm.isEnabled) {
                    AlarmScheduler.scheduleAlarm(context, savedAlarm)
                    requestNotificationPermission()
                }
                alarms = storage.getAlarms()
                editorAlarmId = null
                isCreating = false
            },
            onDelete = editedAlarm?.let { existing ->
                {
                    AlarmScheduler.cancelAlarm(context, existing.id)
                    storage.deleteAlarm(existing.id)
                    alarms = storage.getAlarms()
                    editorAlarmId = null
                    isCreating = false
                }
            }
        )
    } else {
        AlarmListScreen(
            alarms = alarms,
            onAdd = { isCreating = true },
            onEdit = { editorAlarmId = it.id },
            onToggle = { alarm, enabled ->
                val updated = alarm.copy(isEnabled = enabled)
                storage.saveAlarm(updated)
                if (enabled) {
                    AlarmScheduler.scheduleAlarm(context, updated)
                    requestNotificationPermission()
                } else {
                    AlarmScheduler.cancelAlarm(context, updated.id)
                }
                alarms = storage.getAlarms()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmListScreen(
    alarms: List<AlarmItem>,
    onAdd: () -> Unit,
    onEdit: (AlarmItem) -> Unit,
    onToggle: (AlarmItem, Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Alarm", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Text(
                            text = "Schedule your launch times",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    StatusChip("MISSION CLOCK ONLINE")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("New Mission", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            EmptyAlarmList(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(alarms, key = AlarmItem::id) { alarm ->
                    AlarmCard(alarm = alarm, onEdit = onEdit, onToggle = onToggle)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
        shape = CircleShape,
        modifier = Modifier
            .padding(end = 16.dp)
            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), CircleShape)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyAlarmList(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Missions Assigned",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap 'New Mission' to start scheduling your launch sequence.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmItem,
    onEdit: (AlarmItem) -> Unit,
    onToggle: (AlarmItem, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(alarm) }
            .border(
                1.dp,
                if (alarm.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
                else Color.Transparent,
                RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (alarm.isEnabled) "ARMED" else "STANDBY",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = alarm.formattedTime(),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = alarm.label.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (alarm.isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val activeDays = alarm.repeatDays
                    alarmDayLabels.forEach { (day, name) ->
                        DayMiniChip(name = name.take(1), isActive = day in activeDays && alarm.isEnabled)
                    }
                }
            }
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggle(alarm, it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun DayMiniChip(name: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent,
                CircleShape
            )
            .border(
                0.5.dp,
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditorScreen(
    alarm: AlarmItem?,
    onBack: () -> Unit,
    onSave: (Int, Int, String, Set<Int>) -> Unit,
    onDelete: (() -> Unit)?
) {
    val now = remember { Calendar.getInstance() }
    var hour by remember(alarm?.id) { mutableIntStateOf(alarm?.hour ?: now.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember(alarm?.id) { mutableIntStateOf(alarm?.minute ?: now.get(Calendar.MINUTE)) }
    var label by remember(alarm?.id) { mutableStateOf(alarm?.label ?: AlarmStorage.DEFAULT_LABEL) }
    var repeatDays by remember(alarm?.id) { mutableStateOf(alarm?.repeatDays ?: emptySet()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Wake Mission", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { 
                        Text("BACK", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                EditorSection(title = "Launch Time") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                TimePickerDialog(
                                    context,
                                    { _, selectedHour, selectedMinute ->
                                        hour = selectedHour
                                        minute = selectedMinute
                                    },
                                    hour,
                                    minute,
                                    false
                                ).show()
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = border(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = formattedTime(hour, minute),
                                fontSize = 64.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "CALIBRATE SEQUENCE",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            item {
                EditorSection(title = "Mission Label") {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        placeholder = { Text(AlarmStorage.DEFAULT_LABEL) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            item {
                EditorSection(title = "Repeat Orbit") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        alarmDayLabels.take(4).forEach { (day, name) ->
                            MissionDayChip(
                                name = name,
                                selected = day in repeatDays,
                                onClick = {
                                    repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        alarmDayLabels.drop(4).forEach { (day, name) ->
                            MissionDayChip(
                                name = name,
                                selected = day in repeatDays,
                                onClick = {
                                    repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }

            item {
                EditorSection(title = "Mission Controls") {
                    Button(
                        onClick = { onSave(hour, minute, label, repeatDays) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Mission", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    
                    if (onDelete != null) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = border(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
                        ) {
                            Text("Abort Mission", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun MissionDayChip(name: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) border(MaterialTheme.colorScheme.secondary) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun border(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)

private fun AlarmItem.formattedTime(): String = formattedTime(hour, minute)

private fun formattedTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
}
