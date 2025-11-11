package com.alexportfolio.script.host.compiler

import com.alexportfolio.script.definition.WebParser
import com.alexportfolio.script.host.compiler.wrapper.CompiledScriptWrapperImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class ScriptCompiler {
    private val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<WebParser>()
    private val scriptingHost = BasicJvmScriptingHost()

    suspend fun compileFromSource(source: String): CompilationResult {
        val compiledResult = try {
            withContext(Dispatchers.Default) {
                scriptingHost.compiler.invoke(source.toScriptSource(), compilationConfiguration)
            }
        } catch (t: Exception) {
            val err = t.stackTraceToString()
            return CompilationResult.Failure("Unexpected compiler error: $err")
        }

        val logs = compiledResult.reports
            .filter { it.severity >= ScriptDiagnostic.Severity.INFO }
            .joinToString("\n") { it.toString() }

        val compiledScript = compiledResult.valueOrNull()

        return if (compiledScript != null) {
            CompilationResult.Success(CompiledScriptWrapperImpl(compiledScript), logs.takeIf { it.isNotBlank() })
        }else
            CompilationResult.Failure(logs.ifBlank { "Compilation failed with no diagnostics" })
    }

}