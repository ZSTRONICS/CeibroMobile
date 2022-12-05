package com.zstronics.ceibro.base.validator.rule

import android.widget.TextView
import androidx.annotation.Keep
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.getDrawable
import com.zstronics.ceibro.base.extensions.getValueWithoutComaAndSpace
import com.zstronics.ceibro.base.validator.util.EditTextHandler

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class RegexRule(
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
    override fun isValid(view: TextView?): Boolean {
        return view?.text.toString().matches(value?.toRegex()!!)
    }

    override fun onValidationSucceeded(view: TextView?) {
//        view?.apply {
//            setCompoundDrawablesWithIntrinsicBounds(
//                compoundDrawables[0],
//                compoundDrawables[1],
//                getDrawable(R.drawable.ic_path),
//                compoundDrawables[3]
//            )
//        }
        EditTextHandler.removeError(view)
    }

    override fun onValidationFailed(view: TextView?) {
        if (errorEnabled) EditTextHandler.setError(view, errorMessage)
//        view?.apply {
//            setCompoundDrawablesWithIntrinsicBounds(
//                compoundDrawables[0],
//                compoundDrawables[1],
//                null,
//                compoundDrawables[3]
//            )
//        }
    }
}