package com.example.stopwatch.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.example.stopwatch.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "stopwatch.db")
                    .fallbackToDestructiveMigration()
                    .build()
                val running = db.stopwatchDao().allFlow().first().any { it.isRunning }
                if (running) {
                    val serviceIntent = Intent(context.applicationContext, StopwatchService::class.java)
                    context.applicationContext.startForegroundService(serviceIntent)
                }
            }
        }
    }
}
