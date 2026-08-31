package com.lordrimaru.codealphafitnesstracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lordrimaru.codealphafitnesstracker.data.local.entity.WorkoutEntity
import com.lordrimaru.codealphafitnesstracker.viewmodel.FitnessViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProgressView(viewModel: FitnessViewModel, navController: NavController) {
    val weekStart by viewModel.currentWeekStart.collectAsState()
    val weeklyWorkouts by viewModel.weeklyWorkouts.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val summary by viewModel.weeklySummary.collectAsState()
    val goals by viewModel.goalProgress.collectAsState()
    val prefs by viewModel.userPreferences.collectAsState()
    
    val weekEnd = weekStart.plusDays(6)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Week Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Week")
                }
                Text(
                    text = "${weekStart.format(dateFormatter)} - ${weekEnd.format(dateFormatter)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.nextWeek() },
                    enabled = viewModel.isNextWeekEnabled()
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Week")
                }
            }
        }

        // Achievements Button
        item {
            Button(
                onClick = { navController.navigate(Screen.Achievements.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Achievements")
            }
        }

        // Goals Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Weekly Goal Progress", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    GoalItem("Daily Steps", "${(viewModel.todayStats.value?.steps ?: 0)} / ${prefs.dailyStepsGoal}", goals.stepsProgress, MaterialTheme.colorScheme.primary)
                    GoalItem("Weekly Workouts", "${summary.totalWorkouts} / ${prefs.weeklyWorkoutGoal}", goals.workoutProgress, Color(0xFF4CAF50))
                    GoalItem("Weekly Calories", "${summary.totalCalories} / ${prefs.weeklyCaloriesGoal}", goals.caloriesProgress, Color(0xFFF44336))
                }
            }
        }

        // Weekly Summary Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Weekly Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniSummaryCard("Avg. Duration", "${summary.avgDuration}m", Modifier.weight(1f))
                    MiniSummaryCard("Avg. Calories", "${summary.avgCalories}k", Modifier.weight(1f))
                    MiniSummaryCard("Total Steps", "${summary.totalSteps}", Modifier.weight(1f))
                }
            }
        }

        // Chart Section
        item {
            var chartMode by remember { mutableStateOf(ChartMode.DURATION) }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activity Chart", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        ChartToggleButton("Min", chartMode == ChartMode.DURATION) { chartMode = ChartMode.DURATION }
                        ChartToggleButton("Kcal", chartMode == ChartMode.CALORIES) { chartMode = ChartMode.CALORIES }
                    }
                }
                
                WeeklyChart(
                    weekStart = weekStart,
                    workouts = weeklyWorkouts,
                    mode = chartMode
                )
            }
        }

        // Daily Breakdown
        item {
            Text("Daily Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        if (weeklyWorkouts.isEmpty() && weeklyStats.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No activity recorded this week.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            val days = (0..6).map { weekStart.plusDays(it.toLong()) }
            items(days) { day ->
                val dayWorkouts = weeklyWorkouts.filter { it.date == day.toString() }
                val dayStats = weeklyStats.find { it.date == day.toString() }
                
                DailyBreakdownItem(
                    date = day,
                    workouts = dayWorkouts,
                    steps = dayStats?.steps ?: 0
                )
            }
        }
    }
}

@Composable
fun GoalItem(label: String, value: String, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun MiniSummaryCard(label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun ChartToggleButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun WeeklyChart(weekStart: LocalDate, workouts: List<WorkoutEntity>, mode: ChartMode) {
    val maxVal = when (mode) {
        ChartMode.DURATION -> workouts.groupBy { it.date }.map { it.value.sumOf { w -> w.durationMinutes } }.maxOrNull()?.coerceAtLeast(60) ?: 60
        ChartMode.CALORIES -> workouts.groupBy { it.date }.map { it.value.sumOf { w -> w.caloriesBurned } }.maxOrNull()?.coerceAtLeast(500) ?: 500
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val daysLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        (0..6).forEach { i ->
            val date = weekStart.plusDays(i.toLong()).toString()
            val dayValue = when (mode) {
                ChartMode.DURATION -> workouts.filter { it.date == date }.sumOf { it.durationMinutes }
                ChartMode.CALORIES -> workouts.filter { it.date == date }.sumOf { it.caloriesBurned }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height((200 * (dayValue.toFloat() / maxVal.toFloat())).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (dayValue > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(daysLabels[i], style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun DailyBreakdownItem(date: LocalDate, workouts: List<WorkoutEntity>, steps: Int) {
    val duration = workouts.sumOf { it.durationMinutes }
    val calories = workouts.sumOf { it.caloriesBurned }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("${workouts.size} workouts • ${duration}m • ${calories}kcal", style = MaterialTheme.typography.bodyMedium)
                Text("${steps} steps", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

enum class ChartMode { DURATION, CALORIES }
