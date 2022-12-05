package com.zstronics.ceibro.base.validator

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Keep
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.validator.rule.Rule
import com.zstronics.ceibro.base.validator.util.ViewTagHelper.filterViewWithTag
import com.zstronics.ceibro.base.validator.util.ViewTagHelper.filterViewsWithTag
import com.zstronics.ceibro.base.validator.util.ViewTagHelper.getViewsByTag
import java.util.*

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class Validator(val target: ViewDataBinding?) {
    private val disabledViews: MutableSet<View>
    private var validationListener: ValidationListener? = null
    private var mode =
        FORM_VALIDATION_MODE
    private var msgValidationMode =
        VALIDATION_WITHOUT_ERROR_MESSAGES

    var targetViewBinding: ViewDataBinding? = target
        set(value) {
            field = value
            value?.let {
                if (msgValidationMode == VALIDATION_WITHOUT_ERROR_MESSAGES) applyTextWatcherOnAllViews()
            }
        }

    @JvmField
    var isValidate =
        MutableLiveData(false)
    private val textChangedListener: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            if (msgValidationMode == VALIDATION_WITHOUT_ERROR_MESSAGES) isValidate.value =
                validate()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private fun applyTextWatcherOnAllViews() {
        val viewWithValidations =
            getViewsWithValidation()
        for (view in viewWithValidations) {
            if (view is TextView) {
                view.addTextChangedListener(textChangedListener)
            }
        }
    }

    private fun removeTextWatcherOnAllViews() {
        val viewWithValidations =
            getViewsWithValidation()
        for (view in viewWithValidations) {
            if (view is TextView) {
                view.removeTextChangedListener(textChangedListener)
            }
        }
    }

    fun setValidationListener(validationListener: ValidationListener?) {
        this.validationListener = validationListener
    }

    fun toValidate() {
        requireNotNull(validationListener) { "Validation listener should not be null." }
        if (validate()) {
            validationListener?.onValidationSuccess(this)
        } else {
            validationListener?.onValidationError(this)
        }
    }

    fun validate(): Boolean {
        val viewWithValidations =
            getViewsWithValidation()
        return isAllViewsValid(viewWithValidations)
    }

    fun validate(view: View): Boolean {
        val viewWithValidations =
            getViewsWithValidation(view)
        return isAllViewsValid(viewWithValidations)
    }

    fun <ViewType : View?> validate(views: List<ViewType>): Boolean {
        val viewWithValidations =
            getViewsWithValidation(views)
        return isAllViewsValid(viewWithValidations)
    }

    private fun isAllViewsValid(viewWithValidations: List<View>): Boolean {
        var allViewsValid = true
        for (viewWithValidation in viewWithValidations) {
            var viewValid = true
            val rules: List<Rule<*, *>> =
                viewWithValidation.getTag(R.id.validator_rule) as List<Rule<*, *>>
            for (rule in rules) {
                viewValid = viewValid && isRuleValid(rule)
                allViewsValid = allViewsValid && viewValid
            }
            if (mode == FIELD_VALIDATION_MODE && !viewValid) {
                break
            }
        }
        return allViewsValid
    }

    private fun isRuleValid(rule: Rule<*, *>): Boolean {
        return disabledViews.contains(rule.view) || rule.validate()
    }

    fun disableValidation(view: View) {
        disabledViews.add(view)
    }

    fun enableValidation(view: View) {
        disabledViews.remove(view)
    }

    fun enableFormValidationMode() {
        mode = FORM_VALIDATION_MODE
    }

    fun enableWithoutErrorMessageValidation() {
        if (msgValidationMode == VALIDATION_WITH_ERROR_MESSAGES) {
            applyTextWatcherOnAllViews()
            msgValidationMode =
                VALIDATION_WITHOUT_ERROR_MESSAGES
        }
    }

    fun enableWithErrorMessageValidation() {
        if (msgValidationMode == VALIDATION_WITHOUT_ERROR_MESSAGES) {
            removeTextWatcherOnAllViews()
            msgValidationMode =
                VALIDATION_WITH_ERROR_MESSAGES
        }
    }

    fun enableFieldValidationMode() {
        mode = FIELD_VALIDATION_MODE
    }

    private fun getViewsWithValidation(): List<View> {
        targetViewBinding?.let {
            return if (it.root is ViewGroup)
                getViewsByTag((it.root as ViewGroup), R.id.validator_rule)
            else
                listOf(it.root)
        }
        return emptyList()
//        if (targetViewBinding == null) return ArrayList()
//        if (targetViewBinding?.root is ViewGroup) {
//            return getViewsByTag(
//                (targetViewBinding?.root as ViewGroup),
//                R.id.validator_rule
//            )
//        }
//        return listOf(targetViewBinding?.root)
    }

    private fun <ViewType : View?> getViewsWithValidation(views: List<ViewType>): List<View> {
        return filterViewsWithTag(R.id.validator_rule, views)
    }

    private fun getViewsWithValidation(view: View): List<View> {
        return filterViewWithTag(R.id.validator_rule, view)
    }

    interface ValidationListener {
        fun onValidationSuccess(validator: Validator) {
            validator.isValidate.value = true
        }

        fun onValidationError(validator: Validator) {
            validator.isValidate.value = false
        }
    }

    companion object {
        private const val FIELD_VALIDATION_MODE = 0
        private const val FORM_VALIDATION_MODE = 1
        private const val VALIDATION_WITHOUT_ERROR_MESSAGES = 10
        private const val VALIDATION_WITH_ERROR_MESSAGES = 20
    }

    init {
        disabledViews = HashSet()

    }
}