package com.example.stopwatch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stopwatch.data.StopwatchEntity
import com.example.stopwatch.data.StopwatchRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StopwatchUiModel(
    val entity: StopwatchEntity,
    val elapsedSeconds: Long
)

class StopwatchViewModel(private val repo: StopwatchRepository) : ViewModel() {

    private val _now = MutableStateFlow(System.currentTimeMillis())
    private val tickerJob: Job = viewModelScope.launch {
        while (true) {
            _now.value = System.currentTimeMillis()
            delay(1000L)
        }
    }

    init {
        viewModelScope.launch {
            val existing = repo.allFlow().first()
            if (existing.isEmpty()) {
                repo.create("Stoppuhr 1")
            }
        }
    }

    val currentTime: StateFlow<String> = _now.map { ms ->
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(ms))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    )

    val stopwatches: StateFlow<List<StopwatchUiModel>> = repo.allFlow().combine(_now) { list, now ->
        list.map { sw ->
            val elapsedMs = repo.getElapsedMillis(sw, now)
            StopwatchUiModel(sw, (elapsedMs / 1000L).coerceAtLeast(0L))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun create() = viewModelScope.launch {
        val currentList = repo.allFlow().first()
        val nextIndex = currentList.size + 1
        repo.create("Stoppuhr $nextIndex")
    }

    fun start(id: String) = viewModelScope.launch { repo.start(id) }
    fun pause(id: String) = viewModelScope.launch { repo.pause(id) }
    fun reset(id: String) = viewModelScope.launch { repo.reset(id) }
    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
    fun addPastTime(id: String, seconds: Long) = viewModelScope.launch { repo.addPastTime(id, seconds * 1000L) }
    fun setStartTime(id: String, startWallMillis: Long) = viewModelScope.launch { repo.setStartTime(id, startWallMillis) }

    override fun onCleared() {
        super.onCleared()
        tickerJob.cancel()
    }
}
