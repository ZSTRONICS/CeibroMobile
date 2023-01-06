package com.zstronics.ceibro.utils

import android.text.format.DateUtils
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    const val DEFAULT_DATE_FORMAT: String = "dd/MM/yyyy"
    const val SIMPLE_DATE_FORMAT: String = "yyyy-MM-dd"
    val GMT: TimeZone = TimeZone.getTimeZone("GMT")
    val UTC: TimeZone = TimeZone.getTimeZone("UTC")
    val TIME_ZONE_Default: TimeZone = TimeZone.getDefault()

    //    const val FORMAT_LONG_OUTPUT = "dd MMM yy HH:mm:ss"//2015-11-28 10:17:18//2016-12-12 12:23:00
    const val FORMAT_LONG_INPUT = "dd MMM yy HH:mm:ss"//2015-11-28 10:17:18//2016-12-12 12:23:00
    const val FORMAT_LONG_INPUT_ERROR = "dd  yy HH:mm:ss"//02  21 13:35:48
    const val SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm"//2015-11-28 10:17:18
    const val SERVER_DATE_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"//2015-11-28 10:17:18
    const val LEAN_PLUM_EVENT_FORMAT = "yyyy-MM-dd HH:mm:ss"//2015-11-28 10:17:18
    const val FORMAT_MON_YEAR = "MMMM yyyy"
    const val FORMAT_DATE_MON_YEAR = "EEEE dd MMM "
    const val FORMAT_YEAR_MON_DATE = "yyyy-MM-dd"
    const val LEAN_PLUM_FORMAT = "dd MMMM, yyyy"
    const val FORMAT_TIME_24H = "HH:mm"
    const val FORMAT_TIME_12H = "hh:mm a"
    const val FXRATE_DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm a"//20/11/2020 10:17 AM
    const val FORMATE_MONTH_DAY = "MMM dd" // jan 1
    const val FORMATE_DATE_MONTH_YEAR = "dd MMM yyyy" // 12 Jan 2012
    const val FORMATE_DAY_MON_DATE = "EEEE, MMMM dd" // april 2021
    const val FORMAT_LONG_OUTPUT = "MMMM dd, yyyy・hh:mm a"//2015-11-28 10:17:18//2016-12-12 12:23:00
    const val FORMAT_SHORT_INPUT = "dd MM yy hh:mm:ss"

    //    const val FORMAT_TIME_24H_WITH_SECONDS = "HH:mm:ss"
    const val FORMAT_SHORT_DATE_MON_YEAR = "dd.MM.yyyy"
    fun getAge(date: Date): Int {
        val today = Calendar.getInstance()
        val dob = Calendar.getInstance()
        dob.time = date
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }

    fun getAge(day: Int, month: Int, year: Int): Int = getAge(toDate(day, month, year))

    fun isDatePassed(date: Date): Boolean = date.before(Date())

    private fun toDate(day: Int, month: Int, year: Int): Date {
        return if (year.toString().length == 2) {
            normaliseDate(day, month, year)
        } else {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.YEAR, year)
            cal.time
        }

    }

    private fun normaliseDate(day: Int, month: Int, year: Int): Date {
        val dd = if (day < 10) "0$day" else "" + day
        val mm = if (month < 10) "0$month" else "" + month
        val yy = if (year < 10) "0$year" else "" + year
        val format = SimpleDateFormat("dd-mm-yy", Locale.getDefault())
        return format.parse("$dd-$mm-$yy")
    }

    fun reformatStringDate(
        date: String?,
        inputFormatter: String? = SERVER_DATE_FULL_FORMAT,
        outFormatter: String? = DEFAULT_DATE_FORMAT
    ): String {
        var result = ""
        date?.let {
            val formatter = SimpleDateFormat(outFormatter, Locale.getDefault())
            try {
                // formatter.timeZone = TIME_ZONE_Default
                result = formatter.format(stringToDate(it, inputFormatter ?: ""))
            } catch (e: Exception) {
            }
        }
        return result

    }

    fun reformatDate(
        date: String?,
        inputFormatter: String = DEFAULT_DATE_FORMAT,
        outFormatter: String = DEFAULT_DATE_FORMAT,
        inputTimeZone: TimeZone = GMT,
        outTimeZone: TimeZone = TIME_ZONE_Default
    ): String {
        var result = ""
        date?.let {
            try {
                val formatter = SimpleDateFormat(outFormatter, Locale.getDefault())
                formatter.timeZone = outTimeZone
                result = formatter.format(
                    stringToDate(
                        dateStr = it,
                        format = inputFormatter,
                        timeZone = inputTimeZone
                    )!!
                )
            } catch (e: Exception) {
            }
        }
        return result
    }

    fun reformatLiveStringDate(
        date: String,
        inputFormatter: String? = DEFAULT_DATE_FORMAT,
        outFormatter: String? = DEFAULT_DATE_FORMAT
    ): String {
        var result = ""
        val formatter = SimpleDateFormat(outFormatter, Locale.getDefault())
        try {
            formatter.timeZone = TIME_ZONE_Default
            result = formatter.format(stringToDate(date, inputFormatter ?: ""))
        } catch (e: Exception) {
        }

        return result

    }

    fun dateToString(
        date: Date?,
        format: String = DEFAULT_DATE_FORMAT,
        isApplyTimeZone: Boolean = true
    ): String {
        return try {
            SimpleDateFormat(format, Locale.getDefault()).format(date)
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            if (isApplyTimeZone) sdf.timeZone = TimeZone.getTimeZone("UTC")

            return sdf.format(date)
        } catch (e: Exception) {
            " "
        }
    }

    fun dateToString(date: Date?, format: String, timeZone: TimeZone = TIME_ZONE_Default): String {
        date?.let {
            var result = ""
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            formatter.timeZone = timeZone
//            val symbols = DateFormatSymbols(Locale.getDefault())
//            symbols.amPmStrings = arrayOf("am", "pm")
//            formatter.dateFormatSymbols = symbols
            try {
                result = formatter.format(it)
            } catch (e: Exception) {
            }

            return result
        } ?: return ""

    }

    fun stringToDate(dateStr: String, format: String): Date? {
        var d: Date? = null
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.timeZone = UTC
        try {
            formatter.isLenient = false
            d = formatter.parse(dateStr)
            formatter.timeZone = TIME_ZONE_Default
            val newDate = SimpleDateFormat(format, Locale.getDefault()).format(d)
            d = formatter.parse(newDate)
        } catch (e: Exception) {
            d = null
        }
        return d
    }

    fun stringDateToMillis(dateStr: String, format: String): Long? {
        var d: Date? = null
        var timeInMilliseconds: Long? = null
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.timeZone = UTC
        try {
            formatter.isLenient = false
            d = formatter.parse(dateStr)
            timeInMilliseconds = d.getTime()

        } catch (e: Exception) {
            timeInMilliseconds = null
        }
        return timeInMilliseconds
    }

    fun getCurrentTimeStamp(): String {
        val sdf = SimpleDateFormat(SERVER_DATE_FULL_FORMAT, Locale.getDefault())
        sdf.timeZone = UTC
        return sdf.format(Date())
    }
    private fun getCurrentTimeInMillis(): Long {
        return System.currentTimeMillis()
    }

    fun getStringTimeSpan(oldDateString: String, format: String): CharSequence? {
        val msgTimeInMillis = stringDateToMillis(oldDateString, format)
        val currentTime = getCurrentTimeInMillis()
        val time =
            msgTimeInMillis?.let { DateUtils.getRelativeTimeSpanString(it, currentTime, android.text.format.DateUtils.MINUTE_IN_MILLIS) }

        return time
    }

    fun stringToDate(dateStr: String, format: String?, timeZone: TimeZone = GMT): Date? {
        var d: Date? = null
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.timeZone = timeZone
        try {
            formatter.isLenient = false
            d = formatter.parse(dateStr)
        } catch (e: Exception) {
            d = null
        }
        return d
    }

    fun reformatLocalDate(
        date: String,
        inputFormatter: String? = DEFAULT_DATE_FORMAT,
        outputFormatter: String? = DEFAULT_DATE_FORMAT
    ): Date? {
        return try {
            val inFormatter = SimpleDateFormat(inputFormatter, Locale.getDefault())
            val outFormatter = SimpleDateFormat(outputFormatter, Locale.getDefault())
            val newDate = outFormatter.format(inFormatter.parse(date))
            outFormatter.parse(newDate)
        } catch (e: Exception) {
            null
        }
    }

    fun reformatToLocalString(
        date: Date?,
        outputFormatter: String
    ): String {
        return try {
            SimpleDateFormat(outputFormatter, Locale.getDefault()).format(date)
            val outFormatter = SimpleDateFormat(outputFormatter, Locale.getDefault())
            outFormatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun stringToDateLeanPlum(dateStr: String): Date? {
        var d: Date? = null
        val formatter = SimpleDateFormat(LEAN_PLUM_FORMAT, Locale.getDefault())
        formatter.timeZone = GMT
        try {
            formatter.isLenient = false
            d = formatter.parse(dateStr)
        } catch (e: Exception) {
            d = null
        }

        return d
    }

    fun convertTopUpDate(creationDate: String?): String? {
        return try {
            val parser = SimpleDateFormat("MMyy")
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val convertedDate = parser.parse(creationDate)
            val pattern = "MM/yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern)
            simpleDateFormat.format(convertedDate)
        } catch (ex: Exception) {
            ""
        }

    }

    fun isDatePassed(creationDate: String, parser: SimpleDateFormat): Boolean {
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val convertedDate = parser.parse(creationDate)
        return isDatePassed(convertedDate)
    }

    fun getCurrentDateWithFormat(formal: String): String {
        val sdf = SimpleDateFormat(formal, Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date())
    }

    fun getCurrentUTCDateTime(format: String = LEAN_PLUM_EVENT_FORMAT): String {
        SimpleDateFormat(format, Locale.getDefault()).apply {
            timeZone = UTC
            return format(Date())
        }
    }

    fun getCurrentDateWithFormat(formal: String, timeZone: TimeZone): String {
        val sdf = SimpleDateFormat(formal, Locale.getDefault())
        sdf.timeZone = timeZone
        return sdf.format(Date())
    }

    fun convertServerDateToLocalDate(serverDate: String): Date? {
        return try {
            val serverSdf = SimpleDateFormat(SERVER_DATE_FORMAT, Locale.getDefault())
            serverSdf.timeZone = TimeZone.getTimeZone("UTC")
            val serverDate = serverSdf.parse(serverDate)

            val localSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            localSdf.timeZone = TimeZone.getDefault()
            val localDate = localSdf.format(serverDate)

            val convertedSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            convertedSdf.parse(localDate)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getStartAndEndOfMonthAndDay(
        currentDate: Date,
        format: String = FORMATE_MONTH_DAY
    ): String {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH))
        val startDay = dateToString(calendar.time, format, false)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDay = dateToString(calendar.time, format, false)
        return "${startDay.replace("0", "")} - $endDay"
    }

    fun geMonthsBetweenTwoDates(startDate: String, endDate: String): List<Date> {
        val dates = ArrayList<Date>()
        val df1: DateFormat = SimpleDateFormat("yyyy-MM")
        var parsedStartDate: Date? = null
        var parsedEndDate: Date? = null
        try {
            parsedStartDate = df1.parse(startDate)
            parsedEndDate = df1.parse(endDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val cal1 = Calendar.getInstance()
        cal1.time = parsedStartDate


        val cal2 = Calendar.getInstance()
        cal2.time = parsedEndDate

        while (!cal1.after(cal2)) {
            dates.add(cal1.time)
            cal1.add(Calendar.MONTH, 1)
        }
        return dates
    }

    fun getPriviousMonthFromCurrentDate(listOfMonths: List<Date>, currentDate: Date?): Date? {
        var index: Int = -1
        currentDate?.let {
            for (i in listOfMonths.indices) {
                if (isDateMatched(listOfMonths[i], currentDate)) {
                    index = i
                    break
                }
            }
        }

        return if (index != -1) {
            if (index - 1 >= 0 && listOfMonths.size > (index - 1)) listOfMonths[index - 1] else null
        } else {
            null
        }
    }

    fun getNextMonthFromCurrentDate(listOfMonths: List<Date>, currentDate: Date?): Date? {
        var index: Int = -1
        currentDate?.let {
            for (i in 0..listOfMonths.size) {
                if (isDateMatched(listOfMonths[i], currentDate)) {
                    index = i
                    break
                }
            }
        }

        return if (index != -1) {
            if (listOfMonths.size > (index + 1)) listOfMonths[index + 1] else null
        } else {
            null
        }
    }

    fun isDateMatched(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.time = date1
        val calendar2 = Calendar.getInstance()
        calendar2.time = date2
        val sameYear = calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR]
        val sameMonth =
            calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH]
        return sameMonth && sameYear
    }


    fun isToday(date: Date?) = date?.let { android.text.format.DateUtils.isToday(it.time) } ?: false

    fun isToday(date: String, format: String, timeZone: TimeZone) =
        android.text.format.DateUtils.isToday(
            stringToDate(date, format, timeZone)?.time ?: Date().time
        )

    fun isTomorrow(date: Date?): Boolean {
        // Check if yesterday
        val c1 = Calendar.getInstance() // today
        c1.add(Calendar.DAY_OF_YEAR, 1) // yesterday
        val c2 = Calendar.getInstance()
        c2.time = date ?: Date()
        return (c1[Calendar.YEAR] == c2[Calendar.YEAR]
                && c1[Calendar.DAY_OF_YEAR] == c2[Calendar.DAY_OF_YEAR])
    }

    fun isTomorrow(date: Date?, timeZone: TimeZone) =
        afterDay(timeZone) == dateToString(date, DEFAULT_DATE_FORMAT, timeZone)

    fun isYesterday(date: Date?): Boolean {
        // Check if yesterday
        val c1 = Calendar.getInstance() // today
        c1.add(Calendar.DAY_OF_YEAR, -1) // yesterday
        val c2 = Calendar.getInstance()
        c2.time = date ?: Date()
        return (c1[Calendar.YEAR] == c2[Calendar.YEAR]
                && c1[Calendar.DAY_OF_YEAR] == c2[Calendar.DAY_OF_YEAR])
    }

    fun isYesterday(date: Date?, timeZone: TimeZone) =
        befoDay(timeZone) == dateToString(date, DEFAULT_DATE_FORMAT, timeZone)

    fun isYesterday(date: String, format: String, timeZone: TimeZone) =
        stringToDate(date, format, timeZone)?.let {
            isYesterday(it)
        } ?: false

    fun afterDay(timeZone: TimeZone) = dateToString(
        nextDay(Date(), 1),
        DEFAULT_DATE_FORMAT, timeZone
    )

    fun befoDay(timeZone: TimeZone) = befoDay(DEFAULT_DATE_FORMAT, timeZone)

    fun nextDay(date: Date?, day: Int): Date? {
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal.add(Calendar.DAY_OF_YEAR, day)
        return cal.time
    }

    fun befoDay(format: String, timeZone: TimeZone): String? {
        return dateToString(
            nextDay(
                Date(),
                -1
            ), format, timeZone
        )
    }

    fun isCurrentTimeNotBetween(fromDate: Date?, toDate: Date?): Boolean {
        val now = Calendar.getInstance().time
        if (fromDate != null && toDate != null)
            return !now.after(fromDate) && now.before(toDate)
        return false
    }


    // TODO remove this method
    @Deprecated("Use DateUtils class method dateToSting method")
    fun convertLongToDate(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat(DEFAULT_DATE_FORMAT)
        return format.format(date).toString()
    }

    fun dateToMonthAndYear(date: Date?): CharSequence =
        android.text.format.DateFormat.format(FORMAT_MON_YEAR, date)
}