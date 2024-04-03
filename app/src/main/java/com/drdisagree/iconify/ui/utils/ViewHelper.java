package com.drdisagree.iconify.ui.utils;

import static android.content.Context.BATTERY_SERVICE;
import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.xposed.modules.batterystyles.BatteryDrawable;
import com.drdisagree.iconify.xposed.modules.batterystyles.CircleBattery;
import com.drdisagree.iconify.xposed.modules.batterystyles.DefaultBattery;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBattery;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryA;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryB;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryC;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryColorOS;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryD;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryE;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryF;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryG;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryH;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryI;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryJ;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryK;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryL;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryM;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryMIUIPill;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryN;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryO;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatterySmiley;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryStyleA;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryStyleB;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryiOS15;
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryiOS16;
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryAiroo;
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryCapsule;
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryLorn;
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryMx;
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryOrigami;
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBattery;
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBatteryColorOS;
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBatteryStyleA;
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBatteryStyleB;

import java.util.Objects;

public class ViewHelper {

    public static void disableNestedScrolling(ViewPager2 viewPager) {
        RecyclerView recyclerView = null;

        for (int i = 0; i < viewPager.getChildCount(); i++) {
            if (viewPager.getChildAt(i) instanceof RecyclerView) {
                recyclerView = (RecyclerView) viewPager.getChildAt(i);
                break;
            }
        }

        if (recyclerView != null) {
            recyclerView.setNestedScrollingEnabled(false);
        }
    }

    public static void setHeader(Context context, Toolbar toolbar, int title) {
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) context).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(title);
    }

    public static void setHeader(Context context, FragmentManager fragmentManager, Toolbar toolbar, int title) {
        toolbar.setTitle(context.getResources().getString(title));
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) context).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view1 -> new Handler(Looper.getMainLooper()).postDelayed(fragmentManager::popBackStack, FRAGMENT_BACK_BUTTON_DELAY));
    }

    public static int dp2px(float dp) {
        return dp2px((int) dp);
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Iconify.getAppContext().getResources().getDisplayMetrics());
    }

    public static Drawable[] getBatteryDrawables(Context context) {
        boolean nightMode = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int batteryColor = nightMode ? Color.WHITE : Color.BLACK;
        Drawable[] batteryDrawables = new Drawable[] {
                new DefaultBattery(context, batteryColor, false),
                new DefaultBattery(context, batteryColor, false),
                new DefaultBattery(context, batteryColor, false),
                new RLandscapeBattery(context, batteryColor, false),
                new LandscapeBattery(context, batteryColor, false),
                new PortraitBatteryCapsule(context, batteryColor, false),
                new PortraitBatteryLorn(context, batteryColor, false),
                new PortraitBatteryMx(context, batteryColor, false),
                new PortraitBatteryAiroo(context, batteryColor, false),
                new RLandscapeBatteryStyleA(context, batteryColor, false),
                new LandscapeBatteryStyleA(context, batteryColor, false),
                new RLandscapeBatteryStyleB(context, batteryColor, false),
                new LandscapeBatteryStyleB(context, batteryColor, false),
                new LandscapeBatteryiOS15(context, batteryColor, false),
                new LandscapeBatteryiOS16(context, batteryColor, false),
                new PortraitBatteryOrigami(context, batteryColor, false),
                new LandscapeBatterySmiley(context, batteryColor, false),
                new LandscapeBatteryMIUIPill(context, batteryColor, false),
                new LandscapeBatteryColorOS(context, batteryColor, false),
                new RLandscapeBatteryColorOS(context, batteryColor, false),
                new LandscapeBatteryA(context, batteryColor, false),
                new LandscapeBatteryB(context, batteryColor, false),
                new LandscapeBatteryC(context, batteryColor, false),
                new LandscapeBatteryD(context, batteryColor, false),
                new LandscapeBatteryE(context, batteryColor, false),
                new LandscapeBatteryF(context, batteryColor, false),
                new LandscapeBatteryG(context, batteryColor, false),
                new LandscapeBatteryH(context, batteryColor, false),
                new LandscapeBatteryI(context, batteryColor, false),
                new LandscapeBatteryJ(context, batteryColor, false),
                new LandscapeBatteryK(context, batteryColor, false),
                new LandscapeBatteryL(context, batteryColor, false),
                new LandscapeBatteryM(context, batteryColor, false),
                new LandscapeBatteryN(context, batteryColor, false),
                new LandscapeBatteryO(context, batteryColor, false),
                new CircleBattery(context, batteryColor, false),
                new CircleBattery(context, batteryColor, false)
        };

        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        for (Drawable batteryIcon : batteryDrawables) {
            if (batteryIcon == null) continue;
            ((BatteryDrawable)batteryIcon).setBatteryLevel(batLevel);
            ((BatteryDrawable)batteryIcon).setColors(batteryColor, batteryColor, batteryColor);
            if (batteryIcon instanceof CircleBattery && batteryIcon == batteryDrawables[batteryDrawables.length-1]) {
                ((CircleBattery)batteryIcon).setMeterStyle(BATTERY_STYLE_DOTTED_CIRCLE);
            }
            if (batteryIcon == batteryDrawables[1]) {
                RotateDrawable r = new RotateDrawable();
                r.setDrawable(batteryIcon);
                r.setFromDegrees(0);
                r.setToDegrees(90);
                r.setPivotX(0.5f);
                r.setPivotY(0.5f);
                batteryDrawables[1] = r;
            }
            if (batteryIcon == batteryDrawables[2]) {
                RotateDrawable r = new RotateDrawable();
                r.setDrawable(batteryIcon);
                r.setFromDegrees(0);
                r.setToDegrees(-90);
                r.setPivotX(0.5f);
                r.setPivotY(0.5f);
                batteryDrawables[2] = r;
            }

        }
        return batteryDrawables;
    }

    public static Drawable[] getChargingIcons(Context context) {
        Drawable[] chargingIcons = new Drawable[]{
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_bold, context.getTheme()), // Bold
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_asus, context.getTheme()), // Asus
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_buddy, context.getTheme()), // Buddy
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_evplug, context.getTheme()), // EV Plug
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_idc, context.getTheme()), // IDC
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_ios, context.getTheme()), // IOS
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_koplak, context.getTheme()), // Koplak
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_miui, context.getTheme()), // MIUI
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_mmk, context.getTheme()), // MMK
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_moto, context.getTheme()), // Moto
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_nokia, context.getTheme()), // Nokia
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_plug, context.getTheme()), // Plug
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_powercable, context.getTheme()), // Power Cable
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_powercord, context.getTheme()), // Power Cord
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_powerstation, context.getTheme()), // Power Station
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_realme, context.getTheme()), // Realme
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_soak, context.getTheme()), // Soak
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_stres, context.getTheme()), // Stres
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_strip, context.getTheme()), // Strip
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_usbcable, context.getTheme()), // USB Cable
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_charging_xiaomi, context.getTheme()) // Xiaomi
        };
        boolean nightMode = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (nightMode) {
            for (Drawable chargingIcon : chargingIcons) {
                if (chargingIcon == null) continue;
                chargingIcon.setTint(Color.WHITE);
            }
        }
        return chargingIcons;
    }
}
