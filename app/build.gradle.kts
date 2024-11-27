import java.time.LocalDate
import java.time.format.DateTimeFormatter

description = "H2 database tool"

plugins {
    id("buildlogic.kotlin.app")
}

application {
    mainClass = "h2databasetool.app.H2ToolMain"
    applicationName = "h2"
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.clikt.markdown)
    implementation(libs.h2)
    implementation(libs.jdbi.core)
    implementation(libs.jdbi.kotlin)
    implementation(libs.sl4j.api)
    implementation(libs.logback.classic)
}

defaultTasks("clean", "build", "installDist")

val buildName = "Ygdrasil"

val generateBuildInfo by tasks.registering {
    group = "build"
    description = "Generates Application Build Information"
    val output = layout.buildDirectory.dir("generated/src/kotlin/h2databasetool")
    outputs.dir(output)
    doLast {
        output.get().asFile.resolve("BuildInfo.kt").writeText(
            """
            package h2databasetool
            
            object BuildInfo {
                const val APP_DESCRIPTION = "${project.description}"
                const val APP_NAME = "${application.applicationName}"
                const val BUILD_DATE = "${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                const val BUILD_OS = "${System.getProperty("os.name")} (${System.getProperty("os.version")})"
                const val VERSION = "${project.version}"
                const val VERSION_NAME = "$buildName"
            }
            """.trimIndent()
        )
    }
}



sourceSets {
    main {
        kotlin {
            srcDir(generateBuildInfo)
        }
    }
}

tasks.compileKotlin {
    dependsOn(generateBuildInfo)
}