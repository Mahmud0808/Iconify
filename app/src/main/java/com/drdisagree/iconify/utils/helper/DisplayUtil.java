package com.drdisagree.iconify.utils.helper;

import android.util.TypedValue;

import com.drdisagree.iconify.Iconify;

public class DisplayUtil {
    public static int IntToDp(int num) {
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, num, Iconify.Companion.getAppContext().getResources().getDisplayMetrics()));
    }
}
