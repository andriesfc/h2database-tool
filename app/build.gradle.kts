description = "H2 database tool"

plugins {
    id("buildlogic.kotlin.app")
}

application {
    mainClass = "h2databasetool.app.ToolMain"
    applicationName = "h2"
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.clikt.markdown)
    implementation(libs.h2)
}

