package com.lordrimaru.codealphafitnesstracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lordrimaru.codealphafitnesstracker.data.local.dao.FitnessDao
import com.lordrimaru.codealphafitnesstracker.data.local.entity.DailyStatsEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.UserPreferencesEntity
import com.lordrimaru.codealphafitnesstracker.data.local.entity.WorkoutEntity

@Database(
    entities = [WorkoutEntity::class, DailyStatsEntity::class, UserPreferencesEntity::class],
    version = 2,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun fitnessDao(): FitnessDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add category column to workouts
                db.execSQL("ALTER TABLE workouts ADD COLUMN category TEXT NOT NULL DEFAULT 'Other'")
                
                // Create user_preferences table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_preferences (
                        id INTEGER NOT NULL PRIMARY KEY,
                        theme TEXT NOT NULL DEFAULT 'System',
                        dailyStepsGoal INTEGER NOT NULL DEFAULT 10000,
                        weeklyWorkoutGoal INTEGER NOT NULL DEFAULT 5,
                        weeklyCaloriesGoal INTEGER NOT NULL DEFAULT 2000,
                        dailyReminderEnabled INTEGER NOT NULL DEFAULT 0,
                        reminderHour INTEGER NOT NULL DEFAULT 8,
                        reminderMinute INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                
                // Insert default preferences
                db.execSQL("INSERT OR IGNORE INTO user_preferences (id, theme, dailyStepsGoal, weeklyWorkoutGoal, weeklyCaloriesGoal, dailyReminderEnabled, reminderHour, reminderMinute) VALUES (0, 'System', 10000, 5, 2000, 0, 8, 0)")
            }
        }

        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
