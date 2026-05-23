package com.drdisagree.iconify.data.config

import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.utils.AssetsUtils.readRawResource
import com.drdisagree.iconify.core.utils.ModuleUtils
import com.drdisagree.iconify.core.utils.RootUtils
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.helpers.replaceAll
import com.topjohnwu.superuser.Shell

object Config {

    @SuppressWarnings("ConstantConditions")
    private val SKIP_INSTALLATION = false

    @SuppressWarnings("ConstantConditions")
    val FORCE_OVERLAY_INSTALLATION = false

    @SuppressWarnings("ConstantConditions")
    val ENABLE_AUTO_UPDATE_IN_DEBUG = false

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

        val shouldSkipOnboarding = SKIP_TO_HOMEPAGE_FOR_TESTING ||
                (isRooted && isModuleProperlyInstalled && !isFirstInstall)

        if (shouldSkipOnboarding) {
            syncModulePropIfNeeded()
        }

        return shouldSkipOnboarding
    }

    private fun syncModulePropIfNeeded() {
        val modulePropPath = "$MODULE_DIR/module.prop"

        val result = Shell.cmd("cat $modulePropPath").exec()
        if (!result.isSuccess || result.out.isEmpty()) return

        val content = result.out.joinToString("\n")

        var moduleVersion: String? = null
        var moduleVersionCode: Int? = null

        content.lineSequence().forEach { line ->
            when {
                line.startsWith("version=") -> {
                    moduleVersion = line.substringAfter("version=").trim()
                }

                line.startsWith("versionCode=") -> {
                    moduleVersionCode = line.substringAfter("versionCode=").trim().toIntOrNull()
                }
            }
        }

        val needsUpdate = moduleVersion != BuildConfig.VERSION_NAME ||
                moduleVersionCode != BuildConfig.VERSION_CODE

        if (!needsUpdate) return

        val moduleProp = readRawResource(R.raw.module_module).replaceAll(
            "{{VERSION_NAME}}" to BuildConfig.VERSION_NAME,
            "{{VERSION_CODE}}" to BuildConfig.VERSION_CODE,
            "{{APP_MOTO}}" to appContext.resources.getString(R.string.app_motto)
        )

        Shell.cmd(
            "cat << 'EOF' > $modulePropPath\n${moduleProp.trimEnd()}\nEOF"
        ).exec()
    }
}