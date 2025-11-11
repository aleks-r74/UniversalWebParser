package com.alexportfolio.uniparser.execution.worker

import com.alexportfolio.uniparser.service.RunningScriptsRegistry
import com.alexportfolio.uniparser.service.ScriptService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel

class ScriptExecutionWorkerImpl(
    private val queue: ReceiveChannel<Int>,
    private val dbDispatcher: CoroutineDispatcher,
    private val scriptRegistry: RunningScriptsRegistry,
    private val scriptService: ScriptService
) : ScriptExecutionWorker {

    override suspend fun runNext(block: suspend (scriptId:Int)->Unit) {
        val take = queue.receiveCatching()
        val scriptId = take.getOrNull() ?: return
        coroutineScope {
            val job = launch {
                    try{
                        block(scriptId)
                    } finally {
                        withContext(dbDispatcher + NonCancellable ) { scriptService.resetEnqueued(scriptId) }
                    }
                }
            scriptRegistry.registerActiveJob(scriptId, job)
            job.invokeOnCompletion { cause-> scriptRegistry.unregister(scriptId) }
        }
    }
}
