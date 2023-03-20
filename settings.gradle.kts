rootProject.name = "neptune"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "1.7.21"
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
