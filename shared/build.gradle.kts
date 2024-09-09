import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    id("org.jetbrains.kotlinx.atomicfu")
    id("com.vanniktech.maven.publish")
}

group = "com.t895.mcstatuskt"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    listOf(
        macosX64(),
        macosArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    linuxX64 {
        binaries.staticLib {
            baseName = "shared"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.core)
            implementation(libs.ktor.network)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }
}

android {
    namespace = "com.t895.mcstatuskt"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.t895",
        artifactId = "mcstatuskt",
        version = "0.1.0"
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("mcstatuskt")
        description.set("Simple Minecraft Server pinger")
        inceptionYear.set("2024")
        url.set("https://github.com/t895/mcstatuskt")

        licenses {
            license {
                name.set("GPLv3")
                url.set("https://opensource.org/licenses/gpl-3-0")
            }
        }

        // Specify developer information
        developers {
            developer {
                id.set("t895")
                name.set("Charles Lombardo")
                email.set("clombardo169@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/t895/mcstatuskt")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}
