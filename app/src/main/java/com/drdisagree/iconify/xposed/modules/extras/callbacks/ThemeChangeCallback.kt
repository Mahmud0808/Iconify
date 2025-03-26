package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.CopyOnWriteArrayList

class ThemeChangeCallback(context: Context) : ModPack(context) {

    private var lastCallbackTime = 0L
    private val mThemeChangedListeners = CopyOnWriteArrayList<OnThemeChangedListener>()

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        instance = this

        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")
        val notificationPanelViewControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController",
            "$SYSTEMUI_PACKAGE.statusbar.phone.NotificationPanelViewController"
        )
        val configurationListenerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController\$ConfigurationListener",
            "$SYSTEMUI_PACKAGE.statusbar.phone.NotificationPanelViewController\$ConfigurationListener",
            suppressError = true
        )

        scrimControllerClass
            .hookMethod("updateThemeColors")
            .runAfter { onThemeChanged() }

        notificationPanelViewControllerClass
            .hookMethod("onThemeChanged")
            .runAfter { onThemeChanged() }

        configurationListenerClass
            .hookMethod("onThemeChanged")
            .suppressError()
            .runAfter { onThemeChanged() }
    }

    interface OnThemeChangedListener {
        fun onThemeChanged()
    }

    private fun onThemeChanged() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastCallbackTime >= 200) {
            mThemeChangedListeners.forEach {
                try {
                    it.onThemeChanged()
                } catch (throwable: Throwable) {
                    log(this@ThemeChangeCallback, "onThemeChanged: $throwable")
                }
            }
            lastCallbackTime = currentTime
        }
    }

    fun registerThemeChangedCallback(callback: OnThemeChangedListener) {
        mThemeChangedListeners.add(callback)
    }

    fun unRegisterThemeChangedCallback(callback: OnThemeChangedListener?) {
        mThemeChangedListeners.remove(callback)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ThemeChangeCallback? = null

        fun getInstance(): ThemeChangeCallback {
            return checkNotNull(instance) { "ThemeChangeCallback is not initialized yet!" }
        }
    }
}