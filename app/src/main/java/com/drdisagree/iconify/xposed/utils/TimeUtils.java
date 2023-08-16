package com.drdisagree.iconify.xposed.utils;

import static de.robv.android.xposed.XposedBridge.log;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static String getNumericToText(String number) {
        int num = Integer.parseInt(number);
        String[] numbers = {
                "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
                "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen",
                "Twenty", "Twenty One", "Twenty Two", "Twenty Three", "Twenty Four", "Twenty Five", "Twenty Six", "Twenty Seven", "Twenty Eight", "Twenty Nine",
                "Thirty", "Thirty One", "Thirty Two", "Thirty Three", "Thirty Four", "Thirty Five", "Thirty Six", "Thirty Seven", "Thirty Eight", "Thirty Nine",
                "Forty", "Forty One", "Forty Two", "Forty Three", "Forty Four", "Forty Five", "Forty Six", "Forty Seven", "Forty Eight", "Forty Nine",
                "Fifty", "Fifty One", "Fifty Two", "Fifty Three", "Fifty Four", "Fifty Five", "Fifty Six", "Fifty Seven", "Fifty Eight", "Fifty Nine",
                "Sixty"
        };
        return numbers[num];
    }

    public static String regionFormattedDate(String USformat, String EUformat) {
        try {
            Date currentDate = new Date();
            Locale currentLocale = Locale.getDefault();

            if (currentLocale.equals(Locale.US)) {
                SimpleDateFormat usDateFormat = new SimpleDateFormat(USformat, Locale.US);
                return usDateFormat.format(currentDate);
            } else {
                SimpleDateFormat euDateFormat = new SimpleDateFormat(EUformat, currentLocale);
                return euDateFormat.format(currentDate);
            }
        } catch (Throwable throwable) {
            log("Iconify - TimeUtils: " + throwable);
        }

        return new SimpleDateFormat(USformat, Locale.getDefault()).format(new Date());
    }

    public static String formatTime(Context context, String format24H, String format12H) {
        return formatTime(DateFormat.is24HourFormat(context) ? format24H : format12H);
    }

    public static String formatTime(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }
}
