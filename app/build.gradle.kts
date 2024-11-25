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

