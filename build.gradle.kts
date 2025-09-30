import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.sqldelight)
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://packages.jetbrains.team/maven/p/kpm/public/") // fo
}

sqldelight {
    databases {
        create("ApplicationDatabase") {
            packageName.set("com.passwd.common.database")
//            schemaOutputDirectory.set(file("com.passwd.common.database"))
//            migrationOutputDirectory.set(file("com.passwd.common.database"))
//            deriveSchemaFromMigrations.set(true)
//            verifyMigrations.set(true)
        }
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Or your desired Java version
    }

    sourceSets {
        jvm().compilations["main"].defaultSourceSet {
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            dependencies {
                implementation(compose.desktop.currentOs)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.material)
                api(compose.materialIconsExtended)

                implementation(libs.sqldelight)
                implementation(libs.kotlinx.datetime)
                implementation(libs.jSystemThemeDetector)
                implementation(libs.drag.and.drop)
                implementation(libs.jewel.init.ui.standalone)
                implementation(libs.jewel.init.ui.decorated.window)
            }
        }

        dependencies {
            commonMainImplementation(libs.ktor.client.core)
            commonMainImplementation(libs.ktor.client.cio)
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

            commonMainImplementation(libs.logback)
            commonMainImplementation(libs.log4j12)
        }
    }
}

compose.desktop {
    application {

        buildTypes.release {
            proguard {
                configurationFiles.from("compose-desktop.pro")
            }
        }

        mainClass = "MainKt"
        jvmArgs += listOf("-Dapple.awt.application.appearance=system")
        jvmArgs += listOf("-XX:ParallelGCThreads=2")
        jvmArgs += listOf("-XX:ConcGCThreads=2")
        jvmArgs += listOf("-Xms30M")
        jvmArgs += listOf("-Xmx30M")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "Passwd"
            packageVersion = "1.0.0"

            modules("java.sql", "java.naming")

            macOS {
                iconFile.set(project.file("icons/app_icon_round_corner.icns"))
            }
            windows {
                shortcut = true
                iconFile.set(project.file("icons/app_icon_round_corner.ico"))
            }
            linux {
                iconFile.set(project.file("icons/app_icon_round_corner.png"))
            }
        }
    }
}
