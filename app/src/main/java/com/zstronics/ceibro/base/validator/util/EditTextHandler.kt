package com.zstronics.ceibro.base.validator.util

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.databinding.adapters.ListenerUtil
import com.google.android.material.textfield.TextInputLayout
import com.zstronics.ceibro.R


/**
 * Created by Muhammad Ibraheem
 */
object EditTextHandler {
    @JvmStatic
    fun removeError(textView: TextView?) {
        setError(textView, null)
    }

    @JvmStatic
    fun setError(textView: TextView?, errorMessage: String?) {
        val textInputLayout = getTextInputLayout(textView)
        if (textInputLayout != null) {
            textInputLayout.isErrorEnabled = !TextUtils.isEmpty(errorMessage)
            textInputLayout.error = errorMessage
//            textInputLayout.setBackgroundResource(R.drawable.input_field_error)
        } else {
            textView?.error = errorMessage
//            textView?.setBackgroundResource(R.drawable.input_field_error)
        }

        if (errorMessage == null){
//            textView?.setBackgroundResource(R.drawable.input_field)
        }
    }

     fun getTextInputLayout(textView: TextView?): TextInputLayout? {
        var textInputLayout: TextInputLayout? = null
        var parent = textView?.parent
        while (parent is View) {
            if (parent is TextInputLayout) {
                textInputLayout = parent
                break
            }
            parent = parent.getParent()
        }
        return textInputLayout
    }

    fun disableErrorOnChanged(textView: TextView?) {
        if (ListenerUtil.getListener<TextWatcher?>(
                textView,
                R.id.text_watcher_clear_error
            ) != null
        ) {
            return
        }
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                setError(textView, null)
            }

            override fun afterTextChanged(s: Editable) {}
        }
        textView?.addTextChangedListener(textWatcher)
        ListenerUtil.trackListener(
            textView,
            textView,
            R.id.text_watcher_clear_error
        )
    }
}