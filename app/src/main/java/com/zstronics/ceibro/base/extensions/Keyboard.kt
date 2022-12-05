@file:JvmName("KeyboardUtils")

package com.zstronics.ceibro.base.extensions

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment

fun View.showKeyboard(request: Boolean, forced: Boolean) {
    requestFocus()
    if (forced) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
            InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    } else if (request) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }
}

/**
 * Hides the soft keyboard
 * @receiver Activity
 * @return a boolean value if the action was performed or not
 */
fun Activity.hideKeyboard(): Boolean {
    val view = currentFocus
    view?.let {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.hideSoftInputFromWindow(
            view.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
    return false
}

/**
 * Opens up the keyboard by focusing on the view
 * @receiver View
 */
fun View.showKeyboard() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Opens up the keyboard forcefully
 * @receiver Context
 */
fun Context.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

/**
 * Opens up the keyboard forcefully
 * & Focus on view
 * @receiver EditText
 */
fun View.showKeyboardWithFocus() {
    val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    (this as EditText).setRawInputType(InputType.TYPE_CLASS_TEXT)
    this.setTextIsSelectable(true)
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
}


/**
 * Try to hide the keyboard and returns whether it worked
 * https://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
 */
fun View?.hideKeyboard(): Boolean {
    this?.let {
        try {
            val inputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        } catch (ignored: RuntimeException) {
        }
    }
    return false
}

fun Context.hideKeyboard() {
    try {
        val activity = getActivityFromContext(this)
        activity?.hideKeyboard()

    } catch (ignored: Exception) {
    }
}

fun Context.hideSoftKeyboard() {
    try {
        val activity = getActivityFromContext(this)
        activity?.hideKeyboard()

    } catch (ignored: Exception) {
    }

}

private fun getActivityFromContext(@NonNull context: Context): Activity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context
            .baseContext
    }
    return null
}

/**
 * Extension method to provide hide keyboard for [Fragment].
 */
fun Fragment.hideKeyboard() {
    requireActivity().hideKeyboard()
}

/**
 * Extension method to provide hide keyboard for [Fragment].
 */
fun Fragment.showKeyboard() {
    requireActivity().showKeyboard()
}

/**
 * *Extenssion to copy text to clipboard */
fun String?.copyToClipboard(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("YapAccount", this)
    clipboard.setPrimaryClip(clip)
}
