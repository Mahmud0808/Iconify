package com.drdisagree.iconify.xposed.modules.utils;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

public class DisplayUtils {

    public static boolean isScreenOn(Context context) {
        Display[] displays = getDisplayState(context);
        for (Display display : displays) {
            if (display.getState() == Display.STATE_ON) {
                return true;
            }
        }
        return false;
    }

    public static boolean isScreenOff(Context context) {
        Display[] displays = getDisplayState(context);
        for (Display display : displays) {
            if (display.getState() == Display.STATE_OFF) {
                return true;
            }
        }
        return false;
    }

    public static boolean isScreenDozing(Context context) {
        Display[] displays = getDisplayState(context);
        for (Display display : displays) {
            if (display.getState() == Display.STATE_DOZE ||
                    display.getState() == Display.STATE_DOZE_SUSPEND) {
                return true;
            }
        }
        return false;
    }

    private static Display[] getDisplayState(Context context) {
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        return dm.getDisplays();
    }
}
