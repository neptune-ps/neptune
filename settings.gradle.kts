rootProject.name = "neptune"

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io") {
            content {
                // 4koma and dependencies
                includeModule("cc.ekblad", "4koma")
                includeModule("cc.ekblad.konbini", "konbini")
                includeModule("cc.ekblad.konbini", "konbini-jvm")
            }
        }
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.0"
        id("org.jmailen.kotlinter") version "5.0.1"
    }
}

include(
    "clientscript-compiler",
    "runescript-compiler",
    "runescript-parser",
    "runescript-runtime",
)
