package com.drdisagree.iconify.app.navigation

import com.drdisagree.iconify.R
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoutes {

    interface BottomBarTab {
        val route: NavRoutes
        val title: Int
        val iconChecked: Int
        val iconUnchecked: Int
    }

    @Serializable
    data object Onboarding : NavRoutes()

    @Serializable
    data object Main : NavRoutes()

    @Serializable
    sealed class Home : NavRoutes() {

        @Serializable
        data object Root : Home()

        @Serializable
        data object IconPack : Home()

        @Serializable
        data object CellularIcons : Home()

        @Serializable
        data object WifiIcons : Home()

        @Serializable
        data object SettingsIcons : Home()

        @Serializable
        data object Notification : Home()

        @Serializable
        data object ToastFrame : Home()

        @Serializable
        data object IconShape : Home()

        @Serializable
        data object More : Home()

        @Serializable
        data object Tab : Home(), BottomBarTab {
            override val route: NavRoutes = Root
            override val title: Int = R.string.navbar_home
            override val iconChecked: Int = R.drawable.ic_navbar_home_checked
            override val iconUnchecked: Int = R.drawable.ic_navbar_home_unchecked
        }
    }

    @Serializable
    sealed class Xposed : NavRoutes() {

        @Serializable
        data object Root : Xposed()

        @Serializable
        data object Main : Xposed()

        @Serializable
        sealed class Statusbar : Xposed() {

            @Serializable
            data object Root : Statusbar()

            @Serializable
            data object Main : Statusbar()

            @Serializable
            data object ClockChip : Statusbar()

            @Serializable
            data object Logo : Statusbar()

            @Serializable
            data object DualStatusbar : Statusbar()
        }

        @Serializable
        sealed class QuickSettings : Xposed() {

            @Serializable
            data object Root : QuickSettings()

            @Serializable
            data object Main : QuickSettings()

            @Serializable
            data object Transparency : QuickSettings()

            @Serializable
            data object HeaderImage : QuickSettings()

            @Serializable
            data object Themes : QuickSettings()

            @Serializable
            data object Margins : QuickSettings()
        }

        @Serializable
        sealed class Lockscreen : Xposed() {

            @Serializable
            data object Root : Lockscreen()

            @Serializable
            data object Main : Lockscreen()

            @Serializable
            data object Clock : Lockscreen()

            @Serializable
            data object Weather : Lockscreen()

            @Serializable
            sealed class Widgets : Lockscreen() {

                @Serializable
                data object Root : Widgets()

                @Serializable
                data object Main : Widgets()

                @Serializable
                data object Weather : Widgets()
            }

            @Serializable
            data object Location : Lockscreen()

            @Serializable
            data object DepthWallpaper : Lockscreen()

            @Serializable
            data object MediaAlbumArt : Lockscreen()
        }

        @Serializable
        data object VolumePanel : Xposed()

        @Serializable
        data object Tab : Xposed(), BottomBarTab {
            override val route: NavRoutes = Root
            override val title: Int = R.string.navbar_xposed
            override val iconChecked: Int = R.drawable.ic_navbar_xposed_checked
            override val iconUnchecked: Int = R.drawable.ic_navbar_xposed_unchecked
        }
    }

    @Serializable
    sealed class Settings : NavRoutes() {

        @Serializable
        data object Root : Settings()

        @Serializable
        data object LookAndFeel : Settings()

        @Serializable
        data object Credits : Settings()

        @Serializable
        data object Tab : Settings(), BottomBarTab {
            override val route: NavRoutes = Root
            override val title: Int = R.string.navbar_settings
            override val iconChecked: Int = R.drawable.ic_navbar_settings_checked
            override val iconUnchecked: Int = R.drawable.ic_navbar_settings_unchecked
        }
    }
}

val BOTTOM_BAR_TABS = listOf<NavRoutes.BottomBarTab>(
    NavRoutes.Xposed.Tab,
    NavRoutes.Home.Tab,
    NavRoutes.Settings.Tab,
)

val DEFAULT_BOTTOM_BAR_TAB = NavRoutes.Home.Tab