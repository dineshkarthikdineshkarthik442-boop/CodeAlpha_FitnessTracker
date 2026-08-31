package com.lordrimaru.codealphafitnesstracker.data.repository

import com.lordrimaru.codealphafitnesstracker.data.local.dao.FitnessDao
import com.lordrimaru.codealphafitnesstracker.data.local.entity.DailyStatsEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.UserPreferencesEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

class FitnessRepository(private val fitnessDao: FitnessDao) {

    fun getAllWorkouts(): Flow<List<WorkoutEntity>> = fitnessDao.getAllWorkouts()

    fun getWorkoutsByDate(date: String): Flow<List<WorkoutEntity>> {
        return fitnessDao.getWorkoutsByDate(date)
    }

    fun getWorkoutsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutEntity>> {
        return fitnessDao.getWorkoutsBetweenDates(startDate, endDate)
    }

    suspend fun insertWorkout(workout: WorkoutEntity) {
        fitnessDao.insertWorkout(workout)
    }

    suspend fun updateWorkout(workout: WorkoutEntity) {
        fitnessDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: WorkoutEntity) {
        fitnessDao.deleteWorkout(workout)
    }

    fun getDailyStats(date: String): Flow<DailyStatsEntity?> {
        return fitnessDao.getDailyStatsByDate(date)
    }

    fun getDailyStatsBetweenDates(startDate: String, endDate: String): Flow<List<DailyStatsEntity>> {
        return fitnessDao.getDailyStatsBetweenDates(startDate, endDate)
    }

    fun getAllDailyStats(): Flow<List<DailyStatsEntity>> = fitnessDao.getAllDailyStats()

    suspend fun updateSteps(date: String, steps: Int) {
        fitnessDao.insertDailyStats(DailyStatsEntity(date, steps))
    }

    // User Preferences
    fun getUserPreferences(): Flow<UserPreferencesEntity?> = fitnessDao.getUserPreferences()

    suspend fun updateUserPreferences(prefs: UserPreferencesEntity) {
        fitnessDao.insertUserPreferences(prefs)
    }
}
