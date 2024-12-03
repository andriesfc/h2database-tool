import buildlogic.catalog
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
val h2LibVersion = catalog.findVersion("h2").get().requiredVersion

val generateBuildInfo by tasks.registering {
    group = "build"
    description = "Generates Application Build Information"
    val output = layout.buildDirectory.dir("generated/src/kotlin/h2databasetool")
    outputs.dir(output)
    doLast {
        //language=kotlin
        output.get().asFile.resolve("BuildInfo.kt").writeText(
            """
            package h2databasetool
            
            /**
             * Build information structure generated via the actual Gradle build. (have a look at the `:app:$name` task).
             */
            data object BuildInfo {
                /** Official application description. */
                const val APP_DESCRIPTION = "${project.description}"
                /** Name of the application command script taken from the application plugin configuration.*/
                const val APP_EXE = "${application.applicationName}"
                /** Date this build info was gestated. */
                const val BUILD_DATE = "${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}"
                /** The operating system used to produce this build.*/
                const val BUILD_OS = "${System.getProperty("os.name")} (${System.getProperty("os.version")})"
                /** Tool version used for this build. */
                const val VERSION = "${project.version}"
                /** Name of the build */
                const val VERSION_NAME = "$buildName"
                /** Which version of the H2 library included in this tool. */
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

graalvmNative {
    binaries.all {
        resources.autodetect()
    }
    binaries {
        named("main") {
            sharedLibrary = false
            imageName.set(application.applicationName)
            mainClass.set(application.mainClass.get())
        }
    }
}
