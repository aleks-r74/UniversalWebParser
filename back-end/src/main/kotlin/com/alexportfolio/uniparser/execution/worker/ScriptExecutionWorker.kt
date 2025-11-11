package com.alexportfolio.uniparser.execution.worker

interface ScriptExecutionWorker {
    suspend fun runNext(block: suspend (scriptId:Int)->Unit): Unit
}