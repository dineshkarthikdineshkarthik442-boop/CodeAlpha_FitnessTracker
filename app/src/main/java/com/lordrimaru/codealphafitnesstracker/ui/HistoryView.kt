package com.lordrimaru.codealphafitnesstracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lordrimaru.codealphafitnesstracker.data.local.entity.WorkoutEntity
import com.lordrimaru.codealphafitnesstracker.ui.components.WorkoutFormDialog
import com.lordrimaru.codealphafitnesstracker.ui.components.WorkoutItem
import com.lordrimaru.codealphafitnesstracker.viewmodel.FitnessViewModel
import com.lordrimaru.codealphafitnesstracker.viewmodel.HistoryFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryView(viewModel: FitnessViewModel) {
    val history by viewModel.filteredHistory.collectAsState()
    val currentFilter by viewModel.historyFilter.collectAsState()
    var editingWorkout by remember { mutableStateOf<WorkoutEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(history, searchQuery) {
        if (searchQuery.isEmpty()) history
        else history.filter { it.exerciseType.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Workout History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search workouts...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        ScrollableTabRow(
            selectedTabIndex = currentFilter.ordinal,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentFilter.ordinal]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            HistoryFilter.entries.forEach { filter ->
                Tab(
                    selected = currentFilter == filter,
                    onClick = { viewModel.setHistoryFilter(filter) },
                    text = { 
                        Text(
                            text = filter.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelLarge
                        ) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (searchQuery.isEmpty()) "No workout history yet." else "No matches found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val groupedWorkouts = filteredList.groupBy { it.date }
            val sortedDates = groupedWorkouts.keys.sortedDescending()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                sortedDates.forEach { date ->
                    item {
                        val formattedDate = try {
                            val localDate = LocalDate.parse(date)
                            val now = LocalDate.now()
                            when (localDate) {
                                now -> "Today"
                                now.minusDays(1) -> "Yesterday"
                                else -> localDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
                            }
                        } catch (e: Exception) {
                            date
                        }
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(groupedWorkouts[date] ?: emptyList(), key = { it.id }) { workout ->
                        WorkoutItem(
                            workout = workout,
                            onEdit = { editingWorkout = it },
                            onDelete = { viewModel.deleteWorkout(it) }
                        )
                    }
                }
            }
        }
    }

    editingWorkout?.let { workout ->
        WorkoutFormDialog(
            workout = workout,
            onDismiss = { editingWorkout = null },
            onConfirm = { type, category, dur, cal ->
                viewModel.updateWorkout(workout.copy(
                    exerciseType = type,
                    category = category,
                    durationMinutes = dur,
                    caloriesBurned = cal
                ))
                editingWorkout = null
            }
        )
    }
}
