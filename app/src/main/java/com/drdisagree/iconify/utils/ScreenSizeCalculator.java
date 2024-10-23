package com.drdisagree.iconify.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Calculates the size of the device's screen.
 */
public class ScreenSizeCalculator {

    private static final String TAG = "ScreenSizeCalculator";

    private static ScreenSizeCalculator sInstance;

    private Point mPortraitScreenSize;
    private Point mLandscapeScreenSize;

    public static ScreenSizeCalculator getInstance() {
        if (sInstance == null) {
            sInstance = new ScreenSizeCalculator();
        }
        return sInstance;
    }

    /**
     * Clears the static instance of ScreenSizeCalculator. Used in test when display metrics are
     * manipulated between test cases.
     */
    static void clearInstance() {
        sInstance = null;
    }

    /**
     * Calculates the device's screen size, in physical pixels.
     *
     * @return Screen size unadjusted for window decor or compatibility scale factors if API level is
     * 17+, otherwise return adjusted screen size. In both cases, returns size in units of
     * physical pixels.
     */
    public Point getScreenSize(Display display) {
        switch (Resources.getSystem().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                return getPortraitScreenSize(display);
            case Configuration.ORIENTATION_LANDSCAPE:
                return getLandscapeScreenSize(display);
            default:
                Log.e(TAG, "Unknown device orientation: "
                        + Resources.getSystem().getConfiguration().orientation);
                return getPortraitScreenSize(display);
        }
    }

    /**
     * Calculates the device's aspect ratio (height/width).
     * Note: The screen size is getting from {@link #getScreenSize}.
     */
    public float getScreenAspectRatio(Context context) {
        final WindowManager windowManager = context.getSystemService(WindowManager.class);
        final Point screenSize = getScreenSize(windowManager.getDefaultDisplay());
        return (float) screenSize.y / screenSize.x;
    }

    /**
     * Calculates the device's screen height.
     * Note: The screen size is getting from {@link #getScreenSize}.
     */
    public int getScreenHeight(Context context) {
        final WindowManager windowManager = context.getSystemService(WindowManager.class);
        final Point screenSize = getScreenSize(windowManager.getDefaultDisplay());
        return screenSize.y;
    }

    private Point getPortraitScreenSize(Display display) {
        if (mPortraitScreenSize == null) {
            mPortraitScreenSize = new Point();
        }
        writeDisplaySizeToPoint(display, mPortraitScreenSize);
        return new Point(mPortraitScreenSize);
    }

    private Point getLandscapeScreenSize(Display display) {
        if (mLandscapeScreenSize == null) {
            mLandscapeScreenSize = new Point();
        }
        writeDisplaySizeToPoint(display, mLandscapeScreenSize);
        return new Point(mLandscapeScreenSize);
    }

    /**
     * Writes the screen size of the provided display object to the provided Point object.
     */
    private void writeDisplaySizeToPoint(Display display, Point outPoint) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(outPoint);
        } else {
            display.getSize(outPoint);
        }
    }
}

