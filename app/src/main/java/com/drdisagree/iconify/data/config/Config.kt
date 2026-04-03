package com.drdisagree.iconify.data.config

import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.core.utils.ModuleUtils
import com.drdisagree.iconify.core.utils.RootUtils
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.data.keys.SettingsKey

object Config {

    @SuppressWarnings("ConstantConditions")
    private val SKIP_INSTALLATION = false

    @SuppressWarnings("ConstantConditions")
    val FORCE_OVERLAY_INSTALLATION = false

    val SKIP_TO_HOMEPAGE_FOR_TESTING: Boolean
        get() = SKIP_INSTALLATION &&
                !FORCE_OVERLAY_INSTALLATION &&
                BuildConfig.DEBUG

    fun shouldSkipOnboarding(): Boolean {
        val isRooted = RootUtils.deviceProperlyRooted()
        val isModuleInstalled = ModuleUtils.moduleExists()
        val isOverlayInstalled = OverlayUtils.overlayExists()
        var isXposedOnlyMode = RPrefs.getBoolean(SettingsKey.XPOSED_ONLY_MODE, false)
        val savedVersionCode = RPrefs.getInt(SettingsKey.SAVED_VERSION_CODE)
        val isVersionCodeCorrect = BuildConfig.VERSION_CODE == savedVersionCode

        if (isRooted) {
            if (isOverlayInstalled) {
                RPrefs.putBoolean(SettingsKey.XPOSED_ONLY_MODE, false)
            } else if (isModuleInstalled) {
                RPrefs.putBoolean(SettingsKey.XPOSED_ONLY_MODE, true)
                isXposedOnlyMode = true
            }
        }

        val isModuleProperlyInstalled = isModuleInstalled &&
                (isOverlayInstalled || isXposedOnlyMode) &&
                !FORCE_OVERLAY_INSTALLATION

        return SKIP_TO_HOMEPAGE_FOR_TESTING ||
                (isRooted && isModuleProperlyInstalled && isVersionCodeCorrect)
    }
}