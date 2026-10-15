package com.example.stopwatch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StopwatchDao {
    @Query("SELECT * FROM stopwatches ORDER BY name")
    fun allFlow(): Flow<List<StopwatchEntity>>

    @Query("SELECT * FROM stopwatches WHERE id = :id")
    suspend fun getById(id: String): StopwatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stopwatch: StopwatchEntity)

    @Update
    suspend fun update(stopwatch: StopwatchEntity)

    @Delete
    suspend fun delete(stopwatch: StopwatchEntity)
}
