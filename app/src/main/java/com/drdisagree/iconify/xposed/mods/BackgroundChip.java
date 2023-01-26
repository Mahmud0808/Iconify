package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.HEADER_CLOCK_SWITCH;
import static com.drdisagree.iconify.common.References.QSPANEL_CLOCKBG;
import static com.drdisagree.iconify.common.References.QSPANEL_STATUSICONSBG;
import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BackgroundChip extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - BackgroundChip: ";
    private static final String CLASS_CLOCK = SYSTEM_UI_PACKAGE + ".statusbar.policy.Clock";
    private static final String CollapsedStatusBarFragmentClass = SYSTEM_UI_PACKAGE + ".statusbar.phone.fragment.CollapsedStatusBarFragment";
    boolean mShowSBClockBg = false;
    boolean mShowQSClockBg = false;
    boolean mShowQSStatusIcongBg = false;
    private Object mCollapsedStatusBarFragment = null;
    private ViewGroup mStatusBar = null;
    private View mClockView = null;
    private View mCenterClockView = null;
    private View mRightClockView = null;
    boolean showHeaderClock = false;
    int clockWidth = -1;
    int clockHeight = 1;
    private String rootPackagePath = "";

    public BackgroundChip(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mShowSBClockBg = Xprefs.getBoolean(STATUSBAR_CLOCKBG, true);
        mShowQSClockBg = Xprefs.getBoolean(QSPANEL_CLOCKBG, true);
        mShowQSStatusIcongBg = Xprefs.getBoolean(QSPANEL_STATUSICONSBG, true);
        showHeaderClock = Xprefs.getBoolean(HEADER_CLOCK_SWITCH, false);

        updateStatusBarClock();
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;

        rootPackagePath = lpparam.appInfo.sourceDir;

        final Class<?> Clock = findClass(CLASS_CLOCK, lpparam.classLoader);
        final Class<?> CollapsedStatusBarFragment = findClass(CollapsedStatusBarFragmentClass, lpparam.classLoader);

        hookAllConstructors(CollapsedStatusBarFragment, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mCollapsedStatusBarFragment = param.thisObject;
            }
        });

        findAndHookMethod(CollapsedStatusBarFragment, "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    mClockView = (View) getObjectField(param.thisObject, "mClockView");
                } catch (Throwable t) {
                    try {
                        Object mClockController = getObjectField(param.thisObject, "mClockController");
                        mClockView = (View) callMethod(mClockController, "getClock");
                    } catch (Throwable th) {
                        mClockView = (View) getObjectField(param.thisObject, "mLeftClock");
                    }
                }
                try {
                    mCenterClockView = (View) getObjectField(param.thisObject, "mCenterClockView");
                } catch (Throwable t) {
                    try {
                        Object mClockController = getObjectField(param.thisObject, "mClockController");
                        mCenterClockView = (View) callMethod(mClockController, "mCenterClockView");
                    } catch (Throwable th) {
                        mCenterClockView = (View) getObjectField(param.thisObject, "mCenterClock");
                    }
                }
                try {
                    mRightClockView = (View) getObjectField(param.thisObject, "mRightClockView");
                } catch (Throwable t) {
                    try {
                        Object mClockController = getObjectField(param.thisObject, "mClockController");
                        mRightClockView = (View) callMethod(mClockController, "mRightClockView");
                    } catch (Throwable th) {
                        mRightClockView = (View) getObjectField(param.thisObject, "mRightClock");
                    }
                }

                mStatusBar = (ViewGroup) getObjectField(mCollapsedStatusBarFragment, "mStatusBar");

                mStatusBar.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    updateStatusBarClock();
                });
            }
        });

        hookAllMethods(Clock, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    TextView clock = (TextView) param.thisObject;
                    clockWidth = clock.getWidth();
                    clockHeight = clock.getHeight();
                } catch (Throwable t) {
                    log(TAG + t);
                }
            }
        });

        updateStatusBarClock();
    }

    private void updateStatusBarClock() {
        if (mStatusBar == null || mClockView == null || mCenterClockView == null || mRightClockView == null)
            return;

        float corner = (Xprefs.getInt("cornerRadius", 16) + 8) * mContext.getResources().getDisplayMetrics().density;
        ShapeDrawable mDrawable = new ShapeDrawable(new RoundRectShape(
                new float[]{
                        corner, corner,
                        corner, corner,
                        corner, corner,
                        corner, corner}, null, null));

        if (mShowSBClockBg && clockWidth != -1 && clockHeight != -1) {
            mDrawable.getPaint().setShader(new LinearGradient(0, 0, clockWidth, clockHeight, mContext.getResources().getColor(android.R.color.holo_blue_light), mContext.getResources().getColor(android.R.color.holo_blue_light), Shader.TileMode.MIRROR));

            mClockView.setPadding(14, 2, 14, 2);
            mClockView.setBackgroundDrawable(mDrawable);

            mCenterClockView.setPadding(14, 2, 14, 2);
            mCenterClockView.setBackgroundDrawable(mDrawable);

            mRightClockView.setPadding(14, 2, 14, 2);
            mRightClockView.setBackgroundDrawable(mDrawable);
            log(TAG + "added clock bg");
        } else {
            @SuppressLint("DiscouragedApi") int clockPaddingStart = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_clock_starting_padding", "dimen", mContext.getPackageName()));
            @SuppressLint("DiscouragedApi") int clockPaddingEnd = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_clock_end_padding", "dimen", mContext.getPackageName()));
            @SuppressLint("DiscouragedApi") int leftClockPaddingStart = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_left_clock_starting_padding", "dimen", mContext.getPackageName()));
            @SuppressLint("DiscouragedApi") int leftClockPaddingEnd = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_left_clock_end_padding", "dimen", mContext.getPackageName()));

            mClockView.setBackgroundResource(0);
            mClockView.setPaddingRelative(leftClockPaddingStart, 0, leftClockPaddingEnd, 0);
            mCenterClockView.setBackgroundResource(0);
            mCenterClockView.setPaddingRelative(0, 0, 0, 0);
            mRightClockView.setBackgroundResource(0);
            mRightClockView.setPaddingRelative(clockPaddingStart, 0, clockPaddingEnd, 0);
            log(TAG + "no clock bg");
        }
    }
}
