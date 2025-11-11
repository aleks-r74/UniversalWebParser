package com.alexportfolio.script.host.compiler.wrapper

import kotlin.script.experimental.api.CompiledScript
interface CompiledScriptWrapper {
    val getScript: CompiledScript
}