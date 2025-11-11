package com.alexportfolio.script.host.executor

import com.alexportfolio.script.host.compiler.wrapper.CompiledScriptWrapper
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class ScriptExecutor {
    private val scriptingHost = BasicJvmScriptingHost()
    private val executorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    )

    suspend fun run(scripWrapper: CompiledScriptWrapper, vararg args: Any): ExecutionResult {
        return try {
            val config = ScriptEvaluationConfiguration {
                constructorArgs(*args)
            }
            val result = evaluateScript (scripWrapper,config)

            val logs = result.reports
                .filter { it.severity >= ScriptDiagnostic.Severity.DEBUG }
                .joinToString("\n") { it.toString() }

            val evalValue = result.valueOrNull()

            when (val rv = evalValue?.returnValue) {
                is ResultValue.Value -> ExecutionResult.Success(rv.value, logs)
                is ResultValue.Unit -> ExecutionResult.Success(null, logs)
                is ResultValue.Error -> ExecutionResult.Failure(rv.error.stackTraceToString(), logs)
                else -> ExecutionResult.Failure("Script finished with result value: ${rv.toString()}", logs)
            }
        }
        catch(ce: CancellationException){ throw ce }
        catch (e: Exception) {
            ExecutionResult.Failure(e.stackTraceToString(), "Execution failed due to host problems")
        }
    }

    private suspend fun evaluateScript(wrapper: CompiledScriptWrapper, conf: ScriptEvaluationConfiguration): ResultWithDiagnostics<EvaluationResult> =
        suspendCancellableCoroutine { cont ->
            val future = executorService.submit<ResultWithDiagnostics<EvaluationResult>> {
                runBlocking {
                    scriptingHost.evaluator.invoke(wrapper.getScript, conf)
                }
            }
            cont.invokeOnCancellation { future.cancel(true) }
            try {
                val res = future.get()
                cont.resume(res)
            } catch (ee: ExecutionException) {
                val cause = ee.cause ?: ee
                throw cause
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                if (cont.isActive) cont.resumeWithException(t)
                else throw t
            }

        }

}