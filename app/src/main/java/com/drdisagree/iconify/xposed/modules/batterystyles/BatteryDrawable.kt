package com.drdisagree.iconify.xposed.modules.batterystyles

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.drdisagree.iconify.xposed.HookRes
import com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils

abstract class BatteryDrawable : Drawable() {

    protected var isRotation = false
    protected var scaledFillAlpha = false
    protected var scaledPerimeterAlpha = false
    protected var customBlendColor = false
    protected var customFillRainbow = false
    protected var customChargingIcon = false

    protected var chargingColor: Int = Color.TRANSPARENT
    protected var customFillColor: Int = Color.BLACK
    protected var customFillGradColor: Int = Color.BLACK
    protected var powerSaveColor: Int = Color.TRANSPARENT
    protected var powerSaveFillColor: Int = Color.TRANSPARENT

    open fun customizeBatteryDrawable(
        isRotation: Boolean,
        scaledPerimeterAlpha: Boolean,
        scaledFillAlpha: Boolean,
        customBlendColor: Boolean,
        customFillRainbow: Boolean,
        customFillColor: Int,
        customFillGradColor: Int,
        chargingColor: Int,
        powerSaveColor: Int,
        powerSaveFillColor: Int,
        customChargingIcon: Boolean
    ) {
        this.isRotation = isRotation
        this.scaledPerimeterAlpha = scaledPerimeterAlpha
        this.scaledFillAlpha = scaledFillAlpha
        this.customBlendColor = customBlendColor
        this.customFillRainbow = customFillRainbow
        this.customFillColor = customFillColor
        this.customFillGradColor = customFillGradColor
        this.chargingColor = chargingColor
        this.powerSaveColor = powerSaveColor
        this.powerSaveFillColor = powerSaveFillColor
        this.customChargingIcon = customChargingIcon

        invalidateSelf()
    }

    abstract fun setBatteryLevel(mLevel: Int)
    abstract fun setColors(fgColor: Int, bgColor: Int, singleToneColor: Int)
    abstract fun setShowPercentEnabled(showPercent: Boolean)
    abstract fun setChargingEnabled(charging: Boolean)
    abstract fun setPowerSavingEnabled(powerSaveEnabled: Boolean)

    fun getColorAttrDefaultColor(attr: Int, context: Context): Int {
        return getColorAttrDefaultColor(context, attr, 0)
    }

    fun getColorAttrDefaultColor(context: Context, attr: Int): Int {
        return getColorAttrDefaultColor(context, attr, 0)
    }

    fun getColorAttrDefaultColor(context: Context, attr: Int, defValue: Int): Int {
        return try {
            SettingsLibUtils.getColorAttrDefaultColor(attr, context, defValue);
        } catch (ignored: Throwable) {
            val obtainStyledAttributes: TypedArray =
                context.obtainStyledAttributes(intArrayOf(attr))
            val color: Int = obtainStyledAttributes.getColor(0, defValue)
            obtainStyledAttributes.recycle()
            color
        }
    }

    fun getResources(context: Context): Resources {
        return try {
            if (HookRes.modRes != null) HookRes.modRes
            else context.resources
        } catch (ignored: Throwable) {
            context.resources
        }
    }

    companion object {
        var showPercent = false
        var charging = false
        var powerSaveEnabled = false
    }
}