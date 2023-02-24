package com.drdisagree.iconify.utils.helpers;

import android.util.TypedValue;

import com.drdisagree.iconify.Iconify;

public class DisplayUtil {
    public static int IntToDp(int num) {
        return ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, num, Iconify.getAppContext().getResources().getDisplayMetrics()));
    }
}
