import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

defaultTasks("build")

plugins {
    base
    kotlin("jvm")
    id("org.jmailen.kotlinter") apply false
}

allprojects {
    group = "me.filby.neptune"
    version = "0.0.1-SNAPSHOT"

    plugins.withType<BasePlugin> {
        configure<BasePluginConvention> {
            archivesBaseName = "${rootProject.name}-$name"
        }
    }

    plugins.withType<JavaPlugin> {
        configure<JavaPluginExtension> {
            withSourcesJar()

            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    plugins.withType<KotlinPluginWrapper> {
        kotlin {
            explicitApi()
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    plugins.withType<KotlinPluginWrapper> {
        apply(plugin = "org.jmailen.kotlinter")

        dependencies {
            for (module in listOf("stdlib", "stdlib-common", "stdlib-jdk7", "stdlib-jdk8")) {
                api("org.jetbrains.kotlin:kotlin-$module") {
                    version {
                        strictly(kotlinPluginVersion)
                    }
                }
            }

            implementation(libs.inlineLogger)
            implementation(libs.guava)

            testImplementation(kotlin("test-junit5"))
            testImplementation(libs.junit.api)

            testRuntimeOnly(libs.junit.engine)
        }
    }
}
