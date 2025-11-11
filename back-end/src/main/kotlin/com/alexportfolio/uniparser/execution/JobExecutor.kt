package com.alexportfolio.uniparser.execution

import com.alexportfolio.script.host.executor.ExecutionResult
import com.alexportfolio.uniparser.extensions.sha256
import com.alexportfolio.uniparser.extensions.toStatus
import com.alexportfolio.uniparser.model.ResultWithContent
import com.alexportfolio.uniparser.model.entity.ResultEnt
import com.alexportfolio.uniparser.model.enums.ExecutionStatus
import com.alexportfolio.uniparser.service.ResultService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.coroutines.cancellation.CancellationException

@Component
class JobExecutor(
    private val dbDispatcher: CoroutineDispatcher,
    private val resultService: ResultService,
    private val scriptRunner: ScriptRunner,
    private val objectMapper: ObjectMapper
    ) {
    private val logger = LoggerFactory.getLogger(javaClass)
    suspend fun execute(scriptId: Int) {
        try {
            logger.info("Starting script $scriptId")
            val runResult = scriptRunner.run(scriptId)
            val status = runResult.toStatus()

            val content = when(runResult){
                is ExecutionResult.Success -> objectMapper.writeValueAsString(runResult.result)
                is ExecutionResult.Failure -> runResult.error ?: runResult.logs ?: "No errors or logs"
                is ExecutionResult.Cancelled -> runResult.reason
            }
            logger.info("Script $scriptId, Execution status: $status")
            saveResultEnt(ResultWithContent(ResultEnt(scriptId=scriptId, status = status, resultHash = content.sha256()), content))

        } catch (ce: CancellationException) {
            val content = "The job for script $scriptId was cancelled by $ce}"
            logger.info(content)
            saveResultEnt(
                ResultWithContent(ResultEnt(scriptId=scriptId, status = ExecutionStatus.CANCELLED,resultHash = content.sha256()), content),
                true)
            throw ce
        } catch (e: Exception) {
            logger.info("Execution failed for script $scriptId")
            val failureBody = "Script id=${scriptId} failed to run:\n${e.stackTraceToString()}"
            saveResultEnt(ResultWithContent(
                ResultEnt(scriptId=scriptId, status = ExecutionStatus.FAILED, resultHash = failureBody.sha256()), failureBody)
            )
        }
    }

    private suspend fun saveResultEnt(result: ResultWithContent, nonCancellable: Boolean = false) {
        val ctx = if (nonCancellable) dbDispatcher + NonCancellable else dbDispatcher
        withContext(ctx) {
            resultService.saveResult(result)
        }
    }

}