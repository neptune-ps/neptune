plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":runescript-compiler"))
    runtimeOnly(libs.logback)
}
