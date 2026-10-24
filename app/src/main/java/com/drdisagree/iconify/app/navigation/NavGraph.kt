package com.drdisagree.iconify.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import com.drdisagree.iconify.core.common.LocalNavAnimatedVisibilityScope
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalSharedTransitionScope
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.ui.components.scaffolds.MainScaffold
import com.drdisagree.iconify.core.ui.utils.sharedHiltViewModel
import com.drdisagree.iconify.features.changelog.screens.ChangelogScreen
import com.drdisagree.iconify.features.common.viewmodels.BottomNavViewModel
import com.drdisagree.iconify.features.home.cellularicons.screens.CellularIconScreen
import com.drdisagree.iconify.features.home.iconpack.screens.IconPackScreen
import com.drdisagree.iconify.features.home.iconshape.screens.IconShapeScreen
import com.drdisagree.iconify.features.home.main.screens.HomeScreen
import com.drdisagree.iconify.features.home.notification.screens.NotificationScreen
import com.drdisagree.iconify.features.home.settingsicons.screens.SettingsIconsScreen
import com.drdisagree.iconify.features.home.toastframe.screens.ToastFrameScreen
import com.drdisagree.iconify.features.home.tweaks.colornengine.screens.ColorEngineScreen
import com.drdisagree.iconify.features.home.tweaks.cornerradius.screens.CornerRadiusScreen
import com.drdisagree.iconify.features.home.tweaks.main.screens.TweaksScreen
import com.drdisagree.iconify.features.home.tweaks.mediaplayer.screens.MediaPlayerScreen
import com.drdisagree.iconify.features.home.tweaks.miscellaneous.screens.MiscellaneousScreen
import com.drdisagree.iconify.features.home.tweaks.navigationbar.screens.NavigationBarScreen
import com.drdisagree.iconify.features.home.tweaks.statusbar.screens.TweaksStatusbarScreen
import com.drdisagree.iconify.features.home.wifiicons.screens.WifiIconScreen
import com.drdisagree.iconify.features.main.screens.MainScreen
import com.drdisagree.iconify.features.onboarding.screens.OnboardingScreen
import com.drdisagree.iconify.features.playground.screens.PlaygroundScreen
import androidx.activity.compose.LocalActivity
import com.drdisagree.iconify.features.settings.appupdates.screens.AppUpdatesScreen
import com.drdisagree.iconify.features.settings.credits.screens.CreditsScreen
import com.drdisagree.iconify.features.settings.lookandfeel.screens.LookAndFeelScreen
import com.drdisagree.iconify.features.settings.main.screens.SettingsScreen
import com.drdisagree.iconify.features.xposed.lockscreen.albumart.screens.LockscreenAlbumArtScreen
import com.drdisagree.iconify.features.xposed.lockscreen.clock.screens.LockscreenClockScreen
import com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.screens.DepthWallpaperScreen
import com.drdisagree.iconify.features.xposed.lockscreen.location.screens.LocationBrowseScreen
import com.drdisagree.iconify.features.xposed.lockscreen.main.screens.LockscreenScreen
import com.drdisagree.iconify.features.xposed.lockscreen.weather.screens.LockscreenWeatherScreen
import com.drdisagree.iconify.features.xposed.lockscreen.visualizer.screens.LockscreenVisualizerScreen
import com.drdisagree.iconify.features.xposed.lockscreen.widgets.main.screens.LockscreenWidgetsScreen
import com.drdisagree.iconify.features.xposed.lockscreen.widgets.weather.screens.LockscreenWidgetsWeatherScreen
import com.drdisagree.iconify.features.xposed.main.screens.XposedScreen
import com.drdisagree.iconify.features.xposed.quicksettings.clock.screens.HeaderClockScreen
import com.drdisagree.iconify.features.xposed.quicksettings.grid.screens.QsGridScreen
import com.drdisagree.iconify.features.xposed.quicksettings.headerimage.screens.HeaderImageScreen
import com.drdisagree.iconify.features.xposed.quicksettings.main.screens.QuickSettingsScreen
import com.drdisagree.iconify.features.xposed.quicksettings.margins.screens.QsMarginsScreen
import com.drdisagree.iconify.features.xposed.quicksettings.shape.screens.QsShapeSlidersScreen
import com.drdisagree.iconify.features.xposed.quicksettings.themes.screens.QsThemesScreen
import com.drdisagree.iconify.features.xposed.quicksettings.transparency.screens.QsTransparencyScreen
import com.drdisagree.iconify.features.xposed.statusbar.batterystyle.screens.BatteryStyleScreen
import com.drdisagree.iconify.features.xposed.statusbar.clockchip.screens.ClockChipScreen
import com.drdisagree.iconify.features.xposed.statusbar.dualstatusbar.screens.DualStatusbarScreen
import com.drdisagree.iconify.features.xposed.statusbar.logo.screens.StatusbarLogoScreen
import com.drdisagree.iconify.features.xposed.statusbar.main.screens.XposedStatusbarScreen
import com.drdisagree.iconify.features.xposed.volumepanel.screens.VolumePanelScreen
import com.drdisagree.iconify.features.search.SearchScreen
import kotlinx.serialization.json.Json

@Composable
fun NavGraph(
    skipOnboarding: Boolean,
    bottomNavViewModel: BottomNavViewModel = sharedHiltViewModel()
) {
    val previewMode = LocalInspectionMode.current
    val navController = LocalNavController.current
    val layoutDirection = LocalLayoutDirection.current
    val activity = LocalActivity.current
    val animationsEnabled = LocalSettings.current.animationsEnabled

    LaunchedEffect(activity) {
        if (activity?.intent?.getBooleanExtra("open_app_updates", false) == true) {
            activity.intent?.removeExtra("open_app_updates")
            navController.navigate(NavRoutes.MainGraph.Settings.AppUpdates) {
                launchSingleTop = true
            }
        }
    }

    androidx.compose.runtime.DisposableEffect(activity) {
        val listener = androidx.core.util.Consumer<android.content.Intent> { intent ->
            if (intent.getBooleanExtra("open_app_updates", false)) {
                intent.removeExtra("open_app_updates")
                navController.navigate(NavRoutes.MainGraph.Settings.AppUpdates) {
                    launchSingleTop = true
                }
            }
        }
        val componentActivity = activity as? androidx.activity.ComponentActivity
        componentActivity?.addOnNewIntentListener(listener)
        onDispose {
            componentActivity?.removeOnNewIntentListener(listener)
        }
    }

    val startDestination = rememberSaveable(
        saver = Saver(
            save = { route: NavRoutes -> Json.encodeToString(NavRoutes.serializer(), route) },
            restore = { str: String -> Json.decodeFromString(NavRoutes.serializer(), str) }
        )
    ) {
        if (!previewMode && skipOnboarding) {
            NavRoutes.MainGraph.Main
        } else {
            NavRoutes.Onboarding
        }
    }

    MainScaffold {
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = {
                        if (!animationsEnabled) return@NavHost EnterTransition.None

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
                        if (!animationsEnabled) return@NavHost ExitTransition.None

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
                        if (!animationsEnabled) return@NavHost EnterTransition.None

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
                        if (!animationsEnabled) return@NavHost ExitTransition.None

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
                    destination<NavRoutes.Onboarding> {
                        OnboardingScreen(navController = navController)
                    }
                    navigation<NavRoutes.MainGraph.Main>(startDestination = NavRoutes.MainGraph.Root) {
                        destination<NavRoutes.MainGraph.Root> { MainScreen() }
                        destination<NavRoutes.MainGraph.Search> { SearchScreen() }
                        destination<NavRoutes.MainGraph.Changelog> { ChangelogScreen() }
                        destination<NavRoutes.MainGraph.Playground> { PlaygroundScreen() }

                        navigation<NavRoutes.MainGraph.Home.Root>(startDestination = NavRoutes.MainGraph.Home.Tab) {
                            destination<NavRoutes.MainGraph.Home.Tab> { HomeScreen() }
                            destination<NavRoutes.MainGraph.Home.IconPack> { IconPackScreen() }
                            destination<NavRoutes.MainGraph.Home.CellularIcons> { CellularIconScreen() }
                            destination<NavRoutes.MainGraph.Home.WifiIcons> { WifiIconScreen() }
                            destination<NavRoutes.MainGraph.Home.SettingsIcons> { SettingsIconsScreen() }
                            destination<NavRoutes.MainGraph.Home.Notification> { NotificationScreen() }
                            destination<NavRoutes.MainGraph.Home.IconShape> { IconShapeScreen() }
                            destination<NavRoutes.MainGraph.Home.ToastFrame> { ToastFrameScreen() }

                            navigation<NavRoutes.MainGraph.Home.More.Root>(startDestination = NavRoutes.MainGraph.Home.More.Main) {
                                destination<NavRoutes.MainGraph.Home.More.Main> { TweaksScreen() }
                                destination<NavRoutes.MainGraph.Home.More.ColorEngine> { ColorEngineScreen() }
                                destination<NavRoutes.MainGraph.Home.More.UIRoundness> { CornerRadiusScreen() }
                                destination<NavRoutes.MainGraph.Home.More.StatusBar> { TweaksStatusbarScreen() }
                                destination<NavRoutes.MainGraph.Home.More.NavigationBar> { NavigationBarScreen() }
                                destination<NavRoutes.MainGraph.Home.More.MediaPlayer> { MediaPlayerScreen() }
                                destination<NavRoutes.MainGraph.Home.More.Miscellaneous> { MiscellaneousScreen() }
                            }
                        }

                        navigation<NavRoutes.MainGraph.Xposed.Root>(startDestination = NavRoutes.MainGraph.Xposed.Tab) {
                            destination<NavRoutes.MainGraph.Xposed.Tab> { XposedScreen() }

                            navigation<NavRoutes.MainGraph.Xposed.Statusbar.Root>(startDestination = NavRoutes.MainGraph.Xposed.Statusbar.Main) {
                                destination<NavRoutes.MainGraph.Xposed.Statusbar.Main> { XposedStatusbarScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Statusbar.ClockChip> { ClockChipScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Statusbar.BatteryStyle> { BatteryStyleScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Statusbar.Logo> { StatusbarLogoScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Statusbar.DualStatusbar> { DualStatusbarScreen() }
                            }

                            navigation<NavRoutes.MainGraph.Xposed.QuickSettings.Root>(startDestination = NavRoutes.MainGraph.Xposed.QuickSettings.Main) {
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.Main> { QuickSettingsScreen() }
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.Transparency> { QsTransparencyScreen() }
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.HeaderImage> { HeaderImageScreen() }
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.Clock> { HeaderClockScreen() }
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.Grid> { QsGridScreen() }
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.Themes> { QsThemesScreen() }
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.Margins> { QsMarginsScreen() }
                                destination<NavRoutes.MainGraph.Xposed.QuickSettings.ShapeSliders> { QsShapeSlidersScreen() }
                            }

                            navigation<NavRoutes.MainGraph.Xposed.Lockscreen.Root>(startDestination = NavRoutes.MainGraph.Xposed.Lockscreen.Main) {
                                destination<NavRoutes.MainGraph.Xposed.Lockscreen.Main> { LockscreenScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Lockscreen.Clock> { LockscreenClockScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Lockscreen.Weather> { LockscreenWeatherScreen() }

                                navigation<NavRoutes.MainGraph.Xposed.Lockscreen.Widgets.Root>(
                                    startDestination = NavRoutes.MainGraph.Xposed.Lockscreen.Widgets.Main
                                ) {
                                    destination<NavRoutes.MainGraph.Xposed.Lockscreen.Widgets.Main> { LockscreenWidgetsScreen() }
                                    destination<NavRoutes.MainGraph.Xposed.Lockscreen.Widgets.Weather> { LockscreenWidgetsWeatherScreen() }
                                }

                                destination<NavRoutes.MainGraph.Xposed.Lockscreen.DepthWallpaper> { DepthWallpaperScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Lockscreen.MediaAlbumArt> { LockscreenAlbumArtScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Lockscreen.Visualizer> { LockscreenVisualizerScreen() }
                                destination<NavRoutes.MainGraph.Xposed.Lockscreen.Location> { LocationBrowseScreen() }
                            }

                            destination<NavRoutes.MainGraph.Xposed.VolumePanel> { VolumePanelScreen() }
                        }

                        navigation<NavRoutes.MainGraph.Settings.Root>(startDestination = NavRoutes.MainGraph.Settings.Tab) {
                            destination<NavRoutes.MainGraph.Settings.Tab> { SettingsScreen() }
                            destination<NavRoutes.MainGraph.Settings.LookAndFeel> { LookAndFeelScreen() }
                            destination<NavRoutes.MainGraph.Settings.AppUpdates> { AppUpdatesScreen() }
                            destination<NavRoutes.MainGraph.Settings.Credits> { CreditsScreen() }
                        }
                    }
                }
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.destination
            ?.bottomTabIndex()
            ?.let(bottomNavViewModel::selectTab)
    }
}

fun NavDestination?.bottomTabIndex(): Int? {
    return BOTTOM_BAR_TABS.map { it.route }.indexOfFirst { tab ->
        this?.hierarchy?.any { it.hasRoute(tab::class) } == true
    }.takeIf { it >= 0 }
}

private inline fun <reified T : Any> NavGraphBuilder.destination(
    noinline content: @Composable (NavBackStackEntry) -> Unit
) {
    composable<T> { entry ->
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            content(entry)
        }
    }
}
