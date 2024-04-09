package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.text.format.DateFormat
import de.robv.android.xposed.XposedBridge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {

    private val TAG = "Iconify - ${TimeUtils::class.java.simpleName}: "

    fun getNumericToText(number: String): String {
        val num = number.toInt()
        val numbers = arrayOf(
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
        return numbers[num]
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

    fun formatTime(context: Context?, format24H: String?, format12H: String?): String {
        return formatTime(if (DateFormat.is24HourFormat(context)) format24H else format12H)
    }

    fun formatTime(format: String?): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date())
    }
}
