package com.example.stopwatch.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.stopwatch.data.AppDatabase
import com.example.stopwatch.data.StopwatchRepository
import com.example.stopwatch.service.StopwatchService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val db = remember {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "stopwatch.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            val repo = remember { StopwatchRepository(db.stopwatchDao()) }
            val viewModel: StopwatchViewModel = viewModel(
                factory = StopwatchViewModelFactory(repo)
            )
            val items by viewModel.stopwatches.collectAsState()
            val currentTime by viewModel.currentTime.collectAsState()

            val runningCount = items.count { it.entity.isRunning }
            LaunchedEffect(runningCount) {
                val intent = Intent(context, StopwatchService::class.java)
                if (runningCount > 0) {
                    context.startForegroundService(intent)
                } else {
                    context.stopService(intent)
                }
            }

            StopwatchScreen(
                items = items,
                currentTime = currentTime,
                onCreate = { viewModel.create() },
                onStart = { id -> viewModel.start(id) },
                onPause = { id -> viewModel.pause(id) },
                onReset = { id -> viewModel.reset(id) },
                onDelete = { id -> viewModel.delete(id) },
                onAddPastTime = { id, seconds -> viewModel.addPastTime(id, seconds) },
                onSetStart = { id, millis -> viewModel.setStartTime(id, millis) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StopwatchScreen(
    items: List<StopwatchUiModel>,
    currentTime: String,
    onCreate: () -> Unit,
    onStart: (String) -> Unit,
    onPause: (String) -> Unit,
    onReset: (String) -> Unit,
    onDelete: (String) -> Unit,
    onAddPastTime: (String, Long) -> Unit,
    onSetStart: (String, Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Multi-Stoppuhr") })
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aktuelle Uhrzeit: $currentTime",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Neue Stoppuhr hinzufügen")
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.entity.id }) { item ->
                    StopwatchCard(
                        model = item,
                        onStart = { onStart(item.entity.id) },
                        onPause = { onPause(item.entity.id) },
                        onReset = { onReset(item.entity.id) },
                        onDelete = { onDelete(item.entity.id) },
                        onAddPastTime = { onAddPastTime(item.entity.id, 60L) },
                        onSetStart = { millis -> onSetStart(item.entity.id, millis) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StopwatchCard(
    model: StopwatchUiModel,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit,
    onAddPastTime: () -> Unit,
    onSetStart: (Long) -> Unit
) {
    var showDialog by remember(model.entity.id) { mutableStateOf(false) }
    val initialMillis = remember(model.entity) {
        if (model.entity.isRunning && model.entity.startTimeMillis != null) {
            model.entity.startTimeMillis
        } else {
            System.currentTimeMillis() - model.entity.accumulatedMillis
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.entity.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDuration(model.elapsedSeconds),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Text("Löschen", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val buttonModifier = Modifier.weight(1f)
                val buttonPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp)
                val textStyle = MaterialTheme.typography.labelMedium

                if (model.entity.isRunning) {
                    Button(
                        onClick = onPause,
                        modifier = buttonModifier,
                        contentPadding = buttonPadding
                    ) {
                        Text("Pause", maxLines = 1, style = textStyle)
                    }
                } else {
                    Button(
                        onClick = onStart,
                        modifier = buttonModifier,
                        contentPadding = buttonPadding
                    ) {
                        Text("Start", maxLines = 1, style = textStyle)
                    }
                }

                OutlinedButton(
                    onClick = onReset,
                    modifier = buttonModifier,
                    contentPadding = buttonPadding
                ) {
                    Text("Reset", maxLines = 1, style = textStyle)
                }

                OutlinedButton(
                    onClick = onAddPastTime,
                    modifier = buttonModifier,
                    contentPadding = buttonPadding
                ) {
                    Text("+60s", maxLines = 1, style = textStyle)
                }

                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = buttonModifier,
                    contentPadding = buttonPadding
                ) {
                    Text("Startzeit", maxLines = 1, style = textStyle)
                }
            }
        }
    }

    SetStartTimeDialog(
        visible = showDialog,
        initialMillis = initialMillis,
        onDismiss = { showDialog = false },
        onConfirm = { millis ->
            showDialog = false
            onSetStart(millis)
        }
    )
}

private fun formatDuration(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (days > 0) {
        String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
    } else {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

class StopwatchViewModelFactory(private val repo: StopwatchRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StopwatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StopwatchViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
