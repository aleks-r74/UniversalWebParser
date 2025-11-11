package com.alexportfolio.script.host.compiler.wrapper

import kotlin.script.experimental.api.CompiledScript


internal data class CompiledScriptWrapperImpl(val compiledScript: CompiledScript): CompiledScriptWrapper {
    override val getScript: CompiledScript
        get() = this.compiledScript
}
