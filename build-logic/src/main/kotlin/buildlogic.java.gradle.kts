@file:Suppress("UnstableApiUsage")

import buildlogic.catalog
import buildlogic.lib
import buildlogic.version

plugins {
    `java-library`
}

repositories {
    mavenCentral()
    if ("${rootProject.version}".endsWith("-SNAPSHOT", ignoreCase = true))
        maven("https://oss.sonatype.org/content/repositories/snapshots")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Test>() {
    // todo: This should suppress message on JDK 21, but is not!
    val jvmArgs = buildlogic.java21JvmDefaults + "-XX:+EnableDynamicAgentLoading"
    useJUnitPlatform {
        dependencies {
            testImplementation(catalog.lib("junit-jupiter-api"))
            testRuntimeOnly(catalog.lib("junit-jupiter-engine"))
            jvmArgs(jvmArgs)
        }
    }
}
// useJUnitJupiter(catalog.version("junit-jupiter").preferredVersion)

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(catalog.version("junit-jupiter").preferredVersion)
        }
    }
}

