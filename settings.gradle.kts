import java.util.*

pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("com.gradle.develocity").version("3.19")
}


develocity {
    buildScan {
        fun CI() = System.getenv("CI") != null
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        publishing.onlyIf { CI() }
        tag(if (CI()) "CI" else "local")
        val os = System.getProperty("os.name").lowercase(Locale.ENGLISH).replace(" ", "")
        val osVersion = System.getProperty("os.version").lowercase(Locale.ENGLISH).replace(" ", "")
        val osArchitecture = System.getProperty("os.arch").lowercase(Locale.ENGLISH).replace(" ", "")
        tag("$os-$osVersion-$osArchitecture")
        tag(System.getProperty("os.name"))
        uploadInBackground = CI()
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

rootProject.name = "h2database-tool"

include(":app")
