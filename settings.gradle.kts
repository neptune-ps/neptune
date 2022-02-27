rootProject.name = "neptune"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "1.6.10"
        id("org.jmailen.kotlinter") version "3.4.5"
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "runescript-compiler",
    "runescript-parser",
    "runescript-shared"
)
