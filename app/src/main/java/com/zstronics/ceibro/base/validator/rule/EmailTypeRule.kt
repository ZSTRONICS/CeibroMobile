package com.zstronics.ceibro.base.validator.rule

import android.util.Patterns
import android.widget.TextView
import androidx.annotation.Keep
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.getDrawable
import com.zstronics.ceibro.base.validator.util.EditTextHandler

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class EmailTypeRule(
    view: TextView?,
    errorMessage: String?,
    errorEnabled: Boolean
) : TypeRule(
    view,
    FieldType.Email,
    errorMessage,
    errorEnabled
) {
    override fun isValid(view: TextView?): Boolean {
        val emailPattern = Patterns.EMAIL_ADDRESS
        return emailPattern.matcher(view?.text).matches()
    }

    override fun onValidationSucceeded(view: TextView?) {
        super.onValidationSucceeded(view)
        EditTextHandler.removeError(view)

    }

    override fun onValidationFailed(view: TextView?) {
        super.onValidationFailed(view)
        if (errorEnabled) EditTextHandler.setError(view, errorMessage)
    }
}