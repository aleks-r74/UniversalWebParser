package com.alexportfolio.uniparser.service

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.Closeable

@Service
class BrowserManager(
    private val playwright: Playwright,
    @Value("\${playwright.endpoint}") private val endpoint: String
) : Closeable {
    private val logger = LoggerFactory.getLogger(javaClass)
    @Volatile
    private var browser: Browser = connect()

    private fun connect(): Browser {
        return playwright.chromium().connect(endpoint)
    }

    fun getBrowser(): Browser {
        if (!browser.isConnected()) {
            synchronized(this) {
                if (!browser.isConnected()) {
                    logger.info("Browser died, trying to reconnect")
                    browser.closeSafe()
                    browser = connect()
                }
            }
        }
        return browser
    }

    override fun close() {
        browser.closeSafe()
    }

    private fun Browser.closeSafe() {
        try { this.close() } catch (_: Exception) {}
    }
}
