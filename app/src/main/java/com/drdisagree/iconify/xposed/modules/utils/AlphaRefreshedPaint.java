package com.drdisagree.iconify.xposed.modules.utils;

import android.graphics.Paint;

/*
 * When setting a paint's color, alpha gets reset... naturally.
 * So this is kind of paint that remembers its alpha and keeps it intact
 */
public class AlphaRefreshedPaint extends Paint {

    public AlphaRefreshedPaint(int flag) {
        super(flag);
    }

    @Override
    public void setColor(int color) {
        int alpha = getAlpha();

        super.setColor(color);
        setAlpha(alpha);
    }
}
