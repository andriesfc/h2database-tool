import buildlogic.java21JvmDefaults

plugins {
    id("buildlogic.kotlin")
    application
}

application {
    applicationDefaultJvmArgs += java21JvmDefaults
}