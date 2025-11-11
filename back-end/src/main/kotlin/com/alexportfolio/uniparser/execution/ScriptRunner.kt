package com.alexportfolio.uniparser.execution

import com.alexportfolio.script.host.compiler.CompilationResult
import com.alexportfolio.script.host.compiler.ScriptCompiler
import com.alexportfolio.script.host.compiler.wrapper.CompiledScriptWrapper
import com.alexportfolio.script.host.executor.ExecutionResult
import com.alexportfolio.script.host.executor.ScriptExecutor
import com.alexportfolio.uniparser.events.ForceStopEvent
import com.alexportfolio.uniparser.events.ScriptSavedEvent
import com.alexportfolio.uniparser.service.BrowserContextService
import com.alexportfolio.uniparser.service.FileService
import com.alexportfolio.uniparser.service.ScriptService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.ConcurrentHashMap


@Service
class ScriptRunner(private val scriptCompiler: ScriptCompiler,
                   private val scriptExecutor: ScriptExecutor,
                   private val scriptService: ScriptService,
                   private val dbDispatcher: CoroutineDispatcher,
                   private val fs: FileService,
                   private val browserCtxPool: BrowserContextService
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val scripts = ConcurrentHashMap<Int, CompiledScriptWrapper>()
    private val locks = ConcurrentHashMap<Int, Mutex>()

    suspend fun run(scriptId:Int): ExecutionResult {
        val lock = locks.computeIfAbsent(scriptId){ Mutex() }
        lock.withLock {
            if (!scripts.containsKey(scriptId)) {
                val source = withContext(Dispatchers.IO) { fs.readScript(scriptId) }
                if(compileNew(scriptId, source) is CompilationResult.Failure)
                    return ExecutionResult.Cancelled("Unable to run due to compilation failure")
            }

            return browserCtxPool.usePage(scriptId) { page->
                    scriptExecutor.run(scripts[scriptId]!!,
                        fs.getScriptStore(scriptId),
                        page,
                        {input: Any->logger.info("S$scriptId> $input")}
                    )
            }
        }
    }

    suspend fun compileNew(scriptId: Int, source: String): CompilationResult{
        logger.info("Compiling script $scriptId")
        val result = withContext(Dispatchers.Default){ compile(source) }
        return when(result){
            is CompilationResult.Success ->{
                logger.info("Successfully compiled script $scriptId")
                withContext(dbDispatcher) {
                    scriptService.setCompilationSuccess(scriptId, true)
                }
                scripts[scriptId] = result.scriptWrapper
                result
            }
            is CompilationResult.Failure -> {
                logger.info("Compilation failed for script $scriptId. See logs for details.")
                withContext(dbDispatcher){ scriptService.setCompilationSuccess(scriptId,false) }
                withContext(Dispatchers.IO) { fs.writeCompilationLogs(scriptId,"Compilation of script id $scriptId failed: \n ${result.logs}") }
                result
                }
            else -> CompilationResult.Failure("Compilation wasn't successful: $result")
        }
    }

    private suspend fun compile(source: String) = scriptCompiler.compileFromSource(source)

    private fun clearCache(scriptId: Int){
        scripts.remove(scriptId)
        locks.remove(scriptId)
    }

    @EventListener
    fun onForceStop(event: ForceStopEvent)=clearCache(event.scriptId)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onScriptSaved(event: ScriptSavedEvent) = clearCache(event.id)

}
