rootProject.name = "neptune"

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.22"
        id("org.jmailen.kotlinter") version "3.4.5"
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "clientscript-compiler",
    "runescript-compiler",
    "runescript-parser",
    "runescript-runtime",
)
