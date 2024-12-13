@file:Suppress("UnstableApiUsage")

import buildlogic.catalog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

description = "H2 database tool"

plugins {
    id("buildlogic.kotlin.app")
    alias(libs.plugins.graalbuildtools)

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

abstract class GenerateBuildInfo : DefaultTask() {

    init {
        group = "build"
        description = "Generates Application Build Information"
    }

    @get:Input
    abstract val appDescription: Property<String>

    @get:Input
    abstract val appExe: Property<String>

    @get:Input
    abstract val buildDate: Property<String>

    @get:Input
    abstract val buildOS: Property<String>

    @get:Input
    abstract val version: Property<String>

    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val h2LibVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputFile = outputDir.file("BuildInfo.kt").get().asFile
        outputFile.writeText(
            """
            package h2databasetool
            
            /**
            * Build information structure generated via the actual Gradle build. (have a look at the `:app:generateBuildInfo` task).
            */
            data object BuildInfo {
                /** Official application description. */
                const val APP_DESCRIPTION = "${appDescription.get()}"
                /** Name of the application command script taken from the application plugin configuration.*/
                const val APP_EXE = "${appExe.get()}"
                /** Date this build info was gestated. */
                const val BUILD_DATE = "${buildDate.get()}"

                /** The operating system used to produce this build.*/
                const val BUILD_OS = "${buildOS.get()}"
                /** Tool version used for this build. */
                const val VERSION = "${version.get()}"
                /** Name of the build */
                const val VERSION_NAME = "${versionName.get()}"
                /** Which version of the H2 library included in this tool. */
                const val H2_LIB_VERSION = "${h2LibVersion.get()}"
            }
        """.trimIndent()
        )
    }
}

val generateBuildInfo by tasks.registering(GenerateBuildInfo::class) {
    outputDir.set(layout.buildDirectory.dir("generated/src/kotlin/h2databasetool"))
    appDescription.set(project.description)
    appExe.set(application.applicationName)
    buildDate.set(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    buildOS.set("${System.getProperty("os.name")} (${System.getProperty("os.version")})")
    version.set(project.version.toString())
    versionName.set(buildName)
    h2LibVersion.set(catalog.findVersion("h2").get().requiredVersion)
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



graalvmNative {

    fun osName() = System.getProperty("os.name").lowercase(Locale.ENGLISH).replace(" ", "")
    fun osArch() = System.getProperty("os.arch").lowercase(Locale.ENGLISH).replace(" ", "")

    binaries.all {
        resources.autodetect()
    }
    metadataRepository {
        version = "0.1.0"
    }
    binaries {
        named("main") {
            sharedLibrary = false
            imageName.set(application.applicationName)
            mainClass.set(application.mainClass.get())
            when {
                osName() == "macosx" && osArch() == "x86_64" -> buildArgs("-march=skylake")
                else -> buildArgs("-march=native")
            }
        }
    }
}
