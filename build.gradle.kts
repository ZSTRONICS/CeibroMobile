plugins {
    id(BuildPluginsConfig.androidApplication) apply false
    id(BuildPluginsConfig.androidLibrary) apply false
    kotlin(BuildPluginsConfig.kotlinAndroid) apply false
    kotlin(BuildPluginsConfig.kotlinKapt) apply false
    id(BuildPluginsConfig.kotlinParcelize) apply false
//    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(BuildClassesConfig.ANDROID_GRADLE_PLUGIN)
        classpath(BuildClassesConfig.KOTLIN_GRADLE_PLUGIN)
        classpath(BuildClassesConfig.NAVIGATION_SAFE_ARGS)
        classpath(BuildClassesConfig.HILT_GRADLE_PLUGIN)
        classpath("com.squareup:javapoet:1.13.0")
        classpath("com.google.gms:google-services:4.4.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.google.com")
        }
        jcenter()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
subprojects {
    apply {
    }
}
