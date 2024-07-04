package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.widget.TextClock
import android.widget.TextView
import de.robv.android.xposed.XposedBridge
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    private val TAG = "Iconify - ${TimeUtils::class.java.simpleName}: "
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
            XposedBridge.log(TAG + throwable)
        }
        return SimpleDateFormat(usFormat, Locale.getDefault()).format(Date())
    }

    fun formatTime(context: Context, format24H: String, format12H: String): String {
        return formatTime(if (DateFormat.is24HourFormat(context)) format24H else format12H)
    }

    fun formatTime(format: String): String {
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

        setupTextClockListener(context, tickIndicator, hourView, true)
        setupTextClockListener(context, tickIndicator, minuteView, false)
    }

    private fun setupTextClockListener(
        context: Context,
        tickIndicator: TextClock,
        textView: TextView,
        isHour: Boolean
    ) {
        tickIndicator.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    if (isHour) {
                        setCurrentTimeHour(context, textView)
                    } else {
                        setCurrentTimeMinute(textView)
                    }
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
}
