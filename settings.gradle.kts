pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.gradle.enterprise").version("3.7.2")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlwaysIf(System.getenv("CI") == "true")
    }
}

rootProject.name = "h2database-tool"

include(":app")
