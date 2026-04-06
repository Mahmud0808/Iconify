package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.util.concurrent.CopyOnWriteArrayList

class ThemeChangeCallback(context: Context) : ModPack(context) {

    private var lastCallbackTime = 0L
    private val mThemeChangedListeners = CopyOnWriteArrayList<OnThemeChangedListener>()

    override fun onPreferenceUpdated(vararg key: String) {}

    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        instance = this

        val scrimControllerClass = findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController")
        val configurationControllerImplClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ConfigurationControllerImpl")
        val configurationListenerClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController$ConfigurationListener",
            $$"$$SYSTEMUI_PACKAGE.statusbar.phone.NotificationPanelViewController$ConfigurationListener",
            suppressError = true
        )

        scrimControllerClass
            .hookMethod("updateThemeColors")
            .runAfter { onThemeChanged() }

        configurationControllerImplClass
            .hookMethod("notifyThemeChanged")
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