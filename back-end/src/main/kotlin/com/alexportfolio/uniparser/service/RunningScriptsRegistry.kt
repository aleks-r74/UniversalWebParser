package com.alexportfolio.uniparser.service


import com.alexportfolio.uniparser.events.ForceStopEvent
import kotlinx.coroutines.Job
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RunningScriptsRegistry {
    private val runningScripts = ConcurrentHashMap<Int, Job>()

    fun registerActiveJob(scriptId: Int, job: Job) {
        runningScripts[scriptId] = job
    }

    fun unregister(scriptId: Int)  {
        runningScripts.remove(scriptId)
    }
    @EventListener
    fun onForceStop(event: ForceStopEvent)=cancelJob(event.scriptId)

    fun cancelJob(scriptId: Int) {
        runningScripts.remove(scriptId)?.cancel()
    }

    fun isRunning(scriptId: Int) = runningScripts.containsKey(scriptId)
}