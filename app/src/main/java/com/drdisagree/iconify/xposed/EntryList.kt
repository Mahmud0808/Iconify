package com.drdisagree.iconify.xposed

import android.os.Build
import com.drdisagree.iconify.common.Const.PIXEL_LAUNCHER_PACKAGE
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.BackgroundChip
import com.drdisagree.iconify.xposed.modules.BatteryStyleManager
import com.drdisagree.iconify.xposed.modules.DepthWallpaper
import com.drdisagree.iconify.xposed.modules.HeaderClock
import com.drdisagree.iconify.xposed.modules.HeaderImage
import com.drdisagree.iconify.xposed.modules.IconUpdater
import com.drdisagree.iconify.xposed.modules.LockscreenClock
import com.drdisagree.iconify.xposed.modules.Miscellaneous
import com.drdisagree.iconify.xposed.modules.QSTransparency
import com.drdisagree.iconify.xposed.modules.QuickSettings
import com.drdisagree.iconify.xposed.modules.themes.QSBlackThemeA13
import com.drdisagree.iconify.xposed.modules.themes.QSBlackThemeA14
import com.drdisagree.iconify.xposed.modules.themes.QSFluidThemeA13
import com.drdisagree.iconify.xposed.modules.themes.QSFluidThemeA14
import com.drdisagree.iconify.xposed.modules.themes.QSLightThemeA12
import com.drdisagree.iconify.xposed.modules.themes.QSLightThemeA13
import com.drdisagree.iconify.xposed.modules.themes.QSLightThemeA14
import com.drdisagree.iconify.xposed.modules.utils.SettingsLibUtils
import com.drdisagree.iconify.xposed.utils.HookCheck

object EntryList {

    private val topPriorityCommonModPacks = listOf(
        SettingsLibUtils::class.java,
        HookCheck::class.java
    )

    private val systemUICommonModPacks = listOf(
        BackgroundChip::class.java,
        HeaderClock::class.java,
        HeaderImage::class.java,
        DepthWallpaper::class.java,
        LockscreenClock::class.java,
        Miscellaneous::class.java,
        QSTransparency::class.java,
        QuickSettings::class.java,
        BatteryStyleManager::class.java
    )

    private val systemUiAndroid12ModPacks = listOf(
        QSFluidThemeA13::class.java,
        QSBlackThemeA13::class.java,
        QSLightThemeA12::class.java
    )

    private val systemUiAndroid13ModPacks = listOf(
        QSFluidThemeA13::class.java,
        QSBlackThemeA13::class.java,
        QSLightThemeA13::class.java
    )

    private val systemUiAndroid14ModPacks = listOf(
        QSFluidThemeA14::class.java,
        QSBlackThemeA14::class.java,
        QSLightThemeA14::class.java
    )

    private val pixelLauncherModPacks = listOf(
        IconUpdater::class.java
    )

    fun getEntries(packageName: String): ArrayList<Class<out ModPack>> {
        val modPacks = ArrayList<Class<out ModPack>>()

        modPacks.addAll(topPriorityCommonModPacks)

        when (packageName) {
            SYSTEMUI_PACKAGE -> {
                if (!HookEntry.isChildProcess) {
                    modPacks.addAll(systemUICommonModPacks)

                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> { // Android 14+
                            modPacks.addAll(systemUiAndroid14ModPacks)
                        }

                        Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU -> { // Android 13
                            modPacks.addAll(systemUiAndroid13ModPacks)
                        }

                        else -> { // Android 12.0 and 12.1
                            modPacks.addAll(systemUiAndroid12ModPacks)
                        }
                    }
                }
            }

            PIXEL_LAUNCHER_PACKAGE -> {
                modPacks.addAll(pixelLauncherModPacks)
            }
        }

        return modPacks
    }
}
