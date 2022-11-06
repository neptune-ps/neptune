plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":runescript-parser"))
    runtimeOnly(libs.logback)

    testImplementation(project(":runescript-runtime"))
}

kotlin {
    explicitApi()
}

val compilerTest = tasks.create<JavaExec>("compilerTest") {
    description = "Runs tests for the compiler."
    group = "verification"
    classpath = sourceSets["test"].runtimeClasspath
    main = "me.filby.neptune.runescript.compiler.CompilerTestRunner"
}

tasks.test {
    finalizedBy(compilerTest)
}
