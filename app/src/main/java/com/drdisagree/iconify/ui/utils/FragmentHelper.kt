package com.drdisagree.iconify.ui.utils

import androidx.fragment.app.Fragment
import com.drdisagree.iconify.ui.fragments.BasicColors
import com.drdisagree.iconify.ui.fragments.BrightnessBar
import com.drdisagree.iconify.ui.fragments.BrightnessBarPixel
import com.drdisagree.iconify.ui.fragments.CellularIcons
import com.drdisagree.iconify.ui.fragments.ColorEngine
import com.drdisagree.iconify.ui.fragments.ColoredBattery
import com.drdisagree.iconify.ui.fragments.Experimental
import com.drdisagree.iconify.ui.fragments.IconPack
import com.drdisagree.iconify.ui.fragments.IconShape
import com.drdisagree.iconify.ui.fragments.MediaIcons
import com.drdisagree.iconify.ui.fragments.MediaPlayer
import com.drdisagree.iconify.ui.fragments.Miscellaneous
import com.drdisagree.iconify.ui.fragments.MonetEngine
import com.drdisagree.iconify.ui.fragments.NavigationBar
import com.drdisagree.iconify.ui.fragments.Notification
import com.drdisagree.iconify.ui.fragments.NotificationPixel
import com.drdisagree.iconify.ui.fragments.ProgressBar
import com.drdisagree.iconify.ui.fragments.QsIconLabel
import com.drdisagree.iconify.ui.fragments.QsPanelMargin
import com.drdisagree.iconify.ui.fragments.QsPanelTile
import com.drdisagree.iconify.ui.fragments.QsPanelTilePixel
import com.drdisagree.iconify.ui.fragments.QsRowColumn
import com.drdisagree.iconify.ui.fragments.QsTileSize
import com.drdisagree.iconify.ui.fragments.SettingsIcons
import com.drdisagree.iconify.ui.fragments.Statusbar
import com.drdisagree.iconify.ui.fragments.Switch
import com.drdisagree.iconify.ui.fragments.ToastFrame
import com.drdisagree.iconify.ui.fragments.UiRoundness
import com.drdisagree.iconify.ui.fragments.VolumePanel
import com.drdisagree.iconify.ui.fragments.WiFiIcons
import com.drdisagree.iconify.ui.fragments.XposedBackgroundChip
import com.drdisagree.iconify.ui.fragments.XposedBatteryStyle
import com.drdisagree.iconify.ui.fragments.XposedLockscreenWidgets
import com.drdisagree.iconify.ui.fragments.home.Home
import com.drdisagree.iconify.ui.fragments.settings.AppUpdates
import com.drdisagree.iconify.ui.fragments.settings.Changelog
import com.drdisagree.iconify.ui.fragments.settings.Credits
import com.drdisagree.iconify.ui.fragments.settings.Settings
import com.drdisagree.iconify.ui.fragments.tweaks.Tweaks
import com.drdisagree.iconify.ui.fragments.xposed.DepthWallpaper
import com.drdisagree.iconify.ui.fragments.xposed.HeaderClock
import com.drdisagree.iconify.ui.fragments.xposed.HeaderImage
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenClock
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWeather
import com.drdisagree.iconify.ui.fragments.xposed.Others
import com.drdisagree.iconify.ui.fragments.xposed.QuickSettings
import com.drdisagree.iconify.ui.fragments.xposed.Themes
import com.drdisagree.iconify.ui.fragments.xposed.TransparencyBlur
import com.drdisagree.iconify.ui.fragments.xposed.Xposed

object FragmentHelper {

    fun isInGroup(fragment: Fragment, group: Int): Boolean {
        return when (group) {
            0 -> {
                fragment is Home ||
                        fragment is IconPack ||
                        fragment is ColoredBattery ||
                        fragment is MediaIcons ||
                        fragment is SettingsIcons ||
                        fragment is CellularIcons ||
                        fragment is WiFiIcons ||
                        fragment is BrightnessBar ||
                        fragment is BrightnessBarPixel ||
                        fragment is QsPanelTile ||
                        fragment is QsPanelTilePixel ||
                        fragment is Notification ||
                        fragment is NotificationPixel ||
                        fragment is ProgressBar ||
                        fragment is Switch ||
                        fragment is ToastFrame ||
                        fragment is IconShape
            }

            1 -> {
                fragment is Tweaks ||
                        fragment is ColorEngine ||
                        fragment is BasicColors ||
                        fragment is MonetEngine ||
                        fragment is UiRoundness ||
                        fragment is QsRowColumn ||
                        fragment is QsIconLabel ||
                        fragment is QsTileSize ||
                        fragment is QsPanelMargin ||
                        fragment is Statusbar ||
                        fragment is NavigationBar ||
                        fragment is MediaPlayer ||
                        fragment is VolumePanel ||
                        fragment is Miscellaneous
            }

            2 -> {
                fragment is Xposed ||
                        fragment is TransparencyBlur ||
                        fragment is QuickSettings ||
                        fragment is Themes ||
                        fragment is XposedBatteryStyle ||
                        fragment is com.drdisagree.iconify.ui.fragments.xposed.Statusbar ||
                        fragment is com.drdisagree.iconify.ui.fragments.xposed.VolumePanel ||
                        fragment is HeaderImage ||
                        fragment is HeaderClock ||
                        fragment is LockscreenClock ||
                        fragment is LockscreenWeather ||
                        fragment is XposedLockscreenWidgets ||
                        fragment is DepthWallpaper ||
                        fragment is XposedBackgroundChip ||
                        fragment is Others
            }

            3 -> {
                fragment is Settings ||
                        fragment is AppUpdates ||
                        fragment is Changelog ||
                        fragment is Credits ||
                        fragment is Experimental
            }

            else -> false
        }
    }
}