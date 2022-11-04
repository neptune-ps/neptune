plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(project(":runescript-compiler"))
}

kotlin {
    explicitApi()
}
