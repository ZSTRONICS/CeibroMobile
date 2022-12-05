package com.zstronics.ceibro.base.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import com.zstronics.ceibro.base.*

/**
 * Extensions for simpler launching of Activities
 */
fun Fragment.finishAffinity() {
    activity?.finishAffinity()
}

fun Fragment.finish() {
    activity?.finish()
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

/**
 * Extension method to get a new Intent for an Activity class
 */
inline fun <reified T : Any> Context.intent() = Intent(this, T::class.java)

/**
 * Create an intent for [T] and apply a lambda on it
 */
inline fun <reified T : Any> Context.intent(body: Intent.() -> Unit): Intent {
    val intent = Intent(this, T::class.java)
    intent.body()
    return intent
}

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    clearPrevious: Boolean = false,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    intent.putExtra(EXTRA, options)
    if (clearPrevious) finish()
    startActivityForResult(intent, requestCode, options)

}

inline fun <reified T : Any> FragmentActivity.launchActivityForResult(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {},
    noinline completionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = null
) {
    completionHandler?.let {
        val intent = newIntent<T>(this)
        intent.init()
        intent.putExtra(EXTRA, options)
        this@launchActivityForResult.startForResult(intent) { result ->
            it.invoke(result.resultCode, result.data)
        }.onFailed { result ->
            it.invoke(result.resultCode, result.data)
        }
    } ?: run {
        launchActivity<T>(
            requestCode = requestCode,
            options = options,
            init = init
        )
    }
}

inline fun <reified T : Any> Fragment.launchActivity(
    options: Bundle? = null,
    clearPrevious: Boolean = false,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(requireContext())
    intent.init()
    intent.putExtra(EXTRA, options)
    startActivity(intent, options)
    if (clearPrevious) finish()

}

inline fun <reified T : Any> Fragment.launchActivityWithFinishAffinity(
    options: Bundle? = null,
    clearPrevious: Boolean = false,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(requireContext())
    intent.init()
    intent.putExtra(EXTRA, options)
    startActivity(intent, options)
    if (clearPrevious) finishAffinity()

}

//inline fun <reified T : Any> Fragment.launchActivity(
//    options: Bundle? = null,
//    clearPrevious: Boolean = false,
//    noinline init: Intent.() -> Unit = {},
//    noinline onActivityResult: ((resultCode: Int, data: Intent?) -> Unit)? = null
//) {
//    val intent = newIntent<T>(requireContext())
//    intent.init()
//    intent.putExtra(EXTRA, options)
//    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        onActivityResult?.invoke(it.resultCode, it.data)
//    }
//    if (clearPrevious) finish()
//    launcher.launch(intent)
//}

inline fun <reified T : Any> Fragment.launchActivityForResult(
    options: Bundle? = null, clearPrevious: Boolean = false,
    noinline init: Intent.() -> Unit = {},
    noinline onActivityResult: ((resultCode: Int, data: Intent?) -> Unit)? = null
) {

    onActivityResult?.let {
        val intent = newIntent<T>(requireContext())
        intent.init()
        intent.putExtra(EXTRA, options)
        this.startForResult(intent) { result ->
            it.invoke(result.resultCode, result.data)
        }.onFailed { result ->
            it.invoke(result.resultCode, result.data)
        }
    } ?: run {
        launchActivity<T>(clearPrevious = clearPrevious, options = options)
    }
}

fun <T : Fragment> AppCompatActivity.instantiateFragment(fragmentName: String) =
    Fragment.instantiate(this, fragmentName)

fun Fragment.instantiateFragment(fragmentName: String) {
    Fragment.instantiate(this.requireActivity(), fragmentName)
    // FragmentFactory.instantiate
}

fun <T : Fragment> FragmentActivity.createFragmentInstance(
    fragment: T, containerId: Int,
    bundle: Bundle = Bundle()
): T {
    fragment.arguments = bundle
    replaceFragment(fragment, containerId, bundle)
    return fragment
}

fun <T : Fragment> FragmentActivity.createFragmentInstance(fragment: T): T {
    replaceFragment(fragment, android.R.id.content)
    return fragment
}

fun FragmentActivity.replaceFragment(
    fragment: Fragment, @IdRes container: Int, bundle: Bundle = Bundle(),
    addToBackStack: Boolean = false, backStackName: String = "",
    @AnimRes inAnimationRes: Int = 0, @AnimRes outAnimationRes: Int = 0
) {
    val ft = supportFragmentManager.beginTransaction()
    if (inAnimationRes != 0 && outAnimationRes != 0) {
        ft.setCustomAnimations(inAnimationRes, outAnimationRes)
    }
    ft.replace(container, fragment)

    if (addToBackStack) {
        ft.addToBackStack(backStackName)
    }

    ft.commit()
}

fun FragmentActivity.addFragment(
    fragment: Fragment, @IdRes container: Int,
    addToBackStack: Boolean = false, backStackName: String = "",
    @AnimRes inAnimationRes: Int = 0, @AnimRes outAnimationRes: Int = 0
) {
    val ft = supportFragmentManager.beginTransaction()
    if (inAnimationRes != 0 && outAnimationRes != 0) {
        ft.setCustomAnimations(inAnimationRes, outAnimationRes)
    }
    ft.add(container, fragment)

    if (addToBackStack) {
        ft.addToBackStack(backStackName)
    }

    ft.commit()
}

//fun <T : Fragment> FragmentActivity.startFragmentForResult(
//    fragmentName: String,
//    bundle: Bundle = Bundle(),
//    showToolBar: Boolean = false,
//    toolBarTitle: String = "",
//    completionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = null
//) {
//    val intent = Intent(this, FrameActivity::class.java)
//    try {
//        intent.putExtra(FRAGMENT_CLASS, fragmentName)
//        intent.putExtra(EXTRA, bundle)
//        intent.putExtra(SHOW_TOOLBAR, showToolBar)
//        intent.putExtra(TOOLBAR_TITLE, toolBarTitle)
//        (this as AppCompatActivity).startForResult(intent) { result ->
//            completionHandler?.invoke(result.resultCode, result.data)
//        }.onFailed { result ->
//            completionHandler?.invoke(result.resultCode, result.data)
//        }
//
//    } catch (e: Exception) {
//        if (e is ClassNotFoundException) {
//            toast("Something went wrong")
//            startActivity(intent)
//        }
//    }
//}

inline fun <reified a : AppCompatActivity> Fragment.startFragmentForResult(
    fragmentName: String,
    bundle: Bundle = Bundle(),
    showToolBar: Boolean = false,
    toolBarTitle: String = "",
    homeAsUpIndicator: Int = 0,
    crossinline completionHandler: ((resultCode: Int, data: Intent?) -> Unit)
) {
    val intent = Intent(requireActivity(), a::class.java)
    try {
        intent.putExtra(FRAGMENT_CLASS, fragmentName)
        intent.putExtra(EXTRA, bundle)
        intent.putExtra(SHOW_TOOLBAR, showToolBar)
        intent.putExtra(TOOLBAR_TITLE, toolBarTitle)
        intent.putExtra(HOME_AS_UP_INDICATOR, homeAsUpIndicator)
        this.startForResult(intent) { result ->
            completionHandler.invoke(result.resultCode, result.data)
        }.onFailed { result ->
            completionHandler.invoke(result.resultCode, result.data)
        }

    } catch (e: Exception) {
        if (e is ClassNotFoundException) {
            toast("Something went wrong")
            startActivity(intent)
        }
    }
}

// Extension function for manually notifying the livedata observers
fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

fun Fragment.readRaw(@RawRes resourceId: Int): String {
    return resources.openRawResource(resourceId).bufferedReader(Charsets.UTF_8)
        .use { it.readText() }
}
