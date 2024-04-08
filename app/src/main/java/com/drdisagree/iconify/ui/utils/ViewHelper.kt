package com.drdisagree.iconify.ui.utils;

import static com.drdisagree.iconify.Iconify.getAppContext;
import static com.drdisagree.iconify.common.Const.FRAGMENT_BACK_BUTTON_DELAY;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getAppContext().getResources().getDisplayMetrics());
    }

    public static Drawable[] getBatteryDrawables(Context context) {
        int batteryColor = getAppContext().getColor(R.color.textColorPrimary);

        Drawable[] batteryDrawables = new Drawable[] {
                new DefaultBattery(context, batteryColor),
                new DefaultBattery(context, batteryColor),
                new DefaultBattery(context, batteryColor),
                new RLandscapeBattery(context, batteryColor),
                new LandscapeBattery(context, batteryColor),
                new PortraitBatteryCapsule(context, batteryColor),
                new PortraitBatteryLorn(context, batteryColor),
                new PortraitBatteryMx(context, batteryColor),
                new PortraitBatteryAiroo(context, batteryColor),
                new RLandscapeBatteryStyleA(context, batteryColor),
                new LandscapeBatteryStyleA(context, batteryColor),
                new RLandscapeBatteryStyleB(context, batteryColor),
                new LandscapeBatteryStyleB(context, batteryColor),
                new LandscapeBatteryiOS15(context, batteryColor),
                new LandscapeBatteryiOS16(context, batteryColor),
                new PortraitBatteryOrigami(context, batteryColor),
                new LandscapeBatterySmiley(context, batteryColor),
                new LandscapeBatteryMIUIPill(context, batteryColor),
                new LandscapeBatteryColorOS(context, batteryColor),
                new RLandscapeBatteryColorOS(context, batteryColor),
                new LandscapeBatteryA(context, batteryColor),
                new LandscapeBatteryB(context, batteryColor),
                new LandscapeBatteryC(context, batteryColor),
                new LandscapeBatteryD(context, batteryColor),
                new LandscapeBatteryE(context, batteryColor),
                new LandscapeBatteryF(context, batteryColor),
                new LandscapeBatteryG(context, batteryColor),
                new LandscapeBatteryH(context, batteryColor),
                new LandscapeBatteryI(context, batteryColor),
                new LandscapeBatteryJ(context, batteryColor),
                new LandscapeBatteryK(context, batteryColor),
                new LandscapeBatteryL(context, batteryColor),
                new LandscapeBatteryM(context, batteryColor),
                new LandscapeBatteryN(context, batteryColor),
                new LandscapeBatteryO(context, batteryColor),
                new CircleBattery(context, batteryColor),
                new CircleBattery(context, batteryColor)
        };

        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        boolean wasFilledCircleBattery = false;

        for (Drawable batteryIcon : batteryDrawables) {
            if (batteryIcon == null) continue;

            int alpha = (int) (255 * 0.4);
            int batteryColorWithOpacity = ColorUtils.setAlphaComponent(batteryColor, alpha);

            ((BatteryDrawable) batteryIcon).setBatteryLevel(batteryLevel);
            ((BatteryDrawable) batteryIcon).setColors(batteryColor, batteryColorWithOpacity, batteryColor);

            if (batteryIcon instanceof CircleBattery) {
                if (wasFilledCircleBattery) {
                    ((CircleBattery) batteryIcon).setMeterStyle(BATTERY_STYLE_DOTTED_CIRCLE);
                } else {
                    wasFilledCircleBattery = true;
                }
            } else if (batteryIcon == batteryDrawables[1]) {
                batteryDrawables[1] = getRotateDrawable(batteryIcon, 90);
            } else if (batteryIcon == batteryDrawables[2]) {
                batteryDrawables[2] = getRotateDrawable(batteryIcon, -90);
            }
        }

        return batteryDrawables;
    }

    public static Drawable[] getChargingIcons(Context context) {
        Resources res = context.getResources();
        Theme theme = context.getTheme();
        
        Drawable[] chargingIcons = new Drawable[]{
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_bold, theme), // Bold
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_asus, theme), // Asus
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_buddy, theme), // Buddy
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_evplug, theme), // EV Plug
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_idc, theme), // IDC
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_ios, theme), // IOS
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_koplak, theme), // Koplak
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_miui, theme), // MIUI
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_mmk, theme), // MMK
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_moto, theme), // Moto
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_nokia, theme), // Nokia
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_plug, theme), // Plug
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_powercable, theme), // Power Cable
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_powercord, theme), // Power Cord
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_powerstation, theme), // Power Station
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_realme, theme), // Realme
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_soak, theme), // Soak
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_stres, theme), // Stres
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_strip, theme), // Strip
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_usbcable, theme), // USB Cable
                ResourcesCompat.getDrawable(res, R.drawable.ic_charging_xiaomi, theme) // Xiaomi
        };

        int iconColor = getAppContext().getColor(R.color.textColorPrimary);
        for (Drawable chargingIcon : chargingIcons) {
            if (chargingIcon == null) continue;
            chargingIcon.setTint(iconColor);
        }

        return chargingIcons;
    }

    @SuppressWarnings("all")
    private static Drawable getRotateDrawable(final Drawable d, final float angle) {
        final Drawable[] arD = {d};
        return new LayerDrawable(arD) {
            @Override
            public void draw(final Canvas canvas) {
                canvas.save();
                canvas.rotate(angle, (float) d.getBounds().width() / 2, (float) d.getBounds().height() / 2);
                super.draw(canvas);
                canvas.restore();
            }
        };
    }
}
