package com.alexportfolio.uniparser.configs

import org.springframework.boot.context.properties.ConfigurationProperties



@ConfigurationProperties(prefix = "browser-profiles")
data class BrowserProfiles(
    val timeZones: List<String> = emptyList(),
    val userAgents: List<String> = emptyList(),
    val viewPorts: List<List<Int>> = emptyList(),
    val deviceScaleFactors: List<Double> = emptyList(),
    val colorScheme: List<String> = emptyList()
) {
    fun getRandomProfile(): Profile {
        require(userAgents.isNotEmpty()) { "userAgents must not be empty" }
        val vp = if (viewPorts.isNotEmpty()) {
            viewPorts.random()
        } else listOf(1280, 720)

        val dpf = deviceScaleFactors.randomOrNull() ?: 1.0
        val cs = colorScheme.randomOrNull() ?: "LIGHT"
        val tz = timeZones.randomOrNull() ?: "America/Chicago"

        return Profile(
            userAgent = userAgents.random(),
            viewportWidth = vp[0],
            viewportHeight = vp[1],
            deviceScaleFactor = dpf,
            locale = "en-US",
            timezone = tz,
            colorScheme = cs,
            proxy = null
        )
    }
}

data class Profile(
    val userAgent: String,
    val viewportWidth: Int,
    val viewportHeight: Int,
    val deviceScaleFactor: Double,
    val locale: String,
    val timezone: String,
    val colorScheme: String,
    val proxy: String?
)