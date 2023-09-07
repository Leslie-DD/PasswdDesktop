import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.7.20"
    id("org.jetbrains.compose")
}

//group = "com.leslie"
//version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            dependencies {
                implementation(compose.desktop.currentOs)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.material)
                api(compose.materialIconsExtended)
            }
        }
        val jvmTest by getting

        dependencies {
            commonMainImplementation(libs.ktor.client.core)
            commonMainImplementation(libs.ktor.client.cio)
            commonMainImplementation(libs.ktor.client.okhttp)
            commonMainImplementation(libs.ktor.client.logging)
            commonMainImplementation(libs.ktor.client.serialization)
            commonMainImplementation(libs.ktor.client.content.negotiation)
            commonMainImplementation(libs.ktor.serialization.kotlinx.json)

            commonMainImplementation(libs.kotlinx.coroutines.core)
            commonMainImplementation(libs.kotlinx.coroutines.core.jvm)
            commonMainImplementation(libs.kotlinx.coroutines.swing)
            commonMainImplementation(libs.kotlin.reflect)
            commonMainImplementation(libs.gson)
            commonMainImplementation(libs.kotlinx.serialization.json)
            commonMainImplementation(libs.kotlinx.serialization.core)

            commonMainImplementation(libs.ktpref)

            commonMainImplementation(libs.logback)
            commonMainImplementation(libs.log4j12)
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PasswdDesktop"
            packageVersion = "1.0.0"

            buildTypes.release {
                proguard {
                    configurationFiles.from("compose-desktop.pro")
                }
            }

            macOS {
                iconFile.set(project.file("icons/app_icon_round_corner.icns"))
            }
            windows {
                iconFile.set(project.file("icons/app_icon_round_corner.ico"))
            }
            linux {
                iconFile.set(project.file("icons/app_icon_round_corner.png"))
            }
        }
    }
}
