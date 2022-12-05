package com.zstronics.ceibro.base.extensions

fun String?.parseToDouble() = try {
    this?.getValueWithoutComa()?.toDouble() ?: 0.0
} catch (e: NumberFormatException) {
    0.0
}
fun String?.parseToDoubleOrNull() = try {
    this?.getValueWithoutComa()?.toDoubleOrNull()
} catch (e: NumberFormatException) {
    null
}

fun String.parseToInt() = try {
    toInt()
} catch (e: NumberFormatException) {
    0
}

fun String.parseToLong() = try {
    toLong()
} catch (e: NumberFormatException) {
    0L
}

fun Double.parseToInt() = try {
    toInt()
} catch (e: NumberFormatException) {
    0
}

fun String.parseToFloat() = try {
    toFloat()
} catch (e: NumberFormatException) {
    0.0F
}

fun CharSequence?.parseToDouble() = try {
    this?.toString()?.replace(",", "")?.toDouble() ?: 0.0
} catch (e: NumberFormatException) {
    0.0
}

fun Boolean.convertToInt() = if (this) 1 else 0

fun Double?.isValueWithinRange(min: Double?, max: Double?) =
    this.isLessThan(min) || this.isGreaterThan(max)
//    this < (minLimit
//        ?: 0.0) && this > (maxLimit ?: 999999.00)

fun Double?.isGreaterThan(other: Double?) =
    if(other==null) true else this != null && other != null && this > other

fun Double?.isLessThan(other: Double?) =
    this != null && other != null && this < other