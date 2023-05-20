package com.drdisagree.iconify.xposed.mods.batterystyles

import android.graphics.drawable.Drawable

abstract class BatteryDrawable : Drawable() {

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