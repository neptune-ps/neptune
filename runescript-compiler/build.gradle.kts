plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":runescript-parser"))
    runtimeOnly(libs.logback)

    testImplementation(project(":runescript-runtime"))
}

kotlin {
    explicitApi()
}

val compilerTest = tasks.register<JavaExec>("compilerTest") {
    description = "Runs tests for the compiler."
    group = "verification"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("me.filby.neptune.runescript.compiler.CompilerTestRunner")
}

tasks.test {
    finalizedBy(compilerTest)
}
