import buildlogic.java21JvmDefaults

plugins {
    id("buildlogic.kotlin")
    application
}

tasks.register("cleanInstall") {
    description = "Do a clean and then install dist"
    group = "build"
    dependsOn("clean","installDist")
}

application {
    applicationDefaultJvmArgs += java21JvmDefaults
}