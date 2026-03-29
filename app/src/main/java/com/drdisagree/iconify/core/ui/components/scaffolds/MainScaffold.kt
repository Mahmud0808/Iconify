package com.drdisagree.iconify.core.ui.components.scaffolds

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.common.LocalHazeState
import com.drdisagree.iconify.core.common.LocalInnerPadding
import com.drdisagree.iconify.core.common.LocalLayerBackdrop
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.ui.components.bottomnavbar.BottomNavigation
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.others.withHapticResult
import com.drdisagree.iconify.core.ui.utils.sharedHiltViewModel
import com.drdisagree.iconify.features.common.viewmodels.BottomNavViewModel
import com.drdisagree.iconify.features.common.viewmodels.SystemActionViewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.materialkolor.ktx.harmonize
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze

private data class FabMenuItem(
    @param:DrawableRes val icon: Int,
    @param:StringRes val title: Int,
    val containerColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScaffold(
    systemActionViewModel: SystemActionViewModel = hiltViewModel(),
    bottomNavViewModel: BottomNavViewModel = sharedHiltViewModel(),
    content: @Composable () -> Unit
) {
    val hazeState = LocalHazeState.current
    val backdrop = LocalLayerBackdrop.current
    val isDarkTheme = LocalDarkMode.current
    val settings = LocalSettings.current
    val blurEffect = settings.blurEffect
    val floatingBottomBar = settings.floatingBottomBar

    val showBottomBar by bottomNavViewModel::isBottomBarVisible
    var bottomBarVisible by rememberSaveable { mutableStateOf(false) }

    val bottomNavBounceSpec = tween<IntOffset>(
        durationMillis = 400,
        easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)
    )
    val bottomNavFadeSpec = tween<Float>(durationMillis = 400)

    LaunchedEffect(showBottomBar) {
        if (showBottomBar) {
            bottomBarVisible = true
        } else if (bottomBarVisible) {
            bottomBarVisible = false
        }
    }

    val dockedBottomBarBgColor = if (blurEffect) {
        NavigationBarDefaults.containerColor.copy(alpha = .45f)
    } else {
        NavigationBarDefaults.containerColor
    }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val shouldRestartSystemUI by systemActionViewModel.shouldRestartSystemUI.collectAsStateWithLifecycle()
    val shouldRebootDevice by systemActionViewModel.shouldRebootDevice.collectAsStateWithLifecycle()
    var fabMenuVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(shouldRestartSystemUI, shouldRebootDevice) {
        if (shouldRestartSystemUI || shouldRebootDevice) {
            fabMenuVisible = true
        } else if (fabMenuVisible) {
            fabMenuVisible = false
        }
    }

    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer

    val warningContainer = if (isDarkTheme) Color(0xFFA46200) else Color(0xFFFFE0B2)
    val onWarningContainer = if (isDarkTheme) Color(0xFFFFE0B2) else Color(0xFF4E2600)

    val errorContainer = if (isDarkTheme) Color(0xFF8C1D18) else Color(0xFFF9DEDC)
    val onErrorContainer = if (isDarkTheme) Color(0xFFF9DEDC) else Color(0xFF410E0B)

    val harmonizedColors = remember(MaterialTheme.colorScheme) {
        object {
            val warningContainer = warningContainer.harmonize(tertiaryContainer)
            val onWarningContainer = onWarningContainer.harmonize(onTertiaryContainer)
            val errorContainer = errorContainer.harmonize(tertiaryContainer)
            val onErrorContainer = onErrorContainer.harmonize(onTertiaryContainer)
        }
    }

    val fabMenuItems = buildList {
        if (shouldRestartSystemUI || shouldRebootDevice) {
            add(
                FabMenuItem(
                    icon = R.drawable.ic_close,
                    title = R.string.btn_dismiss,
                    containerColor = tertiaryContainer,
                    contentColor = onTertiaryContainer,
                    onClick = { systemActionViewModel.clearFlags() }
                )
            )
        }
        if (shouldRebootDevice) {
            add(
                FabMenuItem(
                    icon = R.drawable.ic_pixel_device,
                    title = R.string.btn_restart_device,
                    containerColor = harmonizedColors.errorContainer,
                    contentColor = harmonizedColors.onErrorContainer,
                    onClick = { systemActionViewModel.triggerRestartDevice() }
                )
            )
        }
        if (shouldRestartSystemUI) {
            add(
                FabMenuItem(
                    icon = R.drawable.ic_restart_systemui,
                    title = R.string.btn_restart_systemui,
                    containerColor = harmonizedColors.warningContainer,
                    contentColor = harmonizedColors.onWarningContainer,
                    onClick = { systemActionViewModel.triggerRestartSystemUI() }
                )
            )
        }
    }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = if (!bottomBarVisible) {
                    slideInVertically(animationSpec = bottomNavBounceSpec) { it } +
                            fadeIn(animationSpec = bottomNavFadeSpec)
                } else EnterTransition.None,
                exit = if (!bottomBarVisible) {
                    slideOutVertically(animationSpec = bottomNavBounceSpec) { it } +
                            fadeOut(animationSpec = bottomNavFadeSpec)
                } else ExitTransition.None
            ) {
                BottomNavigation()
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabMenuVisible,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButtonMenu(
                    expanded = fabMenuExpanded,
                    button = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                if (fabMenuExpanded) TooltipAnchorPosition.Start
                                else TooltipAnchorPosition.Above
                            ),
                            tooltip = {
                                PlainTooltip {
                                    Text(text = stringResource(R.string.btn_pending))
                                }
                            },
                            state = rememberTooltipState(),
                        ) {
                            ToggleFloatingActionButton(
                                modifier = Modifier
                                    .semantics {
                                        stateDescription =
                                            if (fabMenuExpanded) "Expanded" else "Collapsed"
                                        contentDescription = "Toggle menu"
                                    }
                                    .focusRequester(focusRequester),
                                checked = fabMenuExpanded,
                                onCheckedChange = withHapticResult {
                                    fabMenuExpanded = !fabMenuExpanded
                                },
                                containerColor = ToggleFloatingActionButtonDefaults.containerColor(
                                    initialColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    finalColor = MaterialTheme.colorScheme.tertiary,
                                ),
                            ) {
                                val imageVector by remember {
                                    derivedStateOf {
                                        if (checkedProgress > 0.5f) Icons.Rounded.Close else Icons.Rounded.Pending
                                    }
                                }
                                Icon(
                                    painter = rememberVectorPainter(imageVector),
                                    contentDescription = null,
                                    modifier = Modifier.animateIcon(
                                        checkedProgress = { checkedProgress },
                                        color = ToggleFloatingActionButtonDefaults.iconColor(
                                            initialColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                            finalColor = MaterialTheme.colorScheme.onTertiary,
                                        )
                                    ),
                                )
                            }
                        }
                    }
                ) {
                    fabMenuItems.forEachIndexed { i, item ->
                        FloatingActionButtonMenuItem(
                            modifier = Modifier
                                .semantics {
                                    isTraversalGroup = true
                                    if (i == fabMenuItems.size - 1) {
                                        customActions = listOf(
                                            CustomAccessibilityAction("Close menu") {
                                                fabMenuExpanded = false
                                                true
                                            }
                                        )
                                    }
                                }
                                .then(
                                    if (i == 0) Modifier.onKeyEvent {
                                        if (it.type == KeyEventType.KeyDown &&
                                            (it.key == Key.DirectionUp || (it.isShiftPressed && it.key == Key.Tab))
                                        ) {
                                            focusRequester.requestFocus()
                                            true
                                        } else false
                                    } else Modifier
                                ),
                            onClick = withHaptic {
                                fabMenuExpanded = false
                                item.onClick()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(item.title),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            containerColor = item.containerColor,
                            contentColor = item.contentColor
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        CompositionLocalProvider(LocalInnerPadding provides innerPadding) {
            Box(
                modifier = Modifier.then(
                    if (floatingBottomBar) {
                        Modifier.layerBackdrop(backdrop)
                    } else {
                        Modifier.haze(
                            hazeState,
                            HazeStyle(
                                tint = dockedBottomBarBgColor,
                                blurRadius = 12.dp,
                                noiseFactor = HazeDefaults.noiseFactor
                            )
                        )
                    }
                )
            ) {
                content()
            }
        }
    }
}