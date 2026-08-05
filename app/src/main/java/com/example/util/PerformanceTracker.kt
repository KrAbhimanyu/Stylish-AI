package com.example.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PerformanceLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val actionName: String,
    val apiLatencyMs: Long,
    val imageGenLatencyMs: Long = 0L,
    val totalLatencyMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

object PerformanceMonitor {
    private val _logs = MutableStateFlow<List<PerformanceLog>>(emptyList())
    val logs: StateFlow<List<PerformanceLog>> = _logs.asStateFlow()

    fun recordLog(
        actionName: String,
        apiLatencyMs: Long,
        imageGenLatencyMs: Long = 0L,
        totalLatencyMs: Long,
        isSuccess: Boolean = true
    ) {
        val newLog = PerformanceLog(
            actionName = actionName,
            apiLatencyMs = apiLatencyMs,
            imageGenLatencyMs = imageGenLatencyMs,
            totalLatencyMs = totalLatencyMs,
            isSuccess = isSuccess
        )
        _logs.value = (listOf(newLog) + _logs.value).take(15)
    }

    fun getAverageTotalLatencyMs(): Long {
        val list = _logs.value
        if (list.isEmpty()) return 0L
        return list.map { it.totalLatencyMs }.average().toLong()
    }

    fun getAverageApiLatencyMs(): Long {
        val list = _logs.value
        if (list.isEmpty()) return 0L
        return list.map { it.apiLatencyMs }.average().toLong()
    }

    fun getAverageImageGenLatencyMs(): Long {
        val list = _logs.value.filter { it.imageGenLatencyMs > 0 }
        if (list.isEmpty()) return 0L
        return list.map { it.imageGenLatencyMs }.average().toLong()
    }
}
