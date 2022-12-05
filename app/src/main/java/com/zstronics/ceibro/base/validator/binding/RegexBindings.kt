package com.zstronics.ceibro.base.validator.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.validator.rule.RegexRule
import com.zstronics.ceibro.base.validator.util.EditTextHandler
import com.zstronics.ceibro.base.validator.util.ErrorMessageHelper
import com.zstronics.ceibro.base.validator.util.ViewTagHelper

/**
 * Created by Muhammad Ibraheem
 */
object RegexBindings {
    @BindingAdapter(
        value = ["app:validateRegex", "validateRegexMessage", "validateRegexAutoDismiss", "errorEnabledRegex"],
        requireAll = false
    )
    @JvmStatic
    fun bindingRegex(
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
            errorMessage, R.string.error_message_regex_validation
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule,
            view,
            RegexRule(view, pattern, handledErrorMessage, errorEnabled)
        )
    }

    @BindingAdapter(
        value = ["app:validatePasswordRegex", "validateRegexMessage", "validateRegexAutoDismiss", "errorEnabledPassRegex"],
        requireAll = false
    )
    @JvmStatic
    fun bindingPasswordRegex(
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
            errorMessage, R.string.error_message_password_regex_validation
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule,
            view,
            RegexRule(view, pattern, handledErrorMessage, errorEnabled)
        )
    }
}