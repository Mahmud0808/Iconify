package com.drdisagree.iconify.xposed

import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.extras.ActivityLauncherUtils
import com.drdisagree.iconify.xposed.modules.extras.ExpandableViews
import com.drdisagree.iconify.xposed.modules.extras.GraphicsColorKt
import com.drdisagree.iconify.xposed.modules.extras.LaunchableViews
import com.drdisagree.iconify.xposed.modules.extras.MyConstraintSet
import com.drdisagree.iconify.xposed.modules.extras.SettingsLibUtils
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ConfigurationCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ControllersProvider
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.HeadsUpCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.KeyguardShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.QsShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.ThemeChangeCallback
import com.drdisagree.iconify.xposed.modules.lockscreen.AlbumArt
import com.drdisagree.iconify.xposed.modules.lockscreen.DepthWallpaper
import com.drdisagree.iconify.xposed.modules.lockscreen.Lockscreen
import com.drdisagree.iconify.xposed.modules.lockscreen.LockscreenClock
import com.drdisagree.iconify.xposed.modules.lockscreen.LockscreenWeather
import com.drdisagree.iconify.xposed.modules.lockscreen.LockscreenWidgets
import com.drdisagree.iconify.xposed.modules.misc.Miscellaneous
import com.drdisagree.iconify.xposed.modules.quicksettings.AppIconInNotification
import com.drdisagree.iconify.xposed.modules.quicksettings.ColorizeNotificationView
import com.drdisagree.iconify.xposed.modules.quicksettings.HeaderClock
import com.drdisagree.iconify.xposed.modules.quicksettings.HeaderImage
import com.drdisagree.iconify.xposed.modules.quicksettings.HeadsUpBlur
import com.drdisagree.iconify.xposed.modules.quicksettings.QSGrid
import com.drdisagree.iconify.xposed.modules.quicksettings.QSTheme
import com.drdisagree.iconify.xposed.modules.quicksettings.QSTransparency
import com.drdisagree.iconify.xposed.modules.quicksettings.QuickSettings
import com.drdisagree.iconify.xposed.modules.statusbar.AppIconsInStatusbar
import com.drdisagree.iconify.xposed.modules.statusbar.BatteryStyleManager
import com.drdisagree.iconify.xposed.modules.statusbar.ClockChip
import com.drdisagree.iconify.xposed.modules.statusbar.DualStatusbar
import com.drdisagree.iconify.xposed.modules.statusbar.OnGoingActionChip
import com.drdisagree.iconify.xposed.modules.statusbar.StatusbarLogo
import com.drdisagree.iconify.xposed.modules.statusbar.StatusbarMisc
import com.drdisagree.iconify.xposed.modules.statusbar.SwapSignalNetworkType
import com.drdisagree.iconify.xposed.modules.statusbar.SwapWiFiCellular
import com.drdisagree.iconify.xposed.modules.volume.VolumePanel
import com.drdisagree.iconify.xposed.utils.HookCheck

object EntryList {

    private val topPriorityCommonModPacks: List<Class<out ModPack>> = listOf(
        SettingsLibUtils::class.java,
        HookCheck::class.java
    )

    private val systemUIModPacks: List<Class<out ModPack>> = listOf(
        /* Top priority */
        GraphicsColorKt::class.java,
        MyConstraintSet::class.java,
        LaunchableViews::class.java,
        ExpandableViews::class.java,
        ActivityLauncherUtils::class.java,
        ControllersProvider::class.java,
        ThemeChangeCallback::class.java,
        HeadsUpCallback::class.java,
        QsShowingCallback::class.java,
        KeyguardShowingCallback::class.java,
        DozeCallback::class.java,
        ConfigurationCallback::class.java,
        /* Not so top priority :P */
        BatteryStyleManager::class.java,
        QSGrid::class.java,
        ClockChip::class.java,
        HeaderImage::class.java,
        HeaderClock::class.java,
        Lockscreen::class.java,
        LockscreenWidgets::class.java,
        LockscreenWeather::class.java,
        AlbumArt::class.java,
        Miscellaneous::class.java,
        QSTransparency::class.java,
        QuickSettings::class.java,
        AppIconsInStatusbar::class.java,
        SwapWiFiCellular::class.java,
        SwapSignalNetworkType::class.java,
        DualStatusbar::class.java,
        StatusbarMisc::class.java,
        VolumePanel::class.java,
        ColorizeNotificationView::class.java,
        AppIconInNotification::class.java,
        HeadsUpBlur::class.java,
        OnGoingActionChip::class.java,
        StatusbarLogo::class.java,
        DepthWallpaper::class.java,
        LockscreenClock::class.java,
        LockscreenWeather::class.java,
        LockscreenWidgets::class.java,
        QSTheme::class.java
    )

    fun getEntries(packageName: String): ArrayList<Class<out ModPack>> {
        val modPacks = ArrayList<Class<out ModPack>>()

        modPacks.addAll(topPriorityCommonModPacks)

        when (packageName) {
            FRAMEWORK_PACKAGE -> {}

            SYSTEMUI_PACKAGE -> {
                if (!HookEntry.isChildProcess) {
                    modPacks.addAll(systemUIModPacks)
                }
            }
        }

        return modPacks
    }
}
