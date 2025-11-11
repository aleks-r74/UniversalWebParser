package com.alexportfolio.uniparser.execution.scheduler

import com.alexportfolio.uniparser.model.entity.ScriptEnt
import com.alexportfolio.uniparser.service.ScriptService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ScriptExecutionSchedulerImpl(
    val scriptService: ScriptService,
    val dbDispatcher: CoroutineDispatcher
): ScriptExecutionScheduler {
    private val _queue = Channel<Int>(Channel.RENDEZVOUS)
    override val queue
        get() = _queue as ReceiveChannel<Int>

    @Value("\${scripts.scheduler.failthreshold}")
    val failThreshold = 3

    override suspend fun maintainQueue() {
        if(_queue.isClosedForSend) return

        val scripts = withContext(dbDispatcher) {
            scriptService.findAll()
                .filter { s->
                    s.failCount < failThreshold
                            && s.enabled == 1
                            && s.enqueued == 0
                            && isTimeToRun(s.nextRun)
                }
                .map(ScriptEnt::id)
                .sorted()
        }
        if (scripts.isEmpty()) return

        for (id in scripts) {
            if (_queue.isClosedForSend) return
            withContext(dbDispatcher) { scriptService.markEnqueued(id) }
            _queue.send(id)
        }
    }
    override fun stop() {
        _queue.close()
    }
    private fun isTimeToRun(nextRun: String): Boolean{
        if(nextRun.isNullOrBlank()) return true
        return Instant.parse(nextRun).isBefore(Instant.now())
    }
}