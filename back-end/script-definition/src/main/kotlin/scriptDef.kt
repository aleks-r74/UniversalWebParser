package com.alexportfolio.script.definition

import com.microsoft.playwright.Page
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(
    fileExtension = "kts",
    compilationConfiguration = CompilationConfiguration::class
)
abstract class WebParser(val store: ScriptStore, val page: Page, val println: (Any)->Unit)

object CompilationConfiguration : ScriptCompilationConfiguration(
    {
        jvm { dependenciesFromCurrentContext(wholeClasspath = true) }
    }
)