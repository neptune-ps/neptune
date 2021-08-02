rootProject.name = "neptune"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "1.5.0"
        id("org.jmailen.kotlinter") version "3.4.5"
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

include(
    "runescript-compiler",
    "runescript-parser",
    "runescript-shared"
)
