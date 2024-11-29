import java.time.LocalDate
import java.time.format.DateTimeFormatter

description = "H2 database tool"

plugins {
    id("buildlogic.kotlin.app")
}

application {
    mainClass = "h2databasetool.ApplicationKt"
    applicationName = "h2"
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.clikt.markdown)
    implementation(libs.h2)
}

defaultTasks("clean", "build", "installDist")

val buildName = "ygdrasil"

val generateBuildInfo by tasks.registering {
    group = "build"
    description = "Generates Application Build Information"
    val output = layout.buildDirectory.dir("generated/src/kotlin/h2databasetool")
    outputs.dir(output)
    doLast {
        output.get().asFile.resolve("BuildInfo.kt").writeText(
            """
            package h2databasetool
            
            data object BuildInfo {
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