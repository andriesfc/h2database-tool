
plugins {
    id("buildlogic.projectroot")
}

group = "io.github.andriesfc.h2database-tool"
version = "0.0.1"

subprojects {
    group = rootProject.group
    version = rootProject.version
}

dependencies {
    dokkatoo(project(":app"))
}