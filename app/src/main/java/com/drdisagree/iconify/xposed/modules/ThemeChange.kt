package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.xposed.ModPack
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ThemeChange(context: Context?) : ModPack(context!!) {

    private val mThemeChangedListeners = ArrayList<OnThemeChangedListener>()

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        instance = this

        // Get monet change so we can apply theme
        try {
            val ScrimController = XposedHelpers.findClass(
                "com.android.systemui.statusbar.phone.ScrimController",
                loadPackageParam.classLoader
            )
            XposedBridge.hookAllMethods(
                ScrimController,
                "updateThemeColors",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        onThemeChanged()
                    }
                })
        } catch (ignored: Throwable) {}

        try {
            val NotificationPanelViewController = XposedHelpers.findClass(
                "com.android.systemui.shade.NotificationPanelViewController",
                loadPackageParam.classLoader
            )
            XposedBridge.hookAllMethods(
                NotificationPanelViewController,
                "onThemeChanged",
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        onThemeChanged()
                    }
                })
        } catch (ignored: Throwable) {}
    }

    interface OnThemeChangedListener {
        fun onThemeChanged()
    }

    private fun onThemeChanged() {
        for (callback in mThemeChangedListeners) {
            try {
                callback.onThemeChanged()
            } catch (ignored: Throwable) {
            }
        }
    }

    fun registerThemeChangedCallback(callback: OnThemeChangedListener) {
        instance!!.mThemeChangedListeners.add(callback)
    }

    /** @noinspection unused */
    fun unRegisterThemeChangedCallback(callback: OnThemeChangedListener?) {
        instance!!.mThemeChangedListeners.remove(callback)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ThemeChange? = null

        fun getInstance(): ThemeChange {
            return instance!!
        }

    }

}