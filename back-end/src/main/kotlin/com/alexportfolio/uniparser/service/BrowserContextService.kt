package com.alexportfolio.uniparser.service

import com.alexportfolio.uniparser.configs.BrowserProfiles
import com.alexportfolio.uniparser.configs.Profile
import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserContext.StorageStateOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.ColorScheme
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.exists


@Service
class BrowserContextService(private val browserManager: BrowserManager,
                            private val profiles: BrowserProfiles,
                            private val objectMapper: ObjectMapper
) {
    val stateFile = "state.json"
    val profileFile = "profile.json"

    suspend fun <T>usePage(scriptId: Int, block: suspend (p: Page)->T): T{
        val scriptPath = Path.of("scripts",scriptId.toString())
        val context = loadProfileOrRandom(scriptPath)
        return try{
            context.newPage().use { page -> block(page) }
        } finally {
            context.storageState(StorageStateOptions().setPath(scriptPath.resolve(stateFile)))
            context.close()
        }
    }

    private fun loadProfileOrRandom(scriptPath: Path): BrowserContext{
       val pFile = scriptPath.resolve(profileFile).toFile()
       val profile = if(pFile.exists())
            objectMapper.readValue(pFile, Profile::class.java)
        else {
           val random = profiles.getRandomProfile()
           objectMapper.writeValue(pFile,random)
           random
       }

       return browserManager.getBrowser().newContext(
            Browser.NewContextOptions()
                .setUserAgent(profile.userAgent)
                .setLocale(profile.locale)
                .setTimezoneId(profile.timezone)
                .setViewportSize(profile.viewportWidth, profile.viewportHeight)
                .setDeviceScaleFactor(profile.deviceScaleFactor)
                .setColorScheme(ColorScheme.valueOf(profile.colorScheme.uppercase()))
                .also {
                    val sPath = scriptPath.resolve(stateFile)
                    if(sPath.exists()) it.setStorageStatePath(sPath)
                }
        )
    }
}

