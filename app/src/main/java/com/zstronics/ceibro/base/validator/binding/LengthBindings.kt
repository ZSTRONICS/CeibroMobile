package com.zstronics.ceibro.base.validator.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.validator.rule.EmptyRule
import com.zstronics.ceibro.base.validator.rule.MaxLengthRule
import com.zstronics.ceibro.base.validator.rule.MinLengthRule
import com.zstronics.ceibro.base.validator.util.EditTextHandler
import com.zstronics.ceibro.base.validator.util.ErrorMessageHelper
import com.zstronics.ceibro.base.validator.util.ViewTagHelper

/**
 * Created by Muhammad Ibraheem
 */
object LengthBindings {
    @BindingAdapter(
        value = ["app:validateMinLength", "validateMinLengthMessage", "validateMinLengthAutoDismiss", "errorEnabledMinLength"],
        requireAll = false
    )
    @JvmStatic
    fun bindingMinLength(
        view: TextView?,
        minLength: Int,
        errorMessage: String?,
        autoDismiss: Boolean,
        enableError: Boolean
    ) {
        if (autoDismiss) {
            EditTextHandler.disableErrorOnChanged(view)
        }
        val handledErrorMessage = ErrorMessageHelper.getStringOrDefault(
            view,
            errorMessage, R.string.default_required_length_message_min, minLength
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule,
            view,
            MinLengthRule(view, minLength, handledErrorMessage, enableError)
        )
    }

    @BindingAdapter(
        value = ["app:validateMaxLength", "validateMaxLengthMessage", "validateMaxLengthAutoDismiss", "errorEnabledMaxLength"],
        requireAll = false
    )
    @JvmStatic
    fun bindingMaxLength(
        view: TextView?,
        maxLength: Int,
        errorMessage: String?,
        autoDismiss: Boolean,
        enableError: Boolean
    ) {
        if (autoDismiss) {
            EditTextHandler.disableErrorOnChanged(view)
        }
        val handledErrorMessage = ErrorMessageHelper.getStringOrDefault(
            view,
            errorMessage, R.string.default_required_length_message_max, maxLength
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule,
            view,
            MaxLengthRule(view, maxLength, handledErrorMessage, enableError)
        )
    }

    @BindingAdapter(
        value = ["app:validateEmpty", "validateEmptyMessage", "validateEmptyAutoDismiss", "errorEnabledEmpty"],
        requireAll = false
    )
    @JvmStatic
    fun bindingEmpty(
        view: TextView?,
        empty: Boolean,
        errorMessage: String?,
        autoDismiss: Boolean,
        enableError: Boolean
    ) {
        if (autoDismiss) {
            EditTextHandler.disableErrorOnChanged(view)
        }
        val handledErrorMessage = ErrorMessageHelper.getStringOrDefault(
            view,
            errorMessage, R.string.error_message_empty_validation
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule,
            view,
            EmptyRule(view, empty, handledErrorMessage, enableError)
        )
    }
}