package com.zstronics.ceibro.base.validator.util

import android.view.View
import androidx.annotation.StringRes

/**
 * Created by Muhammad Ibraheem
 */
object ErrorMessageHelper {
    fun getStringOrDefault(
        view: View?, errorMessage: String?,
        @StringRes defaultMessage: Int
    ): String {
        return errorMessage ?: view?.context?.getString(defaultMessage)!!
    }

    fun getStringOrDefault(
        view: View?, errorMessage: String?,
        @StringRes defaultMessage: Int, value: Int
    ): String {
        return errorMessage ?: view?.context?.getString(defaultMessage, value)!!
    }

    fun getStringOrDefault(
        view: View?, errorMessage: CharSequence?,
        @StringRes defaultMessage: Int
    ): String {
        return errorMessage?.toString() ?: view?.context?.getString(defaultMessage)!!
    }

    fun getStringOrDefault(
        view: View?, errorMessage: CharSequence?,
        @StringRes defaultMessage: Int, value: Int
    ): String {
        return errorMessage?.toString() ?: view?.context?.getString(defaultMessage, value)!!
    }
}