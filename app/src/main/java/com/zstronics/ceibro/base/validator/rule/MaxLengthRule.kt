package com.zstronics.ceibro.base.validator.rule

import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import com.zstronics.ceibro.base.validator.util.EditTextHandler

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class MaxLengthRule(
    view: TextView?,
    value: Int?,
    errorMessage: String?,
    showErrorMessage: Boolean
) : Rule<TextView?, Int?>(
    view,
    value,
    errorMessage,
    showErrorMessage
) {
    override fun isValid(view: TextView?): Boolean {
        return view?.visibility == View.GONE || view?.length()!! <= value!!
    }

    override fun onValidationSucceeded(view: TextView?) {
        EditTextHandler.removeError(view)
    }

    override fun onValidationFailed(view: TextView?) {
        if (errorEnabled) EditTextHandler.setError(view, errorMessage)
    }
}