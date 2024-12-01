import buildlogic.catalog
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
    testImplementation(kotlin("reflect"))
}

tasks.named("build").configure { dependsOn("assemble", "installDist", "test") }

val buildName = "arbolis"
val h2LibVersion = catalog.findVersion("h2").get().requiredVersion

val generateBuildInfo by tasks.registering {
    group = "build"
    description = "Generates Application Build Information"
    val output = layout.buildDirectory.dir("generated/src/kotlin/h2databasetool")
    outputs.dir(output)
    doLast {
        val h2Version =
            output.get().asFile.resolve("BuildInfo.kt").writeText(
                """
            package h2databasetool
            
            data object BuildInfo {
                const val APP_DESCRIPTION = "${project.description}"
                const val APP_EXE = "${application.applicationName}"
                const val BUILD_DATE = "${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                const val BUILD_OS = "${System.getProperty("os.name")} (${System.getProperty("os.version")})"
                const val VERSION = "${project.version}"
                const val VERSION_NAME = "$buildName"
                const val H2_LIB_VERSION = "$h2LibVersion"
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