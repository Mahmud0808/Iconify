package com.drdisagree.iconify.xposed.modules.batterystyles;

/* Modified from AOSPMods
 * https://github.com/siavash79/PixelXpert/blob/canary/app/src/main/java/sh/siava/pixelxpert/modpacks/utils/batteryStyles/CircleBatteryDrawable.java
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Align.CENTER;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Typeface.BOLD;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_CIRCLE;
import static com.drdisagree.iconify.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_BLEND_COLOR;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_BATTERY_STYLE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static java.lang.Math.round;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.PathParser;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.drdisagree.iconify.xposed.modules.utils.AlphaRefreshedPaint;
import com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils;

import java.util.List;

public class CircleBattery extends BatteryDrawable {

    private static final String WARNING_STRING = "!";
    private static final int CRITICAL_LEVEL = 5;
    private static final int CIRCLE_DIAMETER = 45; //relative to dash effect size. Size doesn't matter as finally it gets scaled by parent
    private static final PathEffect DASH_PATH_EFFECT = new DashPathEffect(new float[]{3f, 2f}, 0f);
    private final Context mContext;
    private final boolean xposed;
    private int mChargingColor = 0xFF34C759;
    private int mPowerSaveColor = 0xFFFFA500;
    private boolean mShowPercentage = false;
    private int mDiameter;
    private final RectF mFrame = new RectF();
    private int mFGColor = Color.WHITE;
    private final Paint mTextPaint = new AlphaRefreshedPaint(ANTI_ALIAS_FLAG);
    private final Paint mFramePaint = new AlphaRefreshedPaint(ANTI_ALIAS_FLAG);
    private final Paint mBatteryPaint = new AlphaRefreshedPaint(ANTI_ALIAS_FLAG);
    private final Paint mWarningTextPaint = new AlphaRefreshedPaint(ANTI_ALIAS_FLAG);
    private final Paint mBoltPaint = new AlphaRefreshedPaint(ANTI_ALIAS_FLAG);
    private final ValueAnimator mBoltAlphaAnimator;
    private int[] mShadeColors;
    private float[] mShadeLevels;
    private Path mBoltPath;
    private float mAlphaPct;
    private boolean powerSaveEnabled = false;
    private boolean charging = false;
    private int batteryLevel = 0;
    private int[] batteryColors;
    private List<Integer> batteryLevels;
    private boolean customBlendColor = false;

    public CircleBattery(Context context, int frameColor, boolean xposed) {
        super();
        mContext = context;
        this.xposed = xposed;

        mFramePaint.setDither(true);
        mFramePaint.setStyle(STROKE);

        mTextPaint.setTypeface(Typeface.create("sans-serif-condensed", BOLD));
        mTextPaint.setTextAlign(CENTER);

        mWarningTextPaint.setTypeface(Typeface.create("sans-serif", BOLD));
        mWarningTextPaint.setTextAlign(CENTER);

        mBatteryPaint.setDither(true);
        mBatteryPaint.setStyle(STROKE);

        setColors(frameColor, frameColor, frameColor);

        setMeterStyle(xposed ? Xprefs.getInt(CUSTOM_BATTERY_STYLE, 0) : BATTERY_STYLE_CIRCLE);

        mBoltAlphaAnimator = ValueAnimator.ofInt(255, 255, 255, 45);

        mBoltAlphaAnimator.setDuration(2000);
        mBoltAlphaAnimator.setInterpolator(new FastOutSlowInInterpolator());
        mBoltAlphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mBoltAlphaAnimator.setRepeatCount(ValueAnimator.INFINITE);

        mBoltAlphaAnimator.addUpdateListener(valueAnimator -> invalidateSelf());
    }

    @Override
    public void setShowPercentEnabled(boolean showPercent) {
        mShowPercentage = showPercent;
        postInvalidate();
    }

    @Override
    public void setChargingEnabled(boolean charging) {
        this.charging = charging;
        postInvalidate();
    }

    @Override
    public void setBatteryLevel(int mLevel) {
        batteryLevel = mLevel;
        invalidateSelf();
    }

    public void setMeterStyle(int batteryStyle) {
        mFramePaint.setPathEffect(batteryStyle == BATTERY_STYLE_DOTTED_CIRCLE ? DASH_PATH_EFFECT : null);
        mBatteryPaint.setPathEffect(batteryStyle == BATTERY_STYLE_DOTTED_CIRCLE ? DASH_PATH_EFFECT : null);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        updateSize();
    }

    @Override
    public void setPowerSavingEnabled(boolean powerSaveEnabled) {
        this.powerSaveEnabled = powerSaveEnabled;
        postInvalidate();
    }

    @Override
    public void setColors(int fgColor, int bgColor, int singleToneColor) {
        mFGColor = fgColor;

        mBoltPaint.setColor(mFGColor);
        mFramePaint.setColor(bgColor);
        mTextPaint.setColor(mFGColor);

        initColors();

        invalidateSelf();
    }

    private void initColors() {
        if (xposed)
            customBlendColor = Xprefs.getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false);
        else
            customBlendColor = false;

        if (customBlendColor && getChargingColor() != Color.BLACK) {
            mChargingColor = getChargingColor();
        } else {
            mChargingColor = 0xFF34C759;
        }

        if (customBlendColor && getPowerSaveFillColor() != Color.BLACK) {
            mPowerSaveColor = getPowerSaveFillColor();
        } else {
            if (xposed)
                mPowerSaveColor = SettingsLibUtils.getColorAttrDefaultColor(android.R.attr.colorError, mContext);
            else
                getColorAttrDefaultColor(mContext, android.R.attr.colorError, Color.RED);
        }

        @ColorInt int fillColor = getCustomFillColor();
        @ColorInt int fillGradColor = getCustomFillGradColor();

        if (customBlendColor) {
            if (fillColor != Color.BLACK && fillGradColor != Color.BLACK) {
                batteryColors = new int[]{fillGradColor, ColorUtils.blendARGB(fillColor, Color.WHITE, 0.4f)};
            } else if (fillColor != Color.BLACK) {
                batteryColors = new int[]{Color.RED, ColorUtils.blendARGB(fillColor, Color.WHITE, 0.4f)};
            } else if (fillGradColor != Color.BLACK) {
                batteryColors = new int[]{fillGradColor, Color.YELLOW};
            } else {
                batteryColors = new int[]{Color.RED, Color.YELLOW};
            }
        } else {
            batteryColors = new int[]{mFGColor, mFGColor};
        }

        if (customBlendColor) {
            batteryLevels = List.of(10, 30);
        } else {
            batteryLevels = List.of(0, 0);
        }
    }

    private void refreshShadeColors() {
        if (batteryColors == null) return;

        initColors();

        mShadeColors = new int[batteryLevels.size() * 2 + 2];
        mShadeLevels = new float[mShadeColors.length];

        float lastPCT = 0f;

        for (int i = 0; i < batteryLevels.size(); i++) {
            float rangeLength = batteryLevels.get(i) - lastPCT;

            int pointer = 2 * i;
            mShadeLevels[pointer] = (lastPCT + rangeLength * 0.3f) / 100;
            mShadeColors[pointer] = batteryColors[i];

            mShadeLevels[pointer + 1] = (batteryLevels.get(i) - rangeLength * 0.3f) / 100;
            mShadeColors[pointer + 1] = batteryColors[i];
            lastPCT = batteryLevels.get(i);
        }

        @ColorInt int fillColor = getCustomFillColor();

        mShadeLevels[mShadeLevels.length - 2] = (batteryLevels.get(batteryLevels.size() - 1) + (100 - batteryLevels.get(batteryLevels.size() - 1) * 0.3f)) / 100;
        mShadeColors[mShadeColors.length - 2] = customBlendColor ? fillColor != Color.BLACK ?
                fillColor : Color.GREEN : mFGColor;

        mShadeLevels[mShadeLevels.length - 1] = 1f;
        mShadeColors[mShadeColors.length - 1] = customBlendColor ? fillColor != Color.BLACK ?
                fillColor : Color.GREEN : mFGColor;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (batteryLevel < 0 || mDiameter == 0) return;

        refreshShadeColors();

        setLevelBasedColors(mBatteryPaint, mFrame.centerX(), mFrame.centerY());

        if (charging && batteryLevel < 100) {
            if (!mBoltAlphaAnimator.isStarted()) {
                mBoltAlphaAnimator.start();
            }

            mBoltPaint.setAlpha(round((int) mBoltAlphaAnimator.getAnimatedValue() * mAlphaPct));

            canvas.drawPath(mBoltPath, mBoltPaint);
        } else if (mBoltAlphaAnimator.isStarted()) {
            mBoltAlphaAnimator.end();
        }

        canvas.drawArc(mFrame, 270f, 360f, false, mFramePaint);

        if (batteryLevel > 0) {
            canvas.drawArc(mFrame, 270f, 3.6f * batteryLevel, false, mBatteryPaint);
        }

        if (!charging && batteryLevel < 100 && mShowPercentage) {
            String pctText = batteryLevel > CRITICAL_LEVEL ? String.valueOf(batteryLevel) : WARNING_STRING;

            float textHeight = -mTextPaint.getFontMetrics().ascent;
            float pctX = mDiameter * .5f;
            float pctY = (mDiameter + textHeight) * 0.47f;
            canvas.drawText(pctText, pctX, pctY, mTextPaint);
        }
    }

    private void setLevelBasedColors(Paint paint, float centerX, float centerY) {
        paint.setShader(null);

        if (powerSaveEnabled) {
            paint.setColor(mPowerSaveColor);
            return;
        } else if (charging) {
            paint.setColor(mChargingColor);
            return;
        }

        if (mShadeColors == null) {
            for (int i = 0; i < batteryLevels.size(); i++) {
                if (batteryLevel <= batteryLevels.get(i)) {
                    if (i > 0) {
                        float range = batteryLevels.get(i) - batteryLevels.get(i - 1);
                        float currentPos = batteryLevel - batteryLevels.get(i - 1);

                        float ratio = currentPos / range;

                        paint.setColor(ColorUtils.blendARGB(batteryColors[i - 1], batteryColors[i], ratio));
                    } else {
                        paint.setColor(batteryColors[i]);
                    }
                    return;
                }
            }
            paint.setColor(mFGColor);
        } else {
            SweepGradient shader = new SweepGradient(centerX, centerY, mShadeColors, mShadeLevels);
            Matrix shaderMatrix = new Matrix();
            shaderMatrix.preRotate(270f, centerX, centerY);
            shader.setLocalMatrix(shaderMatrix);
            paint.setShader(shader);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mAlphaPct = alpha / 255f;

        mFramePaint.setAlpha(round(70 * alpha / 255f));

        mTextPaint.setAlpha(alpha);
        mBatteryPaint.setAlpha(alpha);
    }

    @SuppressLint({"DiscouragedApi", "RestrictedApi"})
    private void updateSize() {
        Resources res = mContext.getResources();

        mDiameter = getBounds().bottom - getBounds().top;

        mWarningTextPaint.setTextSize(mDiameter * 0.75f);

        float strokeWidth = mDiameter / 6.5f;
        mFramePaint.setStrokeWidth(strokeWidth);
        mBatteryPaint.setStrokeWidth(strokeWidth);

        mTextPaint.setTextSize(mDiameter * 0.52f);

        mFrame.set(strokeWidth / 2.0f,
                strokeWidth / 2.0f,
                mDiameter - strokeWidth / 2.0f,
                mDiameter - strokeWidth / 2.0f);

        @SuppressLint("DiscouragedApi")
        Path unscaledBoltPath = new Path();
        unscaledBoltPath.set(
                PathParser.createPathFromPathData(
                        res.getString(
                                res.getIdentifier(
                                        "android:string/config_batterymeterBoltPath",
                                        "string",
                                        "android"))));

        //Bolt icon
        Matrix scaleMatrix = new Matrix();
        RectF pathBounds = new RectF();

        unscaledBoltPath.computeBounds(pathBounds, true);

        float scaleF = (getBounds().height() - strokeWidth * 2) * .8f / pathBounds.height(); //scale comparing to 80% of icon's inner space

        scaleMatrix.setScale(scaleF, scaleF);

        mBoltPath = new Path();

        unscaledBoltPath.transform(scaleMatrix, mBoltPath);

        mBoltPath.computeBounds(pathBounds, true);

        //moving it to center
        mBoltPath.offset(getBounds().centerX() - pathBounds.centerX(),
                getBounds().centerY() - pathBounds.centerY());
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mFramePaint.setColorFilter(colorFilter);
        mBatteryPaint.setColorFilter(colorFilter);
        mWarningTextPaint.setColorFilter(colorFilter);
        mBoltPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public int getIntrinsicHeight() {
        return CIRCLE_DIAMETER;
    }

    @Override
    public int getIntrinsicWidth() {
        return CIRCLE_DIAMETER;
    }

    private final Runnable invalidateRunnable = this::invalidateSelf;

    private void postInvalidate() {
        unscheduleSelf(invalidateRunnable);
        scheduleSelf(invalidateRunnable, 0);
    }
}
