package com.alexportfolio.script.host.compiler

import com.alexportfolio.script.host.compiler.wrapper.CompiledScriptWrapper

sealed class CompilationResult {
    data class Success(val scriptWrapper: CompiledScriptWrapper, val logs: String?) : CompilationResult()
    data class Failure(val logs: String) : CompilationResult()
}
