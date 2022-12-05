package com.zstronics.ceibro.base.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.text.Spanned
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import java.util.*

/**
 * Checks if a string is a valid email
 * @return a boolean representing true if email is valid else false
 */
fun String.isEmail() = android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

/**
 * Checks if String is Number.
 * Checks against regex `^[0-9]+$`
 * @return a boolean representing true if all the characters are numeric else false
 */
fun String.isNumeric(): Boolean {
    val p = "^[0-9]+$".toRegex()
    return matches(p)
}

fun Context?.string(@StringRes idRes: Int): String = Resources.getSystem().getString(idRes)

fun isWhiteSpaces(@Nullable s: String?) = s != null && s.matches("\\s+".toRegex())

fun isEmpty(@Nullable text: String?) =
    text == null || TextUtils.isEmpty(text) || isWhiteSpaces(text) || text.equals(null)

fun isEmpty(@Nullable text: Any?) = text == null || isEmpty(text.toString())

fun isEmpty(@Nullable text: EditText?) = text == null || isEmpty(text.text.toString())

fun isEmpty(@Nullable text: TextView?) = text == null || isEmpty(text.text.toString())

fun isEmpty(@Nullable txt: TextInputLayout?) = txt == null || isEmpty(txt.editText)

fun toString(@NonNull editText: EditText) = editText.text.toString()

fun shortName(cardFullName: String): String {
    var cardFullName = cardFullName
    cardFullName = cardFullName.trim { it <= ' ' }
    var shortName = ""
    if (cardFullName.isNotEmpty() && cardFullName.contains(" ")) {
        val nameStr =
            cardFullName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val firstName = nameStr[0]
        val lastName = nameStr[nameStr.size - 1]
        shortName = firstName.substring(0, 1) + lastName.substring(0, 1)
        return shortName.uppercase()
    } else if (cardFullName.length > 0) {
        val nameStr =
            cardFullName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val firstName = nameStr[0]
        shortName = firstName.substring(0, 1)
        return shortName.uppercase()
    }
    return shortName.uppercase()
}

@SuppressLint("DefaultLocale")
fun String.toCamelCase(): String = split(" ").joinToString(" ") {
    it.lowercase(Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun String.getQRCode(): String {
    if ((this.contains("yap-app:"))) {
        return this.replace("yap-app:", "")
    }
    return this
}

fun String?.getValueWithoutComa(): String? {
    var string = this
    if (string?.contains(",") == true) {
        string = string.replace(",", "")
    }
    if (string?.contains(" ") == true) {
        string = string.substring(string.indexOf(" ") + 1, string.length)
    }
    return string
}

/**
 * Only removes the commas(,) and blank spaces( ) from given string
 */
fun String?.getValueWithoutComaAndSpace(): String? {
    var string = this
    if (string?.contains(",") == true) {
        string = string.replace(",", "")
    }
    if (string?.contains(" ") == true) {
        string = string.replace(" ", "")
    }
    return string
}


/**
 * Checks if a string contains numbers in increasing or decreasing sequence
 */
fun String.isSequenced(): Boolean {
    val sequenced = this.run {
        val first = if (length > 0) get(0).toString().toIntOrNull() else null
        first?.let {
            val low = first - (length - 1)
            val high = first + (length - 1)

            val lowSeq = (low..first).asReversedString()
            val highSeq = (first..high).asString()

            lowSeq == this || highSeq == this
        }
    }
    return sequenced ?: false
}


private fun IntRange.asString(): String = run {
    val s = StringBuilder()
    forEach { s.append(it.toString()) }
    s.toString()
}

private fun IntRange.asReversedString(): String = run {
    asString().reversed()
}

/**
 * Checks if a string contains all same chars like "0000"
 */
fun String.hasAllSameChars(): Boolean = this.run {
    this.matches("^([0-9])\\1*".toRegex())
}

fun String.maskIban(): String = this.run {
    val mask = "#### #### #### #### #### ###"
    val builder = StringBuilder()
    var j = 0
    for (i in mask.indices) {
        if (mask[i] == '#') {
            builder.append(this[j])
            j++
        } else
            builder.append(mask[i])
    }
    return builder.toString()
}

fun String?.toFormattedCurrency(
    showCurrency: Boolean = true,
    currency: String? = "AED",
    withComma: Boolean = true,
    selectedCurrencyDecimal: Int = 2,
): String {
    return try {
        if (this?.isNotBlank() == true) {
            val formattedAmount = getDecimalFormatUpTo(
                selectedCurrencyDecimal = selectedCurrencyDecimal,
                amount = this.parseToDouble().toString(),
                withComma = withComma
            )
            if (formattedAmount.isNotBlank()) {
                if (showCurrency)
                    "$currency $formattedAmount" else formattedAmount
            } else {
                ""
            }
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}

fun getDecimalFormatUpTo(
    selectedCurrencyDecimal: Int = 2,
    amount: String,
    withComma: Boolean = true
): String {
    return try {
        val amountInDouble = java.lang.Double.parseDouble(amount)
        return when (selectedCurrencyDecimal) {
            0 -> {
                if (withComma)
                    DecimalFormat("###,###,##0").format(amountInDouble)
                else
                    DecimalFormat("########0").format(amountInDouble)
            }
            1 -> {
                if (withComma)
                    DecimalFormat("###,###,##0.0").format(amountInDouble)
                else
                    DecimalFormat("########0.0").format(amountInDouble)
            }
            2 -> {
                if (withComma)
                    DecimalFormat("###,###,##0.00").format(amountInDouble)
                else
                    DecimalFormat("########0.00").format(amountInDouble)
            }
            3 -> {
                if (withComma)
                    DecimalFormat("###,###,##0.000").format(amountInDouble)
                else
                    DecimalFormat("########0.000").format(amountInDouble)
            }
            4 -> {
                if (withComma)
                    DecimalFormat("###,###,##0.0000").format(amountInDouble)
                else
                    DecimalFormat("########0.0000").format(amountInDouble)
            }
            6 -> {
                if (withComma)
                    DecimalFormat("###,###,##0.000000").format(amountInDouble)
                else
                    DecimalFormat("########0.000000").format(amountInDouble)
            }
            else -> {
                if (withComma)
                    DecimalFormat("###,###,##0.00").format(
                        amountInDouble
                    )
                else
                    DecimalFormat("########0.00").format(
                        amountInDouble
                    )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun String.removeSpecialChar() = Regex("[^A-Za-z0-9 ]").replace(this, "")
fun String?.toHtml(): Spanned? {
    if (this.isNullOrEmpty()) return null
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT)
}