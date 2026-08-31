package com.lordrimaru.codealphafitnesstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: String, // ISO-8601 format: YYYY-MM-DD
    val steps: Int = 0
)
