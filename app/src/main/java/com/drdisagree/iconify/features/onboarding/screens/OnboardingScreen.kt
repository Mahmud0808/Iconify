package com.drdisagree.iconify.features.onboarding.screens

import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.ui.components.dialogs.ErrorDialog
import com.drdisagree.iconify.core.ui.components.dialogs.InstallationDialog
import com.drdisagree.iconify.core.ui.components.extensions.customClickable
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.utils.SystemUtils.restartDevice
import com.drdisagree.iconify.features.onboarding.components.PageOne
import com.drdisagree.iconify.features.onboarding.components.PageThree
import com.drdisagree.iconify.features.onboarding.components.PageTwo
import com.drdisagree.iconify.features.onboarding.states.InstallationEvent
import com.drdisagree.iconify.features.onboarding.states.InstallationState
import com.drdisagree.iconify.features.onboarding.viewmodels.OnboardingViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController = LocalNavController.current,
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val settings = LocalSettings.current
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getInstance(locale) }

    val pageCount = 3
    val disclaimerPageIndex = 1
    val lastPageIndex = pageCount - 1
    val pagerState = rememberPagerState(pageCount = { 3 })
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val state by onboardingViewModel.state.collectAsState()
    val logs = onboardingViewModel.logs
    val rebootRequired = onboardingViewModel.rebootRequired
    val interactionSource = remember { MutableInteractionSource() }
    var installationProgressRunning by remember { mutableStateOf(false) }
    var errorDialog by remember { mutableStateOf<InstallationEvent.Error?>(null) }
    val shouldShowRebootOption by remember(pagerState.currentPage, rebootRequired) {
        derivedStateOf { pagerState.currentPage == lastPageIndex && rebootRequired }
    }

    DisposableEffect(installationProgressRunning) {
        if (installationProgressRunning) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    when (state) {
        InstallationState.Installing -> {
            installationProgressRunning = true

            InstallationDialog(
                title = stringResource(R.string.installing),
                desc = stringResource(R.string.init_module_installation),
                logs = logs
            )
        }

        is InstallationState.Progressing -> {
            val p = state as InstallationState.Progressing

            val stepLocalized = numberFormat.format(p.step).toInt()
            val stepCountLocalized = numberFormat.format(onboardingViewModel.stepCount).toInt()

            InstallationDialog(
                title = stringResource(
                    R.string.step_number,
                    stepLocalized,
                    stepCountLocalized
                ),
                desc = stringResource(p.descRes),
                logs = logs
            )
        }

        else -> Unit
    }

    LaunchedEffect(Unit) {
        onboardingViewModel.state.collect { state ->
            when (state) {
                is InstallationState.Success -> {
                    installationProgressRunning = false

                    val destination = if (settings.isXposedOnlyMode) {
                        NavRoutes.MainGraph.Xposed.Root
                    } else {
                        NavRoutes.MainGraph.Home.Root
                    }

                    navController.navigate(destination) {
                        popUpTo(NavRoutes.Onboarding) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                InstallationState.Reboot -> {
                    installationProgressRunning = false
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        onboardingViewModel.events.collect { event ->
            when (event) {
                is InstallationEvent.Error -> {
                    installationProgressRunning = false
                    errorDialog = event
                }

                is InstallationEvent.Toast -> {
                    Toast.makeText(
                        context,
                        event.messageRes,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    errorDialog?.let { event ->
        ErrorDialog(
            title = event.titleRes,
            description = event.descRes,
            onDismiss = {
                errorDialog = null
                onboardingViewModel.clearError()
            }
        )
    }

    BackHandler(enabled = pagerState.currentPage != 0 && !installationProgressRunning) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    val isDisclaimerAtBottom by remember {
        derivedStateOf {
            val lastVisible = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val lastIndex = lazyListState.layoutInfo.totalItemsCount - 1
            lastVisible == lastIndex
        }
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = pagerState.currentPage != 1 && !shouldShowRebootOption
            ) { page ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> PageOne(pagerState = pagerState)
                        1 -> PageTwo(pagerState = pagerState, lazyListState = lazyListState)
                        2 -> PageThree(pagerState = pagerState)
                        else -> {}
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 24.dp)
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage != 0,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        initialScale = 0f
                    ) + fadeIn(animationSpec = tween(150)),
                    exit = scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        targetScale = 0f
                    ) + fadeOut(animationSpec = tween(150))
                ) {
                    TextButton(
                        onClick = withHaptic {
                            coroutineScope.launch {
                                if (pagerState.currentPage > 0) {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.padding(start = 24.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_back),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                @Composable
                fun onNextClick(skip: Boolean): () -> Unit = withHaptic {
                    if (shouldShowRebootOption) {
                        restartDevice()
                    } else {
                        coroutineScope.launch {
                            if (pagerState.currentPage < lastPageIndex) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                onboardingViewModel.startInstallation(skip)
                            }
                        }
                    }
                }

                Button(
                    onClick = {},
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .animateContentSize()
                        .padding(end = 24.dp)
                        .customClickable(
                            interactionSource = interactionSource,
                            onClick = onNextClick(false),
                            onLongClick = onNextClick(true),
                        ),
                    enabled = (pagerState.currentPage != disclaimerPageIndex || isDisclaimerAtBottom) && !installationProgressRunning
                ) {
                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                                    fadeOut(animationSpec = tween(90))
                        }
                    ) { targetPage ->
                        val isOnDisclaimerPage = targetPage == disclaimerPageIndex

                        Row(
                            modifier = Modifier.padding(
                                start = if (isOnDisclaimerPage) 8.dp else 10.dp,
                                end = 10.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isOnDisclaimerPage) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer {
                                            scaleX = 1.2f
                                            scaleY = 1.2f
                                        }
                                        .padding(end = 6.dp)
                                )
                            }
                            Text(
                                text = stringResource(
                                    if (!shouldShowRebootOption) {
                                        when (targetPage) {
                                            disclaimerPageIndex -> R.string.btn_agree
                                            lastPageIndex -> R.string.btn_lets_go
                                            else -> R.string.btn_next
                                        }
                                    } else {
                                        R.string.btn_reboot
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    PreviewComposable {
        OnboardingScreen()
    }
}