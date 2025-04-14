package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.CopyOnWriteArrayList

class ConfigurationCallback(context: Context) : ModPack(context) {

    private val lastConfig = Configuration()
    private var density: Int = 0
    private var smallestScreenWidth: Int = 0
    private var fontScale: Float = 0.toFloat()
    private val inCarMode: Boolean
    private var uiMode: Int = 0
    private var localeList: LocaleList? = null
    private var layoutDirection: Int
    private var orientation = Configuration.ORIENTATION_UNDEFINED

    private val mConfigListeners = CopyOnWriteArrayList<ConfigurationListener>()

    init {
        val currentConfig = mContext.resources.configuration
        lastConfig.updateFrom(currentConfig)
        fontScale = currentConfig.fontScale
        density = currentConfig.densityDpi
        smallestScreenWidth = currentConfig.smallestScreenWidthDp
        inCarMode = currentConfig.uiMode and Configuration.UI_MODE_TYPE_MASK ==
                Configuration.UI_MODE_TYPE_CAR
        uiMode = currentConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        localeList = currentConfig.locales
        layoutDirection = currentConfig.layoutDirection
    }

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        instance = this

        val configurationControllerImplClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ConfigurationControllerImpl")

        configurationControllerImplClass
            .hookMethod("onConfigurationChanged")
            .runAfter { param ->
                val newConfig = param.args[0] as Configuration

                val fontScale = newConfig.fontScale
                val density = newConfig.densityDpi
                val uiMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
                val uiModeChanged = uiMode != this.uiMode
                val smallestScreenWidth = newConfig.smallestScreenWidthDp
                val localeList = newConfig.locales
                val newOrientation = newConfig.orientation

                mConfigListeners.forEach { it.onConfigChanged(newConfig) }

                if (density != this.density || fontScale != this.fontScale || inCarMode && uiModeChanged) {
                    mConfigListeners.forEach { it.onDensityOrFontScaleChanged() }
                    this.density = density
                    this.fontScale = fontScale
                }

                if (smallestScreenWidth != this.smallestScreenWidth) {
                    this.smallestScreenWidth = smallestScreenWidth
                    mConfigListeners.forEach { it.onSmallestScreenWidthChanged() }
                }

                if (localeList != this.localeList) {
                    this.localeList = localeList
                    mConfigListeners.forEach { it.onLocaleListChanged() }
                }

                if (uiModeChanged) {
                    this.uiMode = uiMode
                    mConfigListeners.forEach { it.onUiModeChanged() }
                }

                if (layoutDirection != newConfig.layoutDirection) {
                    layoutDirection = newConfig.layoutDirection
                    mConfigListeners.forEach {
                        it.onLayoutDirectionChanged(layoutDirection == View.LAYOUT_DIRECTION_RTL)
                    }
                }

                if ((lastConfig.updateFrom(newConfig) and Int.Companion.MIN_VALUE) != 0) {
                    mConfigListeners.forEach { it.onThemeChanged() }
                }

                if (orientation != newOrientation) {
                    orientation = newOrientation
                    mConfigListeners.forEach { it.onOrientationChanged(orientation) }
                }
            }
    }

    interface ConfigurationListener {
        fun onConfigChanged(newConfig: Configuration) {}
        fun onDensityOrFontScaleChanged() {}
        fun onLayoutDirectionChanged(isLayoutRtl: Boolean) {}
        fun onLocaleListChanged() {}
        fun onOrientationChanged(orientation: Int) {}
        fun onSmallestScreenWidthChanged() {}
        fun onThemeChanged() {}
        fun onUiModeChanged() {}
    }

    fun registerConfigListener(callback: ConfigurationListener) {
        if (!mConfigListeners.contains(callback)) {
            mConfigListeners.add(callback)
        }
    }

    fun unregisterConfigListener(callback: ConfigurationListener) {
        mConfigListeners.remove(callback)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: ConfigurationCallback? = null

        fun getInstance(): ConfigurationCallback {
            return checkNotNull(instance) { "ConfigurationCallback is not initialized yet!" }
        }
    }
}