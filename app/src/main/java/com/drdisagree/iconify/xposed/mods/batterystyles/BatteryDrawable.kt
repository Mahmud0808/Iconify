package com.drdisagree.iconify.xposed.mods.batterystyles

import android.graphics.Color
import android.graphics.drawable.Drawable

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

    companion object {
        var showPercent = false
        var charging = false
        var powerSaveEnabled = false
    }
}