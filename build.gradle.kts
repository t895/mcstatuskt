plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    id("org.jetbrains.kotlinx.atomicfu") version "0.25.0" apply false
}
