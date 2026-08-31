package com.lordrimaru.codealphafitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: Int = 0,
    val theme: String = "System", // Light, Dark, System
    val dailyStepsGoal: Int = 10000,
    val weeklyWorkoutGoal: Int = 5,
    val weeklyCaloriesGoal: Int = 2000,
    val dailyReminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
)
