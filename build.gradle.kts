plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.shadow)
}

group = "net.azisaba"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinxSerizliationJson)
    implementation(libs.bundles.ktor)
    implementation(libs.jda)
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
        archiveFileName.set("YomiageKt.jar")
    }
}
