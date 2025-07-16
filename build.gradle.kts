plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.jobis"
version = "1.0.0"

application {
    mainClass.set("com.jobis.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
    
    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.43.2.2")
    
    // MCP
    implementation("io.modelcontextprotocol:kotlin-sdk:0.5.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("ch.qos.logback:logback-classic:1.4.11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.jobis.MainKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}