package com.zstronics.ceibro.base.validator.binding

import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.validator.rule.TypeRule
import com.zstronics.ceibro.base.validator.util.EditTextHandler
import com.zstronics.ceibro.base.validator.util.ErrorMessageHelper
import com.zstronics.ceibro.base.validator.util.ViewTagHelper

/**
 * Created by Muhammad Ibraheem
 */

object TypeBindings {
    @JvmStatic
    @BindingAdapter(
        value = ["app:validateType", "validateTypeMessage", "validateTypeAutoDismiss", "errorEnabledType"],
        requireAll = false
    )
    fun bindingTypeValidation(
        view: TextView?,
        fieldTypeText: String,
        errorMessage: String?,
        autoDismiss: Boolean,
        enableError: Boolean
    ) {
        if (autoDismiss) {
            EditTextHandler.disableErrorOnChanged(view)
        }
        try {

            val fieldType =
                getFieldTypeByText(fieldTypeText)

            val handledErrorMessage = ErrorMessageHelper.getStringOrDefault(
                view,
                errorMessage, fieldType.errorMessageId
            )
            ViewTagHelper.appendValue(
                R.id.validator_rule,
                view,
                fieldType.instantiate(view, handledErrorMessage, enableError)
            )
        } catch (ignored: Exception) {
            Toast.makeText(view?.context, ignored.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun getFieldTypeByText(fieldTypeText: String): TypeRule.FieldType {
        var fieldType =
            TypeRule.FieldType.None
        for (type in TypeRule.FieldType.values()) {
            if (type.toString().equals(fieldTypeText, ignoreCase = true)) {
                fieldType = type
                break
            }
        }
        return fieldType
    }
}