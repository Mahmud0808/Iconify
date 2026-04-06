package com.drdisagree.iconify.data.config

import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.utils.ModuleUtils
import com.drdisagree.iconify.core.utils.RootUtils
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.data.keys.SettingsKey

object Config {

    @SuppressWarnings("ConstantConditions")
    private val SKIP_INSTALLATION = true

    @SuppressWarnings("ConstantConditions")
    val FORCE_OVERLAY_INSTALLATION = false

    val SKIP_TO_HOMEPAGE_FOR_TESTING: Boolean
        get() = SKIP_INSTALLATION &&
                !FORCE_OVERLAY_INSTALLATION &&
                BuildConfig.DEBUG

    fun shouldSkipOnboarding(prefController: PreferenceController): Boolean {
        val isRooted = RootUtils.deviceProperlyRooted()
        val isModuleInstalled = ModuleUtils.moduleExists()
        val isOverlayInstalled = OverlayUtils.overlayExists()
        var isXposedOnlyMode = prefController.getBoolean(SettingsKey.XPOSED_ONLY_MODE)
        val savedOverlayVersion = prefController.getInt(SettingsKey.OVERLAY_VERSION_CODE)
        val isFirstInstall = prefController.getBoolean(SettingsKey.FIRST_INSTALL)
        val isOverlayVersionCurrent = BuildConfig.OVERLAY_VERSION_CODE == savedOverlayVersion

        if (isRooted) {
            if (isOverlayInstalled) {
                prefController.setBoolean(SettingsKey.XPOSED_ONLY_MODE, false)
            } else if (isModuleInstalled) {
                prefController.setBoolean(SettingsKey.XPOSED_ONLY_MODE, true)
                isXposedOnlyMode = true
            }
        }

        val isModuleProperlyInstalled = isModuleInstalled &&
                ((isOverlayInstalled && isOverlayVersionCurrent) || isXposedOnlyMode) &&
                !FORCE_OVERLAY_INSTALLATION

        return SKIP_TO_HOMEPAGE_FOR_TESTING || (isRooted && isModuleProperlyInstalled && !isFirstInstall)
    }
}