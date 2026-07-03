pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CounterTap"

// Sub-projects — Android apps
include(":counter-tap-business:app")
include(":counter-tap:app")

// Shared Kotlin module
include(":shared")
