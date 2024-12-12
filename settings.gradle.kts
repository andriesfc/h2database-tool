pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.gradle.enterprise").version("3.19")
}


develocity {
    buildScan {
        fun CI() = System.getenv("CI") != null
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        publishing.onlyIf { true }
        tag(if (CI()) "CI" else "local")
        tag(System.getProperty("os.name"))
        uploadInBackground = CI()
    }
}

rootProject.name = "h2database-tool"

include(":app")
