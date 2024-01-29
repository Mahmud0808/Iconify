package com.drdisagree.iconify.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextUtil {

    public static void convertTextViewsToTitleCase(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return;
        }

        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);

            if (child instanceof ViewGroup) {
                convertTextViewsToTitleCase((ViewGroup) child);
            } else if (child instanceof TextView textView) {
                String originalText = textView.getText().toString();
                String convertedText = convertToTitleCase(originalText);
                textView.setText(convertedText);
            }
        }
    }

    public static String convertToTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            } else if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    c = Character.toUpperCase(c);
                    capitalizeNext = false;
                }
            }
            result.append(c);
        }

        return result.toString();
    }
}