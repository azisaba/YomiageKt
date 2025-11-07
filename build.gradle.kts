plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.shadow)
}

group = "net.azisaba"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.kord)
    implementation(libs.slf4jSimple)
    implementation(libs.kaml)
    implementation(libs.lavaplayer)
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        manifest {
            attributes("Main-Class" to "net.azisaba.yomiagekt.MainKt")
        }
    }
}
