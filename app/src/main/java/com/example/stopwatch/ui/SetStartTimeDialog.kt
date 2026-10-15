package com.example.stopwatch.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SetStartTimeDialog(
    visible: Boolean,
    initialMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (millis: Long) -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val initialTime = initialMillis ?: System.currentTimeMillis()
    val calendar = remember(initialMillis) {
        Calendar.getInstance().apply {
            timeInMillis = initialTime
        }
    }

    var year by remember(initialMillis) { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember(initialMillis) { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember(initialMillis) { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember(initialMillis) { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember(initialMillis) { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    val previewMillis = remember(year, month, day, hour, minute) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val previewText = remember(previewMillis) {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        formatter.format(Date(previewMillis))
    }

    fun showDatePicker(context: Context) {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                year = selectedYear
                month = selectedMonth
                day = selectedDay
            },
            year,
            month,
            day
        ).show()
    }

    fun showTimePicker(context: Context) {
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
            },
            hour,
            minute,
            true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Startzeit in der Vergangenheit setzen") },
        text = {
            Column {
                Text(
                    text = "Ausgewählte Startzeit:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker(context) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Datum")
                    }
                    OutlinedButton(
                        onClick = { showTimePicker(context) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Uhrzeit")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(previewMillis)
            }) {
                Text("Bestätigen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
