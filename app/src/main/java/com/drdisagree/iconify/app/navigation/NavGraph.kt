package com.drdisagree.iconify.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.ui.components.scaffolds.MainScaffold
import com.drdisagree.iconify.core.ui.utils.sharedHiltViewModel
import com.drdisagree.iconify.features.common.viewmodels.BottomNavViewModel
import com.drdisagree.iconify.features.home.cellularicons.screens.CellularIconScreen
import com.drdisagree.iconify.features.home.iconpack.screens.IconPackScreen
import com.drdisagree.iconify.features.home.iconshape.screens.IconShapeScreen
import com.drdisagree.iconify.features.home.main.screens.HomeScreen
import com.drdisagree.iconify.features.home.notification.screens.NotificationScreen
import com.drdisagree.iconify.features.home.settingsicons.screens.SettingsIconsScreen
import com.drdisagree.iconify.features.home.toastframe.screens.ToastFrameScreen
import com.drdisagree.iconify.features.home.tweaks.screens.TweaksScreen
import com.drdisagree.iconify.features.home.wifiicons.screens.WifiIconScreen
import com.drdisagree.iconify.features.onboarding.screens.OnboardingScreen
import com.drdisagree.iconify.features.settings.lookandfeel.screens.LookAndFeelScreen
import com.drdisagree.iconify.features.settings.main.screens.SettingsScreen
import com.drdisagree.iconify.features.xposed.lockscreen.albumart.screens.LockscreenAlbumArtScreen
import com.drdisagree.iconify.features.xposed.lockscreen.clock.screens.LockscreenClockScreen
import com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.screens.DepthWallpaperScreen
import com.drdisagree.iconify.features.xposed.lockscreen.location.screens.LocationBrowseScreen
import com.drdisagree.iconify.features.xposed.lockscreen.main.screens.LockscreenScreen
import com.drdisagree.iconify.features.xposed.lockscreen.weather.screens.LockscreenWeatherScreen
import com.drdisagree.iconify.features.xposed.lockscreen.widgets.main.screens.LockscreenWidgetsScreen
import com.drdisagree.iconify.features.xposed.lockscreen.widgets.weather.screens.LockscreenWidgetsWeatherScreen
import com.drdisagree.iconify.features.xposed.main.screens.XposedScreen
import com.drdisagree.iconify.features.xposed.quicksettings.headerimage.screens.HeaderImageScreen
import com.drdisagree.iconify.features.xposed.quicksettings.main.screens.QuickSettingsScreen
import com.drdisagree.iconify.features.xposed.quicksettings.margins.screens.QsMarginsScreen
import com.drdisagree.iconify.features.xposed.quicksettings.themes.screens.QsThemesScreen
import com.drdisagree.iconify.features.xposed.quicksettings.transparency.screens.QsTransparencyScreen
import com.drdisagree.iconify.features.xposed.statusbar.clockchip.screens.ClockChipScreen
import com.drdisagree.iconify.features.xposed.statusbar.dualstatusbar.screens.DualStatusbarScreen
import com.drdisagree.iconify.features.xposed.statusbar.logo.screens.StatusbarLogoScreen
import com.drdisagree.iconify.features.xposed.statusbar.main.screens.StatusbarScreen
import com.drdisagree.iconify.features.xposed.volumepanel.screens.VolumePanelScreen
import kotlinx.serialization.json.Json

@Composable
fun NavGraph(
    skipOnboarding: Boolean,
    bottomNavViewModel: BottomNavViewModel = sharedHiltViewModel()
) {
    val previewMode = LocalInspectionMode.current
    val navController = LocalNavController.current
    val layoutDirection = LocalLayoutDirection.current

    val startDestination = rememberSaveable(
        saver = Saver(
            save = { route: NavRoutes -> Json.encodeToString(NavRoutes.serializer(), route) },
            restore = { str: String -> Json.decodeFromString(NavRoutes.serializer(), str) }
        )
    ) {
        if (!previewMode && skipOnboarding) {
            NavRoutes.Main
        } else {
            NavRoutes.Onboarding
        }
    }

    MainScaffold {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                when (initialState.destination) {
                    NavRoutes.Onboarding -> {
                        slideInHorizontally(
                            initialOffsetX = { 300 },
                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(500))
                    }

                    else -> {
                        val dir = resolveDirection(
                            initialState.destination,
                            targetState.destination
                        )

                        if (dir != null) {
                            slideInFrom(
                                layoutDirection = layoutDirection,
                                direction = dir
                            )
                        } else {
                            scaleIn(
                                initialScale = 1.05f,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        }
                    }
                }
            },
            exitTransition = {
                when (targetState.destination) {
                    NavRoutes.Onboarding -> {
                        slideOutHorizontally(
                            targetOffsetX = { -300 },
                            animationSpec = tween(500)
                        ) + fadeOut(animationSpec = tween(500))
                    }

                    else -> {
                        val dir = resolveDirection(
                            initialState.destination,
                            targetState.destination
                        )

                        if (dir != null) {
                            slideOutTo(
                                layoutDirection = layoutDirection,
                                direction = dir
                            )
                        } else {
                            scaleOut(
                                targetScale = 0.95f,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    }
                }
            },
            popEnterTransition = {
                val dir = resolveDirection(
                    initialState.destination,
                    targetState.destination
                )

                if (dir != null) {
                    slideInFrom(
                        layoutDirection = layoutDirection,
                        direction = dir
                    )
                } else {
                    fadeIn(tween(300)) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(300)
                    )
                }
            },
            popExitTransition = {
                val dir = resolveDirection(
                    initialState.destination,
                    targetState.destination
                )

                if (dir != null) {
                    slideOutTo(
                        layoutDirection = layoutDirection,
                        direction = dir
                    )
                } else {
                    fadeOut(tween(300)) + scaleOut(
                        targetScale = 1.05f,
                        animationSpec = tween(300)
                    )
                }
            }
        ) {
            composable<NavRoutes.Onboarding> {
                OnboardingScreen(navController = navController)
            }
            navigation<NavRoutes.Main>(startDestination = NavRoutes.Home.Root) {
                navigation<NavRoutes.Home.Root>(startDestination = NavRoutes.Home.Tab) {
                    composable<NavRoutes.Home.Tab> { HomeScreen() }
                    composable<NavRoutes.Home.More> { TweaksScreen() }
                    composable<NavRoutes.Home.IconPack> { IconPackScreen() }
                    composable<NavRoutes.Home.CellularIcons> { CellularIconScreen() }
                    composable<NavRoutes.Home.WifiIcons> { WifiIconScreen() }
                    composable<NavRoutes.Home.SettingsIcons> { SettingsIconsScreen() }
                    composable<NavRoutes.Home.Notification> { NotificationScreen() }
                    composable<NavRoutes.Home.IconShape> { IconShapeScreen() }
                    composable<NavRoutes.Home.ToastFrame> { ToastFrameScreen() }
                }

                navigation<NavRoutes.Xposed.Root>(startDestination = NavRoutes.Xposed.Tab) {
                    composable<NavRoutes.Xposed.Tab> { XposedScreen() }

                    navigation<NavRoutes.Xposed.Statusbar.Root>(startDestination = NavRoutes.Xposed.Statusbar.Main) {
                        composable<NavRoutes.Xposed.Statusbar.Main> { StatusbarScreen() }
                        composable<NavRoutes.Xposed.Statusbar.ClockChip> { ClockChipScreen() }
                        composable<NavRoutes.Xposed.Statusbar.Logo> { StatusbarLogoScreen() }
                        composable<NavRoutes.Xposed.Statusbar.DualStatusbar> { DualStatusbarScreen() }
                    }

                    navigation<NavRoutes.Xposed.QuickSettings.Root>(startDestination = NavRoutes.Xposed.QuickSettings.Main) {
                        composable<NavRoutes.Xposed.QuickSettings.Main> { QuickSettingsScreen() }
                        composable<NavRoutes.Xposed.QuickSettings.Transparency> { QsTransparencyScreen() }
                        composable<NavRoutes.Xposed.QuickSettings.HeaderImage> { HeaderImageScreen() }
                        composable<NavRoutes.Xposed.QuickSettings.Themes> { QsThemesScreen() }
                        composable<NavRoutes.Xposed.QuickSettings.Margins> { QsMarginsScreen() }
                    }

                    navigation<NavRoutes.Xposed.Lockscreen.Root>(startDestination = NavRoutes.Xposed.Lockscreen.Main) {
                        composable<NavRoutes.Xposed.Lockscreen.Main> { LockscreenScreen() }
                        composable<NavRoutes.Xposed.Lockscreen.Clock> { LockscreenClockScreen() }
                        composable<NavRoutes.Xposed.Lockscreen.Weather> { LockscreenWeatherScreen() }

                        navigation<NavRoutes.Xposed.Lockscreen.Widgets.Root>(startDestination = NavRoutes.Xposed.Lockscreen.Widgets.Main) {
                            composable<NavRoutes.Xposed.Lockscreen.Widgets.Main> { LockscreenWidgetsScreen() }
                            composable<NavRoutes.Xposed.Lockscreen.Widgets.Weather> { LockscreenWidgetsWeatherScreen() }
                        }

                        composable<NavRoutes.Xposed.Lockscreen.DepthWallpaper> { DepthWallpaperScreen() }
                        composable<NavRoutes.Xposed.Lockscreen.MediaAlbumArt> { LockscreenAlbumArtScreen() }
                        composable<NavRoutes.Xposed.Lockscreen.Location> { LocationBrowseScreen() }
                    }

                    composable<NavRoutes.Xposed.VolumePanel> { VolumePanelScreen() }
                }

                navigation<NavRoutes.Settings.Root>(startDestination = NavRoutes.Settings.Tab) {
                    composable<NavRoutes.Settings.Tab> { SettingsScreen() }
                    composable<NavRoutes.Settings.LookAndFeel> { LookAndFeelScreen() }
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        val currentDestination = navBackStackEntry?.destination
        val selectedIndex = currentDestination.bottomTabIndex()
            ?: BOTTOM_BAR_TABS.indexOf(DEFAULT_BOTTOM_BAR_TAB)

        bottomNavViewModel.selectTab(selectedIndex)
    }
}

fun NavDestination?.bottomTabIndex(): Int? {
    return BOTTOM_BAR_TABS.map { it.route }.indexOfFirst { tab ->
        this?.hierarchy?.any { it.hasRoute(tab::class) } == true
    }.takeIf { it >= 0 }
}