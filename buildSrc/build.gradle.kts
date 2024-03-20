object Dependencies {
    const val AndroidBuildTools = "com.android.tools.build:gradle:8.3.1"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20"
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
    implementation("com.squareup:javapoet:1.13.0")  //Library required by updated HILT library
}
