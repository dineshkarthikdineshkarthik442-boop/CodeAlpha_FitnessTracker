package com.lordrimaru.codealphafitnesstracker.data.local.dao

import androidx.room.*
import com.lordrimaru.codealphafitnesstracker.data.local.entity.DailyStatsEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.UserPreferencesEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessDao {
    // Workouts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY date DESC, id DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY id DESC")
    fun getWorkoutsByDate(date: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWorkoutsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutEntity>>

    // Daily Stats (Steps)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStats(stats: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getDailyStatsByDate(date: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getDailyStatsBetweenDates(startDate: String, endDate: String): Flow<List<DailyStatsEntity>>

    @Query("SELECT * FROM daily_stats")
    fun getAllDailyStats(): Flow<List<DailyStatsEntity>>

    // User Preferences
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(prefs: UserPreferencesEntity)

    @Query("SELECT * FROM user_preferences WHERE id = 0")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>
}
