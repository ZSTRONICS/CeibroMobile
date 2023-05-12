package com.zstronics.ceibro.utils

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.TimeZone
import android.os.Build
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextUtils
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.util.*


fun getNationalNumber(editable: Editable, code: Int, codeString: String): String? {

    val phoneUtil = PhoneNumberUtil.getInstance()
    val tempNo = editable.toString().trim()
    var number: String? = null
    try {
        val ph = phoneUtil.parse(tempNo, codeString)
        ph.countryCode = code
        number = "+" + ph.countryCode + "" + ph.nationalNumber
        return number
    } catch (e: NumberParseException) {
    }

    return number
}

fun getNationalNoFormInterNationalNo(number: String): String? {
    val validatedNumber = if (number.startsWith("+")) number else "+$number"
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val phoneNumber = try {
        phoneNumberUtil.parse(validatedNumber, null)
    } catch (e: NumberParseException) {
        null
    } ?: return null

    return phoneNumber.nationalNumber.toString()
}

fun getCodeFromPhone(pNumber: String): Int {
    val phoneUtil = PhoneNumberUtil.getInstance()
    try {
        // phone must begin with '+'
        val numberProto = phoneUtil.parse(pNumber, "PK")
        val pp = phoneUtil.getNationalSignificantNumber(numberProto)
        return numberProto.countryCode
    } catch (e: NumberParseException) {
        System.err.println("NumberParseException was thrown: $e")
    }

    return 0
}

fun getInterNationalNumber(editable: Editable, codeString: String): String? {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val tempNo = editable.toString().trim()
    var number: String? = null
    try {
        val ph = phoneUtil.parse(tempNo, codeString)
        ph.countryCode = ph.countryCode
        number = phoneUtil.format(ph, PhoneNumberUtil.PhoneNumberFormat.E164)
        //number = "+" + ph.getCountryCode() + "" + ph.getNationalNumber();
        return number
    } catch (e: NumberParseException) {
    }
    return number
}

fun getInterNationalNumber(unFormattedNumber: String, codeString: String): String? {
    val phoneUtil = PhoneNumberUtil.getInstance()
    var number: String? = null
    try {
        val ph = phoneUtil.parse(unFormattedNumber, codeString)
        ph.countryCode = ph.countryCode
        number = phoneUtil.format(ph, PhoneNumberUtil.PhoneNumberFormat.E164)
        //number = "+" + ph.getCountryCode() + "" + ph.getNationalNumber();
        return number
    } catch (e: NumberParseException) {
    }
    return number
}

fun getNationalNumber(text: String, code: Int, codeString: String): String? {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val tempNo = text.trim { it <= ' ' }
    var number: String? = null
    try {
        val ph = phoneUtil.parse(tempNo, codeString)
        ph.countryCode = code

        number = "+" + ph.countryCode + "" + ph.nationalNumber
        return number
    } catch (e: NumberParseException) {


    }

    return number
}

fun getNationalNumber(number: String): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    try {
        val ph = phoneUtil.parse(number, "")
        ph.nationalNumber
        return "" + ph.nationalNumber
    } catch (e: NumberParseException) {
    }
    return " "
}

fun isValidPhoneNumber(no: Editable, code: String): Boolean {
    if (TextUtils.isEmpty(no)) {
        return false
    }
    val number = no.toString().trim()
    return isValidPhoneNumber(number, code)
}

fun isValidPhoneNumber(number: String, code: String): Boolean {
    if (TextUtils.isEmpty(number)) {
        return false
    }
    var isValid = false
    val phoneUtil = PhoneNumberUtil.getInstance()
    var isMobile: PhoneNumberUtil.PhoneNumberType? = null
    return try {
        var rawNumber = number
        if (number.startsWith("00", false)) {
            rawNumber = number.replaceFirst("00", "+")
        }
        val ph = phoneUtil.parseAndKeepRawInput(rawNumber, code)
        ph.countryCode = phoneUtil.getCountryCodeForRegion(code)
        val isPossible = phoneUtil.isPossibleNumber(ph)
        val hasDefaultCountry = code.isNotEmpty() && code != "ZZ"
        if (hasDefaultCountry) {
            isValid = phoneUtil.isValidNumberForRegion(ph, code)
        }
        isMobile = phoneUtil.getNumberType(ph)
        isValid && (PhoneNumberUtil.PhoneNumberType.MOBILE === isMobile)
    } catch (e: NumberParseException) {
        isValid
    }

}

fun parseContact(contact: String, code: String): String? {
    var phoneNumber: Phonenumber.PhoneNumber? = null
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    var finalNumber: String? = null
    // String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countrycode));
    var isValid = false
    var isMobile: PhoneNumberUtil.PhoneNumberType? = null
    try {
        phoneNumber = phoneNumberUtil.parse(contact, code)
        isValid = phoneNumberUtil.isValidNumber(phoneNumber)
        isMobile = phoneNumberUtil.getNumberType(phoneNumber)

    } catch (e: NumberParseException) {
        e.printStackTrace()
    } catch (e: NullPointerException) {
        e.printStackTrace()
    }


    if (isValid && (PhoneNumberUtil.PhoneNumberType.MOBILE === isMobile || PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE === isMobile)) {
        finalNumber = phoneNumberUtil.format(
            phoneNumber,
            PhoneNumberUtil.PhoneNumberFormat.E164
        ).substring(1)
    }
    return finalNumber
}

fun getCountryCodeForRegion(code: String): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val pn = phoneUtil.getCountryCodeForRegion(code.uppercase())
    return "+$pn"
}

fun getCountryIsoCode(number: String): String? {
    val validatedNumber = if (number.startsWith("+")) number else "+$number"
    val phoneNumberUtil = PhoneNumberUtil.getInstance()
    val phoneNumber = try {
        phoneNumberUtil.parse(validatedNumber, null)
    } catch (e: NumberParseException) {
        null
    } ?: return null

    return phoneNumberUtil.getRegionCodeForCountryCode(phoneNumber.countryCode)
}

fun getCountryCodeForRegionSimple(code: String) =
    PhoneNumberUtil.getInstance().getCountryCodeForRegion(code.uppercase())


fun getCountryCodeForRegion(code: Int): String {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return phoneUtil.getRegionCodeForCountryCode(code)

}

fun getDefaultCountryCode(context: Context): String {
    val countryCode = getCountryCodeFromTimeZone(context)
    return if (countryCode == "") "AE" else countryCode
}

private fun getCountryCodeFromTimeZone(context: Context): String {
    val curTimeZoneId = Calendar.getInstance().timeZone.id
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        TimeZone.getRegion(curTimeZoneId)
    } else {
        getCountryCodeFromTelephony(context)
    }
}

@SuppressLint("DefaultLocale")
fun getCountryCodeFromTelephony(context: Context): String {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return tm.networkCountryIso.uppercase(Locale.getDefault())
}

fun getFormattedPhoneNumber(context: Context, mobileNoWithCountryCode: String): String {
    return try {
        val pnu = PhoneNumberUtil.getInstance()
        val pn = pnu.parse(mobileNoWithCountryCode, getDefaultCountryCode(context))
        return pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

}

fun getFormattedPhoneNo(context: Context, mobileNumber: String): String {
    return when {
        mobileNumber.startsWith("00") ->
            getFormattedPhoneNumber(
                context,
                mobileNumber.replaceRange(
                    0,
                    2,
                    "+"
                )
            )
        mobileNumber.startsWith("+") -> getFormattedPhoneNumber(context, mobileNumber)
        else -> formatPhoneWithPlus(mobileNumber)
    }
}

fun formatPhoneWithPlus(phoneNumber: String): String {
    if (phoneNumber.startsWith("00")) {
        return replaceCountryCodePrefix(phoneNumber, 0, 2)
    } else {
        return replaceCountryCodePrefix(phoneNumber, 0, 0)
    }

}

fun replaceCountryCodePrefix(mobileNumber: String, startRange: Int, endRange: Int): String {
    return mobileNumber.replaceRange(
        startRange,
        endRange,
        "+"
    )
}
