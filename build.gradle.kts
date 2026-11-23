import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.spencerwhitehead7"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("io.coil-kt.coil3:coil-compose:3.6.3")
    implementation("org.apache.commons:commons-lang3:3.22.0")

    testImplementation("io.mockk:mockk:1.14.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.4.21")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "photo-organizer"
            packageVersion = "1.0.0"
        }
    }
}
