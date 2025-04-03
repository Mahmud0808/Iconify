package com.drdisagree.iconify.ui.utils

import androidx.fragment.app.Fragment
import com.drdisagree.iconify.ui.fragments.home.BrightnessBar
import com.drdisagree.iconify.ui.fragments.home.BrightnessBarPixel
import com.drdisagree.iconify.ui.fragments.home.CellularIcons
import com.drdisagree.iconify.ui.fragments.home.Home
import com.drdisagree.iconify.ui.fragments.home.IconPack
import com.drdisagree.iconify.ui.fragments.home.IconShape
import com.drdisagree.iconify.ui.fragments.home.MediaIcons
import com.drdisagree.iconify.ui.fragments.home.Notification
import com.drdisagree.iconify.ui.fragments.home.NotificationPixel
import com.drdisagree.iconify.ui.fragments.home.ProgressBar
import com.drdisagree.iconify.ui.fragments.home.QsPanelTile
import com.drdisagree.iconify.ui.fragments.home.QsPanelTilePixel
import com.drdisagree.iconify.ui.fragments.home.SettingsIcons
import com.drdisagree.iconify.ui.fragments.home.Switch
import com.drdisagree.iconify.ui.fragments.home.ToastFrame
import com.drdisagree.iconify.ui.fragments.home.WiFiIcons
import com.drdisagree.iconify.ui.fragments.settings.AppUpdates
import com.drdisagree.iconify.ui.fragments.settings.Changelog
import com.drdisagree.iconify.ui.fragments.settings.Credits
import com.drdisagree.iconify.ui.fragments.settings.Experimental
import com.drdisagree.iconify.ui.fragments.settings.Settings
import com.drdisagree.iconify.ui.fragments.tweaks.BasicColors
import com.drdisagree.iconify.ui.fragments.tweaks.ColorEngine
import com.drdisagree.iconify.ui.fragments.tweaks.ColoredBattery
import com.drdisagree.iconify.ui.fragments.tweaks.MediaPlayer
import com.drdisagree.iconify.ui.fragments.tweaks.Miscellaneous
import com.drdisagree.iconify.ui.fragments.tweaks.NavigationBar
import com.drdisagree.iconify.ui.fragments.tweaks.QsIconLabel
import com.drdisagree.iconify.ui.fragments.tweaks.QsRowColumn
import com.drdisagree.iconify.ui.fragments.tweaks.QsTileSize
import com.drdisagree.iconify.ui.fragments.tweaks.Statusbar
import com.drdisagree.iconify.ui.fragments.tweaks.Tweaks
import com.drdisagree.iconify.ui.fragments.tweaks.UiRoundness
import com.drdisagree.iconify.ui.fragments.tweaks.VolumePanel
import com.drdisagree.iconify.ui.fragments.xposed.AlbumArt
import com.drdisagree.iconify.ui.fragments.xposed.BackgroundChip
import com.drdisagree.iconify.ui.fragments.xposed.BatteryStyle
import com.drdisagree.iconify.ui.fragments.xposed.ClockChip
import com.drdisagree.iconify.ui.fragments.xposed.DepthWallpaper
import com.drdisagree.iconify.ui.fragments.xposed.DualStatusbar
import com.drdisagree.iconify.ui.fragments.xposed.HeaderClock
import com.drdisagree.iconify.ui.fragments.xposed.HeaderImage
import com.drdisagree.iconify.ui.fragments.xposed.Lockscreen
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenClock
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWeather
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWidget
import com.drdisagree.iconify.ui.fragments.xposed.OpQsHeader
import com.drdisagree.iconify.ui.fragments.xposed.Others
import com.drdisagree.iconify.ui.fragments.xposed.QsMargins
import com.drdisagree.iconify.ui.fragments.xposed.QuickSettings
import com.drdisagree.iconify.ui.fragments.xposed.StatusIconsChip
import com.drdisagree.iconify.ui.fragments.xposed.Themes
import com.drdisagree.iconify.ui.fragments.xposed.TransparencyBlur
import com.drdisagree.iconify.ui.fragments.xposed.Xposed

enum class FragmentGroup {
    HOME, TWEAKS, XPOSED, SETTINGS
}

fun isInGroup(fragment: Fragment, group: FragmentGroup): Boolean {
    return when (group) {
        FragmentGroup.HOME -> {
            fragment is Home ||
                    fragment is IconPack ||
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

        FragmentGroup.TWEAKS -> {
            fragment is Tweaks ||
                    fragment is ColorEngine ||
                    fragment is BasicColors ||
                    fragment is UiRoundness ||
                    fragment is ColoredBattery ||
                    fragment is QsRowColumn ||
                    fragment is QsIconLabel ||
                    fragment is QsTileSize ||
                    fragment is Statusbar ||
                    fragment is NavigationBar ||
                    fragment is MediaPlayer ||
                    fragment is VolumePanel ||
                    fragment is Miscellaneous
        }

        FragmentGroup.XPOSED -> {
            fragment is Xposed ||
                    fragment is BackgroundChip ||
                    fragment is ClockChip ||
                    fragment is StatusIconsChip ||
                    fragment is TransparencyBlur ||
                    fragment is QuickSettings ||
                    fragment is Lockscreen ||
                    fragment is Themes ||
                    fragment is OpQsHeader ||
                    fragment is BatteryStyle ||
                    fragment is QsMargins ||
                    fragment is com.drdisagree.iconify.ui.fragments.xposed.Statusbar ||
                    fragment is com.drdisagree.iconify.ui.fragments.xposed.VolumePanel ||
                    fragment is DualStatusbar ||
                    fragment is HeaderImage ||
                    fragment is HeaderClock ||
                    fragment is LockscreenClock ||
                    fragment is LockscreenWeather ||
                    fragment is LockscreenWidget ||
                    fragment is DepthWallpaper ||
                    fragment is AlbumArt ||
                    fragment is Others
        }

        FragmentGroup.SETTINGS -> {
            fragment is Settings ||
                    fragment is AppUpdates ||
                    fragment is Changelog ||
                    fragment is Credits ||
                    fragment is Experimental
        }
    }
}