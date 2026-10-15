package com.example.stopwatch.data

class StopwatchRepository(private val dao: StopwatchDao) {

    fun allFlow() = dao.allFlow()

    fun getElapsedMillis(sw: StopwatchEntity, nowWall: Long = System.currentTimeMillis()): Long {
        val base = sw.accumulatedMillis
        return if (!sw.isRunning || sw.startTimeMillis == null) {
            base
        } else {
            base + (nowWall - sw.startTimeMillis)
        }
    }

    suspend fun create(name: String = "Stoppuhr") {
        dao.insert(StopwatchEntity(name = name))
    }

    suspend fun start(id: String) {
        val entity = dao.getById(id) ?: return
        if (!entity.isRunning) {
            val now = System.currentTimeMillis()
            val updated = entity.copy(
                isRunning = true,
                startTimeMillis = now
            )
            dao.update(updated)
        }
    }

    suspend fun pause(id: String) {
        val entity = dao.getById(id) ?: return
        if (entity.isRunning && entity.startTimeMillis != null) {
            val now = System.currentTimeMillis()
            val elapsed = (now - entity.startTimeMillis).coerceAtLeast(0L)
            val updated = entity.copy(
                isRunning = false,
                startTimeMillis = null,
                accumulatedMillis = entity.accumulatedMillis + elapsed
            )
            dao.update(updated)
        }
    }

    suspend fun reset(id: String) {
        val entity = dao.getById(id) ?: return
        val updated = entity.copy(
            isRunning = false,
            startTimeMillis = null,
            accumulatedMillis = 0L
        )
        dao.update(updated)
    }

    suspend fun delete(id: String) {
        val entity = dao.getById(id) ?: return
        dao.delete(entity)
    }

    suspend fun addPastTime(id: String, addedMillis: Long) {
        val entity = dao.getById(id) ?: return
        if (entity.isRunning && entity.startTimeMillis != null) {
            val updated = entity.copy(startTimeMillis = entity.startTimeMillis - addedMillis)
            dao.update(updated)
        } else {
            val updated = entity.copy(accumulatedMillis = entity.accumulatedMillis + addedMillis)
            dao.update(updated)
        }
    }

    suspend fun setStartTime(id: String, startWallMillis: Long) {
        val entity = dao.getById(id) ?: return
        val updated = entity.copy(
            isRunning = true,
            startTimeMillis = startWallMillis,
            accumulatedMillis = 0L
        )
        dao.update(updated)
    }
}
