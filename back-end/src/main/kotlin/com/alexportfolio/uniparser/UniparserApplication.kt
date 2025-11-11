package com.alexportfolio.uniparser

import com.alexportfolio.uniparser.configs.BrowserProfiles
import org.komamitsu.spring.data.sqlite.EnableSqliteRepositories
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableSqliteRepositories("com.alexportfolio.uniparser.repositories")
@ComponentScan("com.alexportfolio.uniparser")
@EnableConfigurationProperties(BrowserProfiles::class)
@EnableScheduling
class UniparserApplication

fun main(args: Array<String>) {
	runApplication<UniparserApplication>(*args)
}

