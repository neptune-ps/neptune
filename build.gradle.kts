import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

defaultTasks("build")

plugins {
    base
    kotlin("jvm")
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

            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    plugins.withType<KotlinPluginWrapper> {
        kotlin {
            explicitApi()
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(11)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    plugins.withType<KotlinPluginWrapper> {
        dependencies {
            api(libs.kotlin.stdlib)

            implementation(kotlin("reflect"))
            implementation(libs.inlineLogger)
            implementation(libs.guava)

            testImplementation(kotlin("test-junit5"))
            testImplementation(libs.junit.api)

            testRuntimeOnly(libs.junit.engine)
        }
    }
}
