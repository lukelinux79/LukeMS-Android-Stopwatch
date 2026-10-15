package com.example.stopwatch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stopwatches")
data class StopwatchEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Stopwatch",
    val color: Int = 0xFF6200EE.toInt(),
    val isRunning: Boolean = false,
    val startTimeMillis: Long? = null,
    val startElapsedRealtime: Long? = null,
    val accumulatedMillis: Long = 0L
)
