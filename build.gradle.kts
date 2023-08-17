import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.leslie"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val jvmTest by getting

        dependencies {
            val ktor_version: String by project
            commonMainImplementation("io.ktor:ktor-client-core:$ktor_version")
            commonMainImplementation("io.ktor:ktor-client-cio:$ktor_version")
            commonMainImplementation("io.ktor:ktor-client-okhttp:$ktor_version")
            commonMainImplementation("io.ktor:ktor-client-logging:$ktor_version")
            commonMainImplementation("io.ktor:ktor-client-serialization:$ktor_version")
            commonMainImplementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
            commonMainImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

            commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
            commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
            commonMainImplementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
            commonMainImplementation("com.google.code.gson:gson:2.9.0")
            commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
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
        }
    }
}
