plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.azisaba"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io/") }
    maven { url = uri("https://m2.dv8tion.net/releases") }
}

dependencies {
    implementation("dev.kord:kord-core:0.9.0")
    implementation("dev.kord:kord-core-voice:0.9.0")
    implementation("dev.kord:kord-voice:0.9.0")
    implementation("org.slf4j:slf4j-simple:2.0.0")
    implementation("com.charleskorn.kaml:kaml:0.53.0")
    implementation("com.sedmelluq:lavaplayer:1.3.77")
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
