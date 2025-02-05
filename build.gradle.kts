plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.azisaba"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.0.3")
    implementation("io.ktor:ktor-client-cio:3.0.3")
    implementation("dev.kord:kord-core:0.15.0")
    implementation("dev.kord:kord-core-voice:0.15.0")
    implementation("dev.kord:kord-voice:0.15.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.charleskorn.kaml:kaml:0.70.0")
    implementation("dev.arbjerg:lavaplayer:2.2.3")
}

kotlin {
    jvmToolchain(11)
}

tasks {
    shadowJar {
        manifest {
            attributes("Main-Class" to "net.azisaba.yomiagekt.MainKt")
        }
    }
}
