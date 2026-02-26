plugins {
    kotlin("jvm")
    antlr
}

val antlrSource = "src/main/antlr"
val antlrOutput = "src/main/java/me/filby/neptune/runescript/antlr"
val antlrPackage = "me.filby.neptune.runescript.antlr"

dependencies {
    antlr(libs.antlrCore)
    api(libs.antlrRuntime)
}

// the gradle antlr plugin adds all of antlr to runtimeClasspath,
// workaround that https://github.com/gradle/gradle/issues/820
configurations {
    api {
        setExtendsFrom(extendsFrom.filterNot { it == antlr.get() })
    }
}

kotlin {
    explicitApi()
}

tasks.compileKotlin {
    // depend on the grammar source generation, this is implicit for compileJava but not compileKotlin
    dependsOn(tasks.generateGrammarSource)
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

task("antlrOutputDir") {
    doLast {
        mkdir(antlrOutput)
    }
}

tasks.sourcesJar {
    dependsOn(tasks.generateGrammarSource)
}

tasks.kotlinSourcesJar {
    dependsOn(tasks.generateGrammarSource)
}

tasks.generateGrammarSource {
    dependsOn("antlrOutputDir")

    outputDirectory = file(antlrOutput)

    val grammars = fileTree(antlrSource) { include("**/*.g4") }.files.map { it.toString() }
    arguments = listOf("-no-listener", "-visitor", "-package", antlrPackage) + grammars
}

tasks.withType<org.jmailen.gradle.kotlinter.tasks.LintTask> {
    // explicit dependency
    dependsOn(tasks.generateGrammarSource)
}
