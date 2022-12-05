package com.zstronics.ceibro.base.validator.rule

import android.widget.TextView
import androidx.annotation.Keep
import com.zstronics.ceibro.base.validator.util.EditTextHandler.removeError
import com.zstronics.ceibro.base.validator.util.EditTextHandler.setError

/**
 * Created by Muhammad Ibraheem
 */
@Keep
class CpfTypeRule(
    view: TextView?,
    value: FieldType?,
    errorMessage: String?,
    errorEnabled: Boolean
) : TypeRule(view, value, errorMessage, errorEnabled) {
    override fun isValid(view: TextView?): Boolean {
        val rawCpf =
            view?.text.toString().trim { it <= ' ' }.replace("[^\\d]".toRegex(), "")
        return (rawCpf.length == 11 && !onBlackList(rawCpf)
                && (cpfDv(rawCpf, 1) == Character.getNumericValue(rawCpf[9])
                && cpfDv(rawCpf, 2) == Character.getNumericValue(rawCpf[10])))
    }

    /**
     * @param rawCpf raw CPF with length equal to 11.
     * @param step   1 or 2.
     * @return verification sum.
     */
    private fun cpfDv(rawCpf: String, step: Int): Int {
        val dv = 11 - cpfSum(rawCpf, step) % 11
        return if (dv == 10 || dv == 11) 0 else dv
    }

    private fun cpfSum(rawCPF: String, step: Int): Int {
        var sum = 0
        val count = 8 + step
        val baseMultiplier = 9 + step
        for (i in 0 until count) {
            sum += (baseMultiplier - i) * Character.getNumericValue(rawCPF[i])
        }
        return sum
    }

    // Reference: https://github.com/concretesolutions/canarinho/blob/master/canarinho/src/main/java/br/com/concretesolutions/canarinho/validator/ValidadorCPF.java
    private fun onBlackList(rawCpf: String): Boolean {
        var equal = true
        var i = 1
        while (i < 11 && equal) {
            if (rawCpf[i] != rawCpf[0]) {
                equal = false
            }
            i++
        }
        return equal || rawCpf == "12345678909"
    }

    override fun onValidationSucceeded(view: TextView?) {
        super.onValidationSucceeded(view)
        removeError(view)
    }

    override fun onValidationFailed(view: TextView?) {
        super.onValidationFailed(view)
        setError(view, errorMessage)
    }
}