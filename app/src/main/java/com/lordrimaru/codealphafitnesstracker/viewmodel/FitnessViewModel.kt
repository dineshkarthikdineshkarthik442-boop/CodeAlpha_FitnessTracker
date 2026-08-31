package com.lordrimaru.codealphafitnesstracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lordrimaru.codealphafitnesstracker.data.local.entity.DailyStatsEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.UserPreferencesEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.WorkoutEntity
import com.lordrimaru.codealphafitnesstracker.data.repository.FitnessRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class FitnessViewModel(private val repository: FitnessRepository) : ViewModel() {

    private val today = LocalDate.now()

    // --- User Preferences & Goals ---
    val userPreferences: StateFlow<UserPreferencesEntity> = repository.getUserPreferences()
        .map { it ?: UserPreferencesEntity() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesEntity())

    // --- Home Screen Data ---
    val todayWorkouts: StateFlow<List<WorkoutEntity>> = repository.getWorkoutsByDate(today.toString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayStats: StateFlow<DailyStatsEntity?> = repository.getDailyStats(today.toString())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalCaloriesBurned: StateFlow<Int> = todayWorkouts.map { workouts ->
        workouts.sumOf { it.caloriesBurned }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalWorkoutDuration: StateFlow<Int> = todayWorkouts.map { workouts ->
        workouts.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val workoutCount: StateFlow<Int> = todayWorkouts.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Progress Screen Data ---
    private val _currentWeekStart = MutableStateFlow(today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart.asStateFlow()

    val weeklyWorkouts: StateFlow<List<WorkoutEntity>> = _currentWeekStart
        .flatMapLatest { start ->
            repository.getWorkoutsBetweenDates(start.toString(), start.plusDays(6).toString())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyStats: StateFlow<List<DailyStatsEntity>> = _currentWeekStart
        .flatMapLatest { start ->
            repository.getDailyStatsBetweenDates(start.toString(), start.plusDays(6).toString())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklySummary = combine(weeklyWorkouts, weeklyStats) { workouts, stats ->
        val totalWorkouts = workouts.size
        val totalCalories = workouts.sumOf { it.caloriesBurned }
        val totalDuration = workouts.sumOf { it.durationMinutes }
        val avgCalories = if (totalWorkouts > 0) totalCalories / totalWorkouts else 0
        val avgDuration = if (totalWorkouts > 0) totalDuration / totalWorkouts else 0
        val totalSteps = stats.sumOf { it.steps }
        
        WeeklySummary(
            totalWorkouts = totalWorkouts,
            totalCalories = totalCalories,
            totalDuration = totalDuration,
            avgCalories = avgCalories,
            avgDuration = avgDuration,
            totalSteps = totalSteps
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklySummary())

    val goalProgress = combine(todayStats, weeklySummary, userPreferences) { todayStats, summary, prefs ->
        GoalProgress(
            stepsProgress = (todayStats?.steps ?: 0).toFloat() / prefs.dailyStepsGoal,
            workoutProgress = summary.totalWorkouts.toFloat() / prefs.weeklyWorkoutGoal,
            caloriesProgress = summary.totalCalories.toFloat() / prefs.weeklyCaloriesGoal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalProgress())

    // --- Achievements ---
    val allWorkouts = repository.getAllWorkouts()
    val allStats = repository.getAllDailyStats()

    val achievements = combine(allWorkouts, allStats, userPreferences) { workouts, stats, prefs ->
        val daysWithWorkouts = workouts.map { it.date }.distinct().size
        val totalCals = workouts.sumOf { it.caloriesBurned }
        val daysWithStepGoal = stats.count { it.steps >= prefs.dailyStepsGoal }

        listOf(
            Achievement("First Workout", "Complete your first workout.", workouts.isNotEmpty()),
            Achievement("Workout Warrior", "Complete 5 workouts.", workouts.size >= 5),
            Achievement("Consistency", "Complete workouts on 3 different days.", daysWithWorkouts >= 3),
            Achievement("Calorie Crusher", "Burn 1,000 calories.", totalCals >= 1000),
            Achievement("Step Starter", "Reach your daily step goal.", daysWithStepGoal >= 1),
            Achievement("Step Champion", "Reach your step goal 7 times.", daysWithStepGoal >= 7)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- History Screen Data ---
    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    val filteredHistory: StateFlow<List<WorkoutEntity>> = combine(
        repository.getAllWorkouts(),
        _historyFilter
    ) { workouts, filter ->
        val now = LocalDate.now()
        when (filter) {
            HistoryFilter.ALL -> workouts
            HistoryFilter.TODAY -> workouts.filter { it.date == now.toString() }
            HistoryFilter.THIS_WEEK -> {
                val start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                workouts.filter { LocalDate.parse(it.date) in start..end }
            }
            HistoryFilter.THIS_MONTH -> {
                val start = now.with(TemporalAdjusters.firstDayOfMonth())
                val end = now.with(TemporalAdjusters.lastDayOfMonth())
                workouts.filter { LocalDate.parse(it.date) in start..end }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---
    fun nextWeek() {
        val next = _currentWeekStart.value.plusWeeks(1)
        val currentWeekStartActual = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        if (!next.isAfter(currentWeekStartActual)) {
            _currentWeekStart.value = next
        }
    }

    fun previousWeek() {
        _currentWeekStart.value = _currentWeekStart.value.minusWeeks(1)
    }

    fun isNextWeekEnabled(): Boolean {
        val next = _currentWeekStart.value.plusWeeks(1)
        val currentWeekStartActual = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return !next.isAfter(currentWeekStartActual)
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun updatePreferences(update: (UserPreferencesEntity) -> UserPreferencesEntity) {
        viewModelScope.launch {
            repository.updateUserPreferences(update(userPreferences.value))
        }
    }

    fun addWorkout(type: String, category: String, duration: Int, calories: Int) {
        viewModelScope.launch {
            repository.insertWorkout(
                WorkoutEntity(
                    exerciseType = type,
                    category = category,
                    durationMinutes = duration,
                    caloriesBurned = calories,
                    date = LocalDate.now().toString()
                )
            )
        }
    }

    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun updateSteps(steps: Int) {
        viewModelScope.launch {
            repository.updateSteps(LocalDate.now().toString(), steps)
        }
    }
}

data class WeeklySummary(
    val totalWorkouts: Int = 0,
    val totalCalories: Int = 0,
    val totalDuration: Int = 0,
    val avgCalories: Int = 0,
    val avgDuration: Int = 0,
    val totalSteps: Int = 0
)

data class GoalProgress(
    val stepsProgress: Float = 0f,
    val workoutProgress: Float = 0f,
    val caloriesProgress: Float = 0f
)

data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

enum class HistoryFilter {
    ALL, TODAY, THIS_WEEK, THIS_MONTH
}
