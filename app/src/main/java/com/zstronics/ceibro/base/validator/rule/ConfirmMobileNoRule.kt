package com.zstronics.ceibro.base.validator.rule

import android.widget.TextView
import androidx.annotation.Keep
import com.google.android.material.textfield.TextInputEditText
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.getDrawable
import com.zstronics.ceibro.base.extensions.isEmpty
import com.zstronics.ceibro.base.validator.util.EditTextHandler
import com.zstronics.ceibro.base.validator.util.EditTextHandler.removeError

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class ConfirmMobileNoRule(
    view: TextView?,
    value: TextView?,
    errorMessage: String?,
    showErrorMessage: Boolean
) : Rule<TextView?, TextView?>(
    view,
    value,
    errorMessage,
    showErrorMessage
) {
    override fun isValid(view: TextView?): Boolean {
        if (value == null) return false
        val value1 = view?.text.toString()
        val value2 = value?.text.toString()
        return !isEmpty(value1) && value1.trim { it <= ' ' } == value2.trim { it <= ' ' }
    }

    override fun onValidationSucceeded(view: TextView?) {
        removeError(view)
    }

    override fun onValidationFailed(view: TextView?) {
        if (errorEnabled) EditTextHandler.setError(view, errorMessage)
    }
}