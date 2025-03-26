package com.drdisagree.iconify.xposed.modules.launcher

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.drdisagree.iconify.data.common.Preferences.LAUNCHER_HIDE_TOP_SHADOW
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.launcher.LauncherUtils.Companion.restartLauncher
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class TopShadow(context: Context) : ModPack(context) {

    private var removeTopShadow = false
    private var sysUiScrimInstance: Any? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            removeTopShadow = getBoolean(LAUNCHER_HIDE_TOP_SHADOW, false)
        }

        when (key.firstOrNull()) {
            LAUNCHER_HIDE_TOP_SHADOW -> if (removeTopShadow) {
                updateMaskBitmaps()
            } else {
                restartLauncher(mContext)
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val sysUiScrimClass = findClass("com.android.launcher3.graphics.SysUiScrim")

        sysUiScrimClass
            .hookConstructor()
            .runAfter { param ->
                sysUiScrimInstance = param.thisObject
                updateMaskBitmaps()
            }

        sysUiScrimClass
            .hookMethod(
                "onViewAttachedToWindow",
                "onViewDetachedFromWindow"
            )
            .runAfter { param ->
                if (!removeTopShadow) return@runAfter

                param.result = null
            }
    }

    private fun updateMaskBitmaps() {
        if (!removeTopShadow || sysUiScrimInstance == null) return

        val mRoot = sysUiScrimInstance.getField("mRoot") as View
        val mTopMaskPaint = sysUiScrimInstance.getField("mTopMaskPaint") as Paint

        mTopMaskPaint.setColor(Color.rgb(0x22, 0x22, 0x22))

        sysUiScrimInstance.setField("mHideSysUiScrim", true)
        sysUiScrimInstance.setField("mTopMaskBitmap", null)
        sysUiScrimInstance.setField("mBottomMaskBitmap", null)
        sysUiScrimInstance.setField("mTopMaskPaint", mTopMaskPaint)

        mRoot.invalidate()
    }
}