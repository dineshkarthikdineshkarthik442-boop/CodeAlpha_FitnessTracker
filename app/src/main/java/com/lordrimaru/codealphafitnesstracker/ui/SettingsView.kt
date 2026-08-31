package com.lordrimaru.codealphafitnesstracker.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lordrimaru.codealphafitnesstracker.notifications.NotificationHelper
import com.lordrimaru.codealphafitnesstracker.viewmodel.FitnessViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(viewModel: FitnessViewModel) {
    val prefs by viewModel.userPreferences.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            viewModel.updatePreferences { it.copy(reminderHour = hour, reminderMinute = minute) }
            if (prefs.dailyReminderEnabled) {
                NotificationHelper.scheduleReminder(context, hour, minute)
            }
        },
        prefs.reminderHour,
        prefs.reminderMinute,
        false
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.updatePreferences { it.copy(dailyReminderEnabled = true) }
            NotificationHelper.scheduleReminder(context, prefs.reminderHour, prefs.reminderMinute)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        // Appearance
        SettingsSection(title = "Appearance", icon = Icons.Default.Settings) {
            Text("Theme", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("System", "Light", "Dark").forEach { theme ->
                    FilterChip(
                        selected = prefs.theme == theme,
                        onClick = { viewModel.updatePreferences { it.copy(theme = theme) } },
                        label = { Text(theme) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Goals
        SettingsSection(title = "Fitness Goals", icon = Icons.Default.Person) {
            GoalSettingItem(
                label = "Daily Steps Goal",
                value = prefs.dailyStepsGoal.toString(),
                onValueChange = { newVal ->
                    newVal.toIntOrNull()?.let { 
                        if (it > 0) viewModel.updatePreferences { p -> p.copy(dailyStepsGoal = it) }
                    }
                }
            )
            GoalSettingItem(
                label = "Weekly Workout Goal",
                value = prefs.weeklyWorkoutGoal.toString(),
                onValueChange = { newVal ->
                    newVal.toIntOrNull()?.let { 
                        if (it > 0) viewModel.updatePreferences { p -> p.copy(weeklyWorkoutGoal = it) }
                    }
                }
            )
            GoalSettingItem(
                label = "Weekly Calories Goal",
                value = prefs.weeklyCaloriesGoal.toString(),
                onValueChange = { newVal ->
                    newVal.toIntOrNull()?.let { 
                        if (it > 0) viewModel.updatePreferences { p -> p.copy(weeklyCaloriesGoal = it) }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notifications
        SettingsSection(title = "Notifications", icon = Icons.Default.Notifications) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily Reminder")
                Switch(
                    checked = prefs.dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        viewModel.updatePreferences { it.copy(dailyReminderEnabled = true) }
                                        NotificationHelper.scheduleReminder(context, prefs.reminderHour, prefs.reminderMinute)
                                    }
                                    else -> permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                viewModel.updatePreferences { it.copy(dailyReminderEnabled = true) }
                                NotificationHelper.scheduleReminder(context, prefs.reminderHour, prefs.reminderMinute)
                            }
                        } else {
                            viewModel.updatePreferences { it.copy(dailyReminderEnabled = false) }
                            NotificationHelper.cancelReminder(context)
                        }
                    }
                )
            }
            if (prefs.dailyReminderEnabled) {
                OutlinedButton(
                    onClick = { timePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Reminder Time: ${String.format(Locale.getDefault(), "%02d:%02d", prefs.reminderHour, prefs.reminderMinute)}"
                    )
                }
                Text(
                    text = "You will receive a notification daily to check your progress.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About
        SettingsSection(title = "About", icon = Icons.Default.Info) {
            Text("CodeAlpha Fit", fontWeight = FontWeight.Bold)
            Text("Version 4.0 (Final Professional Release)", style = MaterialTheme.typography.bodySmall)
            Text("Developed by Dinesh Karthik S", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun GoalSettingItem(label: String, value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(100.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
