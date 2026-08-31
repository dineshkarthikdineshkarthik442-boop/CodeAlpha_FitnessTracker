package com.lordrimaru.codealphafitnesstracker.model

import java.time.LocalDate
import java.util.UUID

data class Workout(
    val id: UUID = UUID.randomUUID(),
    val exerciseType: String,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val date: LocalDate = LocalDate.now()
)
