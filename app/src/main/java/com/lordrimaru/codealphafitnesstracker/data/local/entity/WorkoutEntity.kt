package com.lordrimaru.codealphafitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val exerciseType: String,
    val category: String = "Other",
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val date: String // ISO-8601 format: YYYY-MM-DD
)
