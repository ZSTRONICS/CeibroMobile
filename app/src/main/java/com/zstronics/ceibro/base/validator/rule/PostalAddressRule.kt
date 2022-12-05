package com.zstronics.ceibro.base.validator.rule

import android.widget.TextView
import androidx.annotation.Keep
import com.zstronics.ceibro.base.validator.util.EditTextHandler.removeError
import com.zstronics.ceibro.base.validator.util.EditTextHandler.setError

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class PostalAddressRule(
    view: TextView?,
    value: FieldType?,
    errorMessage: String?,
    errorEnabled: Boolean
) : TypeRule(view, value, errorMessage, errorEnabled) {
    override fun isValid(view: TextView?): Boolean {
        val postalAddress = view?.text.toString()
        return postalAddress.matches("\\\\d+\\\\s+([a-zA-Z]+|[a-zA-Z]+\\\\s[a-zA-Z]+)".toRegex())
    }

    override fun onValidationSucceeded(view: TextView?) {
        super.onValidationSucceeded(view)
        removeError(view)
    }

    override fun onValidationFailed(view: TextView?) {
        super.onValidationFailed(view)
        if (errorEnabled) setError(view, errorMessage)
    }
}
