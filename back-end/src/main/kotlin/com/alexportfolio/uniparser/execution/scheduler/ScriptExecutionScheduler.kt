package com.alexportfolio.uniparser.execution.scheduler

import kotlinx.coroutines.channels.ReceiveChannel

interface ScriptExecutionScheduler {
    val queue: ReceiveChannel<Int>
    suspend fun maintainQueue()
    fun stop()
}