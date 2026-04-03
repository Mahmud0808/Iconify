package com.drdisagree.iconify.xposed.modules.extras.utils

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.widget.TextClock
import android.widget.TextView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


object TimeUtils {

    private val numbers = arrayOf(
        "Zero",
        "One",
        "Two",
        "Three",
        "Four",
        "Five",
        "Six",
        "Seven",
        "Eight",
        "Nine",
        "Ten",
        "Eleven",
        "Twelve",
        "Thirteen",
        "Fourteen",
        "Fifteen",
        "Sixteen",
        "Seventeen",
        "Eighteen",
        "Nineteen",
        "Twenty",
        "Twenty One",
        "Twenty Two",
        "Twenty Three",
        "Twenty Four",
        "Twenty Five",
        "Twenty Six",
        "Twenty Seven",
        "Twenty Eight",
        "Twenty Nine",
        "Thirty",
        "Thirty One",
        "Thirty Two",
        "Thirty Three",
        "Thirty Four",
        "Thirty Five",
        "Thirty Six",
        "Thirty Seven",
        "Thirty Eight",
        "Thirty Nine",
        "Forty",
        "Forty One",
        "Forty Two",
        "Forty Three",
        "Forty Four",
        "Forty Five",
        "Forty Six",
        "Forty Seven",
        "Forty Eight",
        "Forty Nine",
        "Fifty",
        "Fifty One",
        "Fifty Two",
        "Fifty Three",
        "Fifty Four",
        "Fifty Five",
        "Fifty Six",
        "Fifty Seven",
        "Fifty Eight",
        "Fifty Nine",
        "Sixty"
    )

    private fun convertNumberToText(number: String): String {
        return try {
            numbers[number.toInt()]
        } catch (throwable: Throwable) {
            number
        }
    }

    fun regionFormattedDate(usFormat: String?, euFormat: String?): String {
        try {
            val currentDate = Date()
            val currentLocale = Locale.getDefault()
            return if (currentLocale == Locale.US) {
                val usDateFormat = SimpleDateFormat(usFormat, Locale.US)
                usDateFormat.format(currentDate)
            } else {
                val euDateFormat = SimpleDateFormat(euFormat, currentLocale)
                euDateFormat.format(currentDate)
            }
        } catch (throwable: Throwable) {
            log(this@TimeUtils, throwable)
        }
        return SimpleDateFormat(usFormat, Locale.getDefault()).format(Date())
    }

    fun formatTime(context: Context, format24H: String, format12H: String): String {
        return formatTime(if (DateFormat.is24HourFormat(context)) format24H else format12H)
    }

    private fun formatTime(format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date())
    }

    fun setCurrentTimeTextClock(
        context: Context,
        tickIndicator: TextClock,
        hourView: TextView,
        minuteView: TextView
    ) {
        setCurrentTimeHour(context, hourView)
        setCurrentTimeMinute(minuteView)

        tickIndicator.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    setCurrentTimeHour(context, hourView)
                    setCurrentTimeMinute(minuteView)
                }
            }
        })
    }

    private fun setCurrentTimeHour(context: Context, hourView: TextView) {
        val hourFormat = if (DateFormat.is24HourFormat(context)) "HH" else "hh"
        val hour = SimpleDateFormat(
            hourFormat,
            Locale.getDefault()
        ).format(Calendar.getInstance().time)
        hourView.text = convertNumberToText(hour)
    }

    private fun setCurrentTimeMinute(minuteView: TextView) {
        val minuteFormat = "mm"
        val minute = SimpleDateFormat(
            minuteFormat,
            Locale.getDefault()
        ).format(Calendar.getInstance().time)
        minuteView.text = convertNumberToText(minute)
    }

    fun isSecurityPatchBefore(targetDate: Calendar): Boolean {
        val securityPatch = Build.VERSION.SECURITY_PATCH
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return try {
            val securityPatchDate = dateFormat.parse(securityPatch)
            (securityPatchDate != null && (securityPatchDate < targetDate.time))
        } catch (e: Exception) {
            log(this@TimeUtils, "Error parsing security patch date\n$e")
            false
        }
    }

    fun isSecurityPatchAfter(targetDate: Calendar): Boolean {
        val securityPatch = Build.VERSION.SECURITY_PATCH
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return try {
            val securityPatchDate = dateFormat.parse(securityPatch)
            (securityPatchDate != null && (securityPatchDate > targetDate.time))
        } catch (e: Exception) {
            log(this@TimeUtils, "Error parsing security patch date\n$e")
            false
        }
    }

    fun setCurrentTimeTextClockAccent(tickIndicator: TextClock?, hourView: TextView?, color: Int) {
        if (tickIndicator == null || hourView == null) return

        setCurrentTimeHourRed(tickIndicator, hourView, color)

        tickIndicator.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (!TextUtils.isEmpty(s)) {
                    setCurrentTimeHourRed(tickIndicator, hourView, color)
                }
            }
        })
    }

    private fun setCurrentTimeHourRed(tickIndicator: TextClock, hourView: TextView, color: Int) {
        val hourFormat = tickIndicator.text.toString()
        val sb = StringBuilder(hourFormat)
        val spannableString = SpannableString(sb)
        var i = 0
        while (i < 2 && i < sb.length) {
            if (sb[i] == '1') {
                spannableString.setSpan(
                    ForegroundColorSpan(color),
                    i,
                    i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            i++
        }
        hourView.setText(spannableString, TextView.BufferType.SPANNABLE)
    }
}
