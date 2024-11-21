plugins {
    id("buildlogic.kotlin.app")
}


application {
    mainClass = "HelloWorldKt"
}

dependencies {
    implementation(project(":commons"))
}

