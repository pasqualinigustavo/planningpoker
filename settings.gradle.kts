pluginManagement {
    includeBuild("build-logic")
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "PlanningPoker"

include(":app")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:ai")
include(":core:designsystem")
include(":core:ui")
include(":feature:sprints")
include(":feature:tasks")
