plugins {
    kotlin("jvm")
}

group = "com.alexportfolio.script"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    api("com.microsoft.playwright:playwright:1.55.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}