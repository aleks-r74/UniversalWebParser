package com.alexportfolio.uniparser.service

import com.alexportfolio.uniparser.execution.JobExecutor
import com.alexportfolio.uniparser.execution.scheduler.ScriptExecutionScheduler
import com.alexportfolio.uniparser.execution.worker.ScriptExecutionWorker
import com.alexportfolio.uniparser.execution.worker.ScriptExecutionWorkerImpl
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class StartupService(
    private val scheduler: ScriptExecutionScheduler,
    private val dbDispatcher: CoroutineDispatcher,
    private val jobExecutor: JobExecutor,
    private val scriptRegistry: RunningScriptsRegistry,
    private val scriptService: ScriptService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        logger.error("Uncaught coroutine exception", exception)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)

    @Value("\${scripts.workers}")
    private val workers: Int = 1

    fun CoroutineScope.startScriptScheduler() {
        launch {
            while (isActive) {
                try {
                    scheduler.maintainQueue()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    logger.error("Scheduler iteration failed — will retry after backoff", e)
                }
                delay(1_000)
            }
        }
    }

    fun CoroutineScope.startWorkerRunner(workersList: List<ScriptExecutionWorker>) {
        workersList.forEach { worker ->
            launch {
                while (isActive) {
                    try {
                        worker.runNext(jobExecutor::execute)
                        yield()
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (e: Exception) {
                        logger.error("Unhandled exception in worker iteration - swallowing and continuing", e)
                    }
                    delay(1_000)
                }
            }
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun run() {
        scriptService.failEnqueued()
        scope.startScriptScheduler()

        val workersList = List(workers) {
            ScriptExecutionWorkerImpl(scheduler.queue, dbDispatcher, scriptRegistry, scriptService)
        }
        scope.startWorkerRunner(workersList)
    }

    @PreDestroy
    fun shutdown() {
        scheduler.stop()
        scope.cancel()
        runBlocking { scope.coroutineContext[Job]?.join() }
    }
}
