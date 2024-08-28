package com.drdisagree.iconify.utils

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object TextUtils {
    fun convertTextViewsToTitleCase(view: View?) {
        if (view == null) return

        if (view is ViewGroup) {
            val childCount: Int = view.childCount

            for (i in 0 until childCount) {
                val child: View = view.getChildAt(i)

                if (child is ViewGroup) {
                    convertTextViewsToTitleCase(child)
                } else if (child is TextView) {
                    val originalText: String = child.getText().toString()
                    val convertedText = convertToTitleCase(originalText)
                    child.text = convertedText
                }
            }
        } else if (view is TextView) {
            val originalText: String = view.getText().toString()
            val convertedText = convertToTitleCase(originalText)
            view.text = convertedText
        }
    }

    private fun convertToTitleCase(input: String?): String? {
        if (input.isNullOrEmpty()) return input

        val result = StringBuilder()
        var capitalizeNext = true

        for (c in input.toCharArray()) {
            var char = c

            if (Character.isWhitespace(char)) {
                capitalizeNext = true
            } else if (Character.isLetter(char)) {
                if (capitalizeNext) {
                    char = char.uppercaseChar()
                    capitalizeNext = false
                }
            }

            result.append(char)
        }
        return result.toString()
    }
}