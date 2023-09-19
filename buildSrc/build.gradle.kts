object Dependencies {
    const val AndroidBuildTools = "com.android.tools.build:gradle:8.1.1"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10"
//    const val detektGradlePlugin = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.14.1"
}

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven {
        url = uri("https://jitpack.io")
    }
}
//kotlinDslPluginOptions.experimentalWarning.set(false)
dependencies {
    implementation(Dependencies.AndroidBuildTools)
    implementation(Dependencies.kotlinGradlePlugin)
}
