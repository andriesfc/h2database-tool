
plugins {
    id("buildlogic.projectroot")
}

group = "com.some.sample"
version = "0.0.1"

subprojects {
    group = rootProject.group
    version = rootProject.version
}

dependencies {
    dokkatoo(project(":app"))
    dokkatoo(project(":commons"))
}