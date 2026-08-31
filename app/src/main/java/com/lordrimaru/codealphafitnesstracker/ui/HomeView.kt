package com.lordrimaru.codealphafitnesstracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lordrimaru.codealphafitnesstracker.data.local.entity.WorkoutEntity
import com.lordrimaru.codealphafitnesstracker.ui.components.SummaryCard
import com.lordrimaru.codealphafitnesstracker.ui.components.UpdateStepsDialog
import com.lordrimaru.codealphafitnesstracker.ui.components.WorkoutFormDialog
import com.lordrimaru.codealphafitnesstracker.ui.components.WorkoutItem
import com.lordrimaru.codealphafitnesstracker.viewmodel.FitnessViewModel

@Composable
fun HomeView(viewModel: FitnessViewModel) {
    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var showStepsDialog by remember { mutableStateOf(false) }
    var editingWorkout by remember { mutableStateOf<WorkoutEntity?>(null) }

    val workouts by viewModel.todayWorkouts.collectAsState()
    val stats by viewModel.todayStats.collectAsState()
    val calories by viewModel.totalCaloriesBurned.collectAsState()
    val duration by viewModel.totalWorkoutDuration.collectAsState()
    val count by viewModel.workoutCount.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 20.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard(
                        title = "Steps",
                        value = (stats?.steps ?: 0).toString(),
                        unit = "/ ${prefs.dailyStepsGoal}",
                        progress = (stats?.steps ?: 0).toFloat() / prefs.dailyStepsGoal,
                        icon = Icons.Default.PlayArrow,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { showStepsDialog = true }
                    )
                    SummaryCard(
                        title = "Calories",
                        value = calories.toString(),
                        unit = "/ ${prefs.weeklyCaloriesGoal / 7} kcal",
                        progress = calories.toFloat() / (prefs.weeklyCaloriesGoal / 7).coerceAtLeast(1),
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard(
                        title = "Duration",
                        value = duration.toString(),
                        unit = "min total",
                        progress = duration / 60f,
                        icon = Icons.Default.Info,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Sessions",
                        value = count.toString(),
                        unit = "workouts",
                        progress = count / 3f,
                        icon = Icons.Default.Star,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Today's Workouts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (workouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No workouts recorded today.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(workouts, key = { it.id }) { workout ->
                        WorkoutItem(
                            workout = workout,
                            onEdit = { editingWorkout = it },
                            onDelete = { viewModel.deleteWorkout(it) }
                        )
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            text = { Text("Log Workout") },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            onClick = { showAddWorkoutDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddWorkoutDialog) {
        WorkoutFormDialog(
            onDismiss = { showAddWorkoutDialog = false },
            onConfirm = { type, category, dur, cal ->
                viewModel.addWorkout(type, category, dur, cal)
                showAddWorkoutDialog = false
            }
        )
    }

    editingWorkout?.let { workout ->
        WorkoutFormDialog(
            workout = workout,
            onDismiss = { editingWorkout = null },
            onConfirm = { type, category, dur, cal ->
                viewModel.updateWorkout(workout.copy(exerciseType = type, category = category, durationMinutes = dur, caloriesBurned = cal))
                editingWorkout = null
            }
        )
    }

    if (showStepsDialog) {
        UpdateStepsDialog(
            currentSteps = stats?.steps ?: 0,
            onDismiss = { showStepsDialog = false },
            onConfirm = { steps ->
                viewModel.updateSteps(steps)
                showStepsDialog = false
            }
        )
    }
}
