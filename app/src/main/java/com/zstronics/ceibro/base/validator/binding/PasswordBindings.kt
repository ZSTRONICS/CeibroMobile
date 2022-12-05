package com.zstronics.ceibro.base.validator.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.validator.rule.ConfirmMobileNoRule
import com.zstronics.ceibro.base.validator.util.EditTextHandler
import com.zstronics.ceibro.base.validator.util.ErrorMessageHelper
import com.zstronics.ceibro.base.validator.util.ViewTagHelper

/**
 * Created by Muhammad Ibraheem
 */
object PasswordBindings {
    @BindingAdapter(
        value = ["app:validatePassword", "validatePasswordMessage", "validatePasswordAutoDismiss", "errorEnabledPassword"],
        requireAll = false
    )
    @JvmStatic
    fun bindingPassword(
        view: TextView?,
        comparableView: TextView?,
        errorMessage: String?,
        autoDismiss: Boolean,
        errorEnabled: Boolean
    ) {
        if (autoDismiss) {
            EditTextHandler.disableErrorOnChanged(view)
        }
        val handledErrorMessage = ErrorMessageHelper.getStringOrDefault(
            view,
            errorMessage, R.string.error_message_not_equal_password
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule, view,
            ConfirmMobileNoRule(view, comparableView, handledErrorMessage, errorEnabled)
        )
    }

    @BindingAdapter(
        value = ["validateIban", "validateIbanMessage", "validateIbanAutoDismiss"],
        requireAll = false
    )
    @JvmStatic
    fun bindingIban(
        view: TextInputEditText?,
        comparableView: TextInputEditText?,
        errorMessage: String?,
        autoDismiss: Boolean
    ) {
        if (autoDismiss) {
            EditTextHandler.disableErrorOnChanged(view)
        }
        val handledErrorMessage = ErrorMessageHelper.getStringOrDefault(
            view,
            errorMessage, R.string.error_message_not_equal_phone
        )
        ViewTagHelper.appendValue(
            R.id.validator_rule, view,
            ConfirmMobileNoRule(view, comparableView, handledErrorMessage, false)
        )
    }
}