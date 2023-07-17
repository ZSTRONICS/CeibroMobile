plugins {
    id(BuildPluginsConfig.androidApplication)
    kotlin(BuildPluginsConfig.kotlinAndroid)
    kotlin(BuildPluginsConfig.kotlinKapt)
    id(BuildPluginsConfig.kotlinParcelize)
    id(BuildPluginsConfig.androidHilt)
    id(BuildPluginsConfig.navigationSafeArgs)

    // Internal Script plugins
    id(ScriptPlugins.variants)
    id(ScriptPlugins.compilation)
//    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AppConfig.MIN_SDK_VERSION
        targetSdk = AppConfig.TARGET_SDK_VERSION
        multiDexEnabled = true
        applicationId = AppConfig.APP_ID
        versionCode = AppConfig.VERSION_CODE
        versionName = AppConfig.VERSION_NAME
        testInstrumentationRunner = AppConfig.androidTestInstrumentation
    }
    viewBinding {
        android.buildFeatures.viewBinding = false
    }

    dataBinding {
        android.buildFeatures.dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    namespace = "com.zstronics.ceibro"

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(DependenciesManager.kotlinImplementation)
    implementation(DependenciesManager.lifeCycleKtxImplementation)
    implementation(DependenciesManager.androidxImplementation)
    implementation(DependenciesManager.navigationImplementation)
    implementation(DependenciesManager.thirdPartyImplementation)
    implementation(DependenciesManager.networkImplementation)
    implementation(DependenciesManager.hiltImplementation)
    implementation(DependenciesManager.workerDependencies)
    implementation(project(mapOf("path" to ":ceibrocamera")))

    kapt(DependenciesManager.hiltKapt)
    kapt(DependenciesManager.networkKapt)
    testImplementation(DependenciesManager.testingImplementation)
    androidTestImplementation(DependenciesManager.androidTestImplementation)
    implementation(project(mapOf("path" to ":permissionx")))

    // Room components
    implementation("android.arch.persistence.room:runtime:1.1.1")
    annotationProcessor("android.arch.persistence.room:compiler:1.1.1")
    implementation("androidx.room:room-ktx:2.5.0")
    implementation("androidx.room:room-runtime:2.5.0")
    implementation("androidx.room:room-common:2.5.0")
    kapt("androidx.room:room-compiler:2.5.0")

    implementation("io.socket:socket.io-client:2.0.0"){
        exclude("org.json","json")
    }
    implementation("com.github.clans:fab:1.6.4")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.ericktijerou.koleton:koleton:1.0.0-beta01")
    implementation("com.github.tntkhang:full-screen-image-view-library:1.1.0")
    implementation("com.onesignal:OneSignal:[4.0.0, 4.99.99]")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.30")
    implementation("com.hbb20:ccp:2.6.1")
    implementation("io.ak1.pix:piximagepicker:1.6.3")
    implementation("androidx.paging:paging-runtime-ktx:3.1.1")
}
