package com.zstronics.ceibro.base.validator.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.validator.rule.DateRule
import com.zstronics.ceibro.base.validator.util.EditTextHandler
import com.zstronics.ceibro.base.validator.util.ErrorMessageHelper
import com.zstronics.ceibro.base.validator.util.ViewTagHelper

/**
 * Created by Muhammad Ibraheem
 */
object DateBindings {
    @BindingAdapter(
        value = ["validateDate", "validateDateMessage", "validateDateAutoDismiss", "errorEnabled"],
        requireAll = false
    )
    @JvmStatic
    fun bindingDate(
        view: TextView?,
        pattern: String?,
        errorMessage: String?,
        autoDismiss: Boolean,
        errorEnabled: Boolean
    ) {
        if (autoDismiss) {
            EditTextHandler.disableErrorOnChanged(view)
        }
        val handledErrorMessage = ErrorMessageHelper.getStringOrDefault(
            view,
            errorMessage, R.string.error_message_date_validation
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule,
            view,
            DateRule(view, pattern, handledErrorMessage, errorEnabled)
        )
    }
}