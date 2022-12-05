//    Kotlin
object KotlinDependencies {
    const val KOTLIN_REFLECTION =
        "org.jetbrains.kotlin:kotlin-reflect:${KotlinVersions.STANDARD_LIBRARY}"
    const val KOTLIN_STD_LIB =
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${KotlinVersions.STANDARD_LIBRARY}"

    // kotlin coroutine
    const val COROUTINE_CORE =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${KotlinVersions.COROUTINE_VERSION}"
    const val COROUTINE_ANDROID =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${KotlinVersions.COROUTINE_VERSION}"
}

// Retrofit2 & Networking
object NetworkDependencies {

    const val RETROFIT = "com.squareup.retrofit2:retrofit:${NetworkVersions.RETROFIT}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${NetworkVersions.OKHTTP}"
    const val OKHTTP_LOGGING_INTERCEPTOR =
        "com.squareup.okhttp3:logging-interceptor:${NetworkVersions.OKHTTP}"
    const val RETROFIT_CONVERTOR_GSON =
        "com.squareup.retrofit2:converter-gson:${NetworkVersions.RETROFIT}"
    const val GSON = "com.google.code.gson:gson:${NetworkVersions.GSON}"

    //Glide Image Loading
    const val GLIDE = "com.github.bumptech.glide:glide:${NetworkVersions.GLIDE}"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:${NetworkVersions.GLIDE}"

}

//LifeCycle_KTX
object LifeCycleKtxDependencies {
    const val CORE_KTX = "androidx.core:core-ktx:${LifeCycle_KTX.CORE_KTX}"
    const val LIFECYCL_EEXTENSIONS =
        "androidx.lifecycle:lifecycle-extensions:${LifeCycle_KTX.LIFECYCL_EEXTENSIONS}"
    const val Collection_KTX =
        "androidx.collection:collection-ktx:${LifeCycle_KTX.Collection_KTX}"
    const val VIEW_MODEL_KTX =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${LifeCycle_KTX.LIFECYCLE}"
    const val VIEW_MODEL_SAVE_STATE_KTX =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${LifeCycle_KTX.LIFECYCLE}"
    const val LIVEDATA_KTX =
        "androidx.lifecycle:lifecycle-livedata-ktx:${LifeCycle_KTX.LIFECYCLE}"
    const val LIFECYCLE_RUNTIME =
        "androidx.lifecycle:lifecycle-runtime:${LifeCycle_KTX.LIFECYCLE}"
    const val LIFECYCLE_RUNTIME_KTX =
        "androidx.lifecycle:lifecycle-runtime-ktx:${LifeCycle_KTX.LIFECYCLE}"
    const val LIFECYCLE_COMMON_JAVA =
        "androidx.lifecycle:lifecycle-common-java8:${LifeCycle_KTX.LIFECYCLE}"
    const val REACTIVE_STREAMS =
        "androidx.lifecycle:lifecycle-reactivestreams-ktx:${LifeCycle_KTX.LIFECYCLE}"
}

// Androidx Architecture
// Androidx

object AndroidxDependencies {
    const val APPCOMPAT = "androidx.appcompat:appcompat:${AndroidXVersions.APPCOMPAT}"
    const val FRAGMENT_KTX = "androidx.fragment:fragment-ktx:${AndroidXVersions.FRAGMENT_KTX}"
    const val ACTIVITY_KTX = "androidx.activity:activity-ktx:${AndroidXVersions.ACTIVITY_KTX}"
    const val CONSTRAINT_LAYOUT =
        "androidx.constraintlayout:constraintlayout:${AndroidXVersions.CONSTRAINT_LAYOUT}"
    const val CONSTRAINT_LAYOUT_SOLVER =
        "androidx.constraintlayout:constraintlayout-solver:${AndroidXVersions.CONSTRAINT_LAYOUT}"
    const val CARD_VIEW = "androidx.cardview:cardview:${AndroidXVersions.CARD_VIEW}"
    const val RECYCLERVIEW =
        "androidx.recyclerview:recyclerview:${AndroidXVersions.RECYCLERVIEW}"
    const val VECTOR_DRAWABLE =
        "androidx.vectordrawable:vectordrawable:${AndroidXVersions.VECTOR_DRAWABLE}"
    const val VECTOR_DRAWABLE_ANIMATED =
        "androidx.vectordrawable:vectordrawable-animated:${AndroidXVersions.VECTOR_DRAWABLE}"
    const val BIOMETRIC = "androidx.biometric:biometric:${AndroidXVersions.BIOMETRIC}"
    const val VIEWPAGER2 = "androidx.viewpager2:viewpager2:${AndroidXVersions.VIEWPAGER2}"
    const val SECURITY = "androidx.security:security-crypto:${AndroidXVersions.SECURITY_CRYPTO}"
    const val MATERIAL = "com.google.android.material:material:${AndroidXVersions.MATERIAL}"
    const val PAGING_RUNTIME = "androidx.paging:paging-runtime:${AndroidXVersions.PAGING}"
    const val SWIPE_REFRESH = "androidx.swiperefreshlayout:swiperefreshlayout:${AndroidXVersions.SWIPE_REFRESH}"
    const val WEB_KIT = "androidx.webkit:webkit:${AndroidXVersions.WEB_KIT}"
}

object NavigationDependencies {
    //    Navigation KTX
    const val NAVIGATION_RUNTIME =
        "androidx.navigation:navigation-runtime-ktx:${BuildPluginsVersions.NAVIGATION}"
    const val NAVIGATION_FRAGMENT_KTX =
        "androidx.navigation:navigation-fragment-ktx:${BuildPluginsVersions.NAVIGATION}"
    const val NAVIGATION_UI_KTX =
        "androidx.navigation:navigation-ui-ktx:${BuildPluginsVersions.NAVIGATION}"
}

object FireBaseDependencies {
    //    Google Play Services
    //sms consent
    const val PLAY_SERVICES_AUTH =
        "com.google.android.gms:play-services-auth:${FireBaseVersions.AUTH}"
    const val PLAY_SERVICES_PHONE =
        "com.google.android.gms:play-services-auth-api-phone:${FireBaseVersions.AUTH_PHONE}"

    //Firebase Crashlytics
    const val FIREBASE_CRASHLYTICS =
        "com.google.firebase:firebase-crashlytics-ktx:${FireBaseVersions.FIREBASE_CRASHLYTICS}"
    const val FIREBASE_CRASH =
        "com.google.firebase:firebase-crash:${FireBaseVersions.FIREBASE_CRASH}"
    const val FIREBASE_ANALYTICS =
        "com.google.firebase:firebase-analytics-ktx:${FireBaseVersions.ANALYTICS}"
    const val FIREBASE_MESSAGING =
        "com.google.firebase:firebase-messaging-ktx:${FireBaseVersions.MESSAGING_FCM}"
    const val ADS_IDENTIFIER =
        "com.google.android.gms:play-services-ads-identifier:17.0.1"
    const val PLAY_CORE =
        "com.google.android.play:core-ktx:1.8.1"

    // maps Location Place
    const val PLAY_SERVICES_LOCATION =
        "com.google.android.gms:play-services-location:${FireBaseVersions.ANALYTICS}"
    const val PLAY_SERVICES_PLACES =
        "com.google.android.gms:play-services-places:${FireBaseVersions.PLAY_SERVICES_PLACES}"
    const val PLAY_LIB_PLACE =
        "com.google.android.libraries.places:places:${FireBaseVersions.PLAY_LIB_PLACE}"
    const val PLAY_SERVICES_MAP =
        "com.google.android.gms:play-services-maps:${FireBaseVersions.PLAY_SERVICES_MAP}"
    const val ML_KIT =
        "com.google.mlkit:object-detection:${FireBaseVersions.FIREBASE_ML_KIT}"
    const val ML_KIT_TEXT_RECOGNITION =
        "com.google.android.gms:play-services-mlkit-text-recognition:${FireBaseVersions.FIREBASE_ML_TEXT_RECOGNITION}"
}

//ThirdParty
object ThirdPartyDependencies {
    const val SDP = "com.intuit.sdp:sdp-android:${ThirdPartyVersions.SDP}"
    const val SSP = "com.intuit.ssp:ssp-android:${ThirdPartyVersions.SDP}"
    const val INLINE_ACTIVITY_RESULT =
        "com.github.florent37:inline-activity-result-kotlin:${ThirdPartyVersions.INLINE_ACTIVITY_RESULT}"
    const val IMAGE_SLIDER ="com.github.denzcoskun:ImageSlideshow:0.1.0"
}

object HiltDaggerDependencies {
    // Hilt Dagger DI
    const val DAGGER_HILT = "com.google.dagger:hilt-android:${HiltDaggerVersion.HILT_DI}"
    const val DAGGER_COMPILER =
        "com.google.dagger:hilt-android-compiler:${HiltDaggerVersion.HILT_DI}"
    const val HILT_FRAGMENT = "androidx.hilt:hilt-navigation-fragment:${HiltDaggerVersion.HILT_VM}"
}

object TestDependencies {
    const val JUNIT = "junit:junit:${TestDependenciesVersions.JUNIT4}"
    const val MOCKK = "io.mockk:mockk:${TestDependenciesVersions.MOCKK}"
    const val TESTRUNNER = "androidx.test:runner:${TestDependenciesVersions.TESTRUNNER}"
    const val TESTRULES = "androidx.test:rules:${TestDependenciesVersions.TESTRULES}"
    const val ESPRESSOCORE =
        "androidx.test.espresso:espresso-core:${TestDependenciesVersions.ESPRESSOCORE}"
    const val ESPRESSOINTENTS =
        "androidx.test.espresso:espresso-intents:${TestDependenciesVersions.ESPRESSOINTENTS}"
    const val TESTEXTENSIONS = "androidx.test.ext:junit:${TestDependenciesVersions.TESTEXTENSIONS}"
    const val HILTTESTING =
        "com.google.dagger:hilt-android-testing:${TestDependenciesVersions.HILTTESTING}"
    const val MOCKITO_INLINE =
        "org.mockito:mockito-inline:${TestDependenciesVersions.MOCKITO_INLINE}"
    const val MOCKITO_CORE =
        "org.mockito:mockito-inline:${TestDependenciesVersions.MOCKITO_CORE}"
    const val COROUTINES =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4"
    const val ANDROIDX_ARCH_CORE =
        "androidx.arch.core:core-testing:${TestDependenciesVersions.ANDROIDX_ARCH_CORE}"
}

object Room{

}

