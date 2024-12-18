@file:Suppress("UnstableApiUsage")

import buildlogic.catalog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

description =
    "A convenient collection of H2 database scripts packaged into a single CLI Tool."

plugins {
    id("buildlogic.kotlin.app")
    alias(libs.plugins.graalbuildtools)
}

application {
    mainClass = "h2databasetool.H2DbToolApp"
    applicationName = "h2db"
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.clikt.markdown)
    implementation(libs.h2)
    testImplementation(kotlin("reflect"))
}

tasks.named("installDist").configure { dependsOn("test") }


tasks.named("build").configure {
    dependsOn("assemble", "installDist")
}

val buildName = "zappy"

abstract class GenerateAppBuildInfo : DefaultTask() {

    init {
        group = "codegen"
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

    @get:Input
    abstract val packageName: Property<String>

    @TaskAction
    fun generate() {
        val outputFile = outputDir.file("BuildInfo.kt").get().asFile
        outputFile.writeText(
            """
            package ${packageName.get()}
            
            /**
            * Build information generated build file.
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

val generateAppBuildInfo by tasks.registering(GenerateAppBuildInfo::class) {
    outputDir.set(layout.buildDirectory.dir("generated/src/kotlin/h2databasetool"))
    appDescription.set(project.description)
    appExe.set(application.applicationName)
    buildDate.set(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    buildOS.set("${System.getProperty("os.name")} (${System.getProperty("os.version")})")
    version.set(project.version.toString())
    versionName.set(buildName)
    h2LibVersion.set(catalog.findVersion("h2").get().requiredVersion)
    packageName.set("h2databasetool")
}


sourceSets {
    main {
        kotlin {
            srcDir(generateAppBuildInfo)
        }
    }
}

tasks.compileKotlin {
    dependsOn(generateAppBuildInfo)
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
