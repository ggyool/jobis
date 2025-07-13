plugins {
    kotlin("jvm") version "1.9.10"
    application
}

group = "com.jobis"
version = "1.0.0"

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
    
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.jobis.MainKt")
}