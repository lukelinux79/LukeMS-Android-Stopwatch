package com.example.stopwatch.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StopwatchEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stopwatchDao(): StopwatchDao
}
