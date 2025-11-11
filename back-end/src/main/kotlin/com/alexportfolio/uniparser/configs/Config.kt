package com.alexportfolio.uniparser.configs

import com.alexportfolio.script.host.compiler.ScriptCompiler
import com.alexportfolio.script.host.executor.ScriptExecutor
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration
class Config {
    @Bean(destroyMethod = "shutdown")
    fun dbExecutor(): ExecutorService =
        Executors.newSingleThreadExecutor { r -> Thread(r, "db-thread") }

    @Bean
    fun dbDispatcher(dbExecutor: ExecutorService): CoroutineDispatcher {
        return dbExecutor.asCoroutineDispatcher()
    }

    @Bean
    fun scriptCompiler() = ScriptCompiler()

    @Bean
    fun scriptExecutor() = ScriptExecutor()

    @Bean
    fun playwright(): Playwright = Playwright.create()

}