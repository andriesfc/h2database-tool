@file:Suppress("UnstableApiUsage")

import buildlogic.catalog
import buildlogic.ensureParents
import buildlogic.lib
import org.gradle.kotlin.dsl.support.useToRun
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    id("buildlogic.java")
    id("org.jetbrains.kotlin.jvm")
    id("buildlogic.dokkatoo")
}

repositories {
    mavenCentral()
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(catalog.lib("io-kotest-runner-junit5"))
                implementation(catalog.lib("io-kotest-assertions-core"))
                implementation(catalog.lib("io-kotest-framework-datatest"))
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        // Flag ensures all codes get compiled with JDK 21
        // compatible runtime libraries:
        freeCompilerArgs.add("-Xjdk-release=21")
        freeCompilerArgs.add("-Xwhen-guards")
        freeCompilerArgs.add("-Xnon-local-break-continue")
        freeCompilerArgs.add("-Xmulti-dollar-interpolation")
    }
}

val optimizeKotestSettings by tasks.registering {
    group = "build"
    description =
        "Initializes either a new kotest.properties file, or updated it with defaults if it exists but not using the defaults."

    val propsFile = project.layout.projectDirectory.file("src/test/resources/kotest.properties")
    val defaultProps = listOf(
        "kotest.framework.classpath.scanning.config.disable" to "true",
        "kotest.framework.classpath.scanning.autoscan.disable" to "true",
    )
    doLast {

        val props = Properties()

        propsFile.asFile.takeIf { it.exists() }?.reader()?.use(props::load)

        val modified = defaultProps
            .fold(false) { updated, (defaultProperty, defaultValue) ->
                val updating = props[defaultProperty] == null
                if (updating) props[defaultProperty] = defaultValue
                updated || updating
            }

        if (modified) propsFile
            .asFile
            .ensureParents()
            .outputStream()
            .useToRun { props.store(this, null) }

    }
}

