package com.lordrimaru.codealphafitnesstracker

import android.app.Application
import com.lordrimaru.codealphafitnesstracker.data.local.database.FitnessDatabase
import com.lordrimaru.codealphafitnesstracker.data.repository.FitnessRepository

class FitnessApplication : Application() {
    val database by lazy { FitnessDatabase.getDatabase(this) }
    val repository by lazy { FitnessRepository(database.fitnessDao()) }
}
