import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.initialization.dsl.ScriptHandler.CLASSPATH_CONFIGURATION

object DependenciesManager {
    val kotlinImplementation = arrayListOf<String>().apply {
        add(KotlinDependencies.KOTLIN_REFLECTION)
        add(KotlinDependencies.KOTLIN_STD_LIB)
        add(KotlinDependencies.COROUTINE_CORE)
        add(KotlinDependencies.COROUTINE_ANDROID)
    }

    val networkImplementation = arrayListOf<String>().apply {
        add(NetworkDependencies.RETROFIT)
        add(NetworkDependencies.OKHTTP)
        add(NetworkDependencies.OKHTTP_LOGGING_INTERCEPTOR)
        add(NetworkDependencies.RETROFIT_CONVERTOR_GSON)
        add(NetworkDependencies.GSON)
        add(NetworkDependencies.GLIDE)
    }
    val networkKapt = arrayListOf<String>().apply {
        add(NetworkDependencies.GLIDE_COMPILER)
    }
    val lifeCycleKtxImplementation = arrayListOf<String>().apply {
        add(LifeCycleKtxDependencies.CORE_KTX)
        add(LifeCycleKtxDependencies.LIFECYCL_EEXTENSIONS)
        add(LifeCycleKtxDependencies.Collection_KTX)
        add(LifeCycleKtxDependencies.VIEW_MODEL_KTX)
        add(LifeCycleKtxDependencies.VIEW_MODEL_SAVE_STATE_KTX)
        add(LifeCycleKtxDependencies.LIVEDATA_KTX)
        add(LifeCycleKtxDependencies.LIFECYCLE_RUNTIME)
        add(LifeCycleKtxDependencies.LIFECYCLE_RUNTIME_KTX)
        add(LifeCycleKtxDependencies.LIFECYCLE_COMMON_JAVA)
        add(LifeCycleKtxDependencies.REACTIVE_STREAMS)
    }
    val androidxImplementation = arrayListOf<String>().apply {
        add(AndroidxDependencies.APPCOMPAT)
        add(AndroidxDependencies.FRAGMENT_KTX)
        add(AndroidxDependencies.ACTIVITY_KTX)
        add(AndroidxDependencies.CONSTRAINT_LAYOUT)
        add(AndroidxDependencies.CONSTRAINT_LAYOUT_SOLVER)
        add(AndroidxDependencies.CARD_VIEW)
        add(AndroidxDependencies.RECYCLERVIEW)
        add(AndroidxDependencies.VECTOR_DRAWABLE)
        add(AndroidxDependencies.VECTOR_DRAWABLE_ANIMATED)
        add(AndroidxDependencies.BIOMETRIC)
        add(AndroidxDependencies.VIEWPAGER2)
        add(AndroidxDependencies.SECURITY)
        add(AndroidxDependencies.MATERIAL)
        add(AndroidxDependencies.PAGING_RUNTIME)
        add(AndroidxDependencies.SWIPE_REFRESH)
        add(AndroidxDependencies.WEB_KIT)

    }
    val navigationImplementation = arrayListOf<String>().apply {
        add(NavigationDependencies.NAVIGATION_RUNTIME)
        add(NavigationDependencies.NAVIGATION_FRAGMENT_KTX)
        add(NavigationDependencies.NAVIGATION_UI_KTX)
    }
    val fireBaseImplementation = arrayListOf<String>().apply {
//        add(FireBaseDependencies.PLAY_SERVICES_AUTH)
        add(FireBaseDependencies.PLAY_SERVICES_PHONE)
        add(FireBaseDependencies.FIREBASE_CRASHLYTICS)
//        add(FireBaseDependencies.ADS_IDENTIFIER)
        add(FireBaseDependencies.FIREBASE_ANALYTICS)
//        add(FireBaseDependencies.FIREBASE_MESSAGING)
//        add(FireBaseDependencies.PLAY_SERVICES_LOCATION)
//        add(FireBaseDependencies.PLAY_SERVICES_PLACES)
//        add(FireBaseDependencies.PLAY_LIB_PLACE)
//        add(FireBaseDependencies.PLAY_SERVICES_MAP)

    }
    val thirdPartyImplementation = arrayListOf<String>().apply {
        add(ThirdPartyDependencies.SDP)
        add(ThirdPartyDependencies.SSP)
        add(ThirdPartyDependencies.INLINE_ACTIVITY_RESULT)
        add(ThirdPartyDependencies.IMAGE_SLIDER)
    }
    val hiltImplementation = arrayListOf<String>().apply {
        add(HiltDaggerDependencies.DAGGER_HILT)
        add(HiltDaggerDependencies.HILT_FRAGMENT)
//       add(HiltDaggerDependencies.HILT_VM)
    }
    val hiltKapt = arrayListOf<String>().apply {
        add(HiltDaggerDependencies.DAGGER_COMPILER)
//        add(HiltDaggerDependencies.HILT_COMPILER)
    }

    val testingImplementation = arrayListOf<String>().apply {
        add(TestDependencies.JUNIT)
        add(TestDependencies.MOCKK)
        add(TestDependencies.TESTEXTENSIONS)
        add(TestDependencies.HILTTESTING)
        add(TestDependencies.COROUTINES)
        add(TestDependencies.ANDROIDX_ARCH_CORE)
    }
    val androidTestImplementation = arrayListOf<String>().apply {
        add(TestDependencies.TESTEXTENSIONS)
        add(TestDependencies.ESPRESSOCORE)
        add(TestDependencies.TESTRUNNER)
        add(TestDependencies.TESTRULES)
        add(TestDependencies.ESPRESSOINTENTS)
        add(TestDependencies.HILTTESTING)
        add(TestDependencies.MOCKITO_CORE)
        add(TestDependencies.MOCKITO_INLINE)
    }
}

//util functions for adding the different type dependencies from build.gradle file
fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestImplementation", dependency)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("testImplementation", dependency)
    }
}

fun DependencyHandler.classpath(list: List<String>) {
    list.forEach { classPath ->
        this.add(CLASSPATH_CONFIGURATION, classPath)
    }
}
