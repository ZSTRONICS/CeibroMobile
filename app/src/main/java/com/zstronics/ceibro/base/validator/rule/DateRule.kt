package com.zstronics.ceibro.base.validator.rule

import android.widget.TextView
import androidx.annotation.Keep
import com.zstronics.ceibro.base.validator.util.DateValidator
import com.zstronics.ceibro.base.validator.util.EditTextHandler.removeError

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class DateRule(
    view: TextView?,
    value: String?,
    errorMessage: String?,
    showErrorMessage: Boolean
) : Rule<TextView?, String?>(
    view,
    value,
    errorMessage,
    showErrorMessage
) {
    private val dateValidator: DateValidator = DateValidator()
    override fun isValid(view: TextView?): Boolean {
        return dateValidator.isValid(view?.text.toString(), value)
    }

    override fun onValidationSucceeded(view: TextView?) {
        removeError(view)
    }

    override fun onValidationFailed(view: TextView?) {
//        setError(view, errorMessage)
    }

}