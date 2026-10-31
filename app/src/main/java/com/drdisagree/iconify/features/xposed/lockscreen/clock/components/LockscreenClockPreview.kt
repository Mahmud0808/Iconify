package com.drdisagree.iconify.features.xposed.lockscreen.clock.components

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.pagers.DevicePreviewPager
import com.drdisagree.iconify.core.utils.clock.ClockCustomizer
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.lockscreen.clock.viewmodels.LockscreenClockViewModel
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LockscreenClockPreview(
    lockscreenClockViewModel: LockscreenClockViewModel? = hiltViewModel()
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val navController = LocalNavController.current
    val prefController = LocalPreferenceController.current
    val isClockEnabled by prefController.observe(XposedKey.CUSTOM_LOCKSCREEN_CLOCK.name, false)
    val selectedStyle by prefController.observe(XposedKey.LSCLOCK_STYLE.name, 0)
    val elementEdits by prefController.observe(XposedKey.LSCLOCK_ELEMENT_EDITS.name, "")
    val customClock by prefController.observe(XposedKey.LSCLOCK_CUSTOM_CLOCK.name, "")

    LaunchedEffect(Unit) {
        lockscreenClockViewModel?.loadClockLayouts(resources)
        lockscreenClockViewModel?.loadWallpaper()
    }

    val layoutIds by (lockscreenClockViewModel?.clockLayoutIds ?: flowOf(emptyList()))
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val wallpaperBytes by (lockscreenClockViewModel?.wallpaperBytes ?: flowOf(null))
        .collectAsStateWithLifecycle(initialValue = null)
    val wallpaperReady by (lockscreenClockViewModel?.wallpaperReady ?: flowOf(false))
        .collectAsStateWithLifecycle(initialValue = false)

    val clockNone = stringResource(R.string.clock_none)
    val clockStyleName = stringResource(R.string.clock_style_name)
    val clockCustom = stringResource(R.string.lsclock_custom_style)
    val customIndex = layoutIds.lastIndex

    val names by remember(layoutIds, clockNone, clockStyleName, clockCustom) {
        mutableStateOf(
            List(layoutIds.size) { index ->
                when (index) {
                    0 -> clockNone
                    customIndex -> clockCustom
                    else -> clockStyleName.format(index)
                }
            }
        )
    }

    val allEdits = remember(elementEdits) { ClockCustomizer.parseEdits(elementEdits) }
    val customElements = remember(customClock) { ClockCustomizer.parseCustomClock(customClock) }

    val bindView = remember(allEdits, customElements, customIndex) {
        { index: Int, view: View ->
            if (index == customIndex) {
                ClockCustomizer.buildCustomClock(
                    view as ViewGroup,
                    customElements,
                    context,
                    LSCLOCK_FONT_FILE
                )
            } else {
                ClockCustomizer.applyEdits(view, allEdits[index].orEmpty(), context, LSCLOCK_FONT_FILE)
            }
        }
    }

    fun styleOf(index: Int): Int =
        if (index == customIndex) ClockCustomizer.CUSTOM_STYLE else index

    val startPageIndex = if (selectedStyle == ClockCustomizer.CUSTOM_STYLE) {
        customIndex.coerceAtLeast(0)
    } else {
        selectedStyle
    }

    key(layoutIds.isNotEmpty()) {
        DevicePreviewPager(
            modifier = Modifier.padding(top = 24.dp),
            enabled = isClockEnabled,
            startPageIndex = startPageIndex,
            layoutResIds = layoutIds,
            names = names,
            wallpaperReady = wallpaperReady,
            wallpaperBytes = wallpaperBytes,
            horizontalPaddingToIgnore = 16.dp,
            paddingTopPx = 100,
            paddingHorizontalPx = 28,
            onSelect = { index ->
                prefController.setInt(XposedKey.LSCLOCK_STYLE, styleOf(index))
            },
            bindView = bindView,
            trailingAction = { index ->
                FilledTonalIconButton(
                    enabled = isClockEnabled && index != 0,
                    onClick = withHaptic {
                        prefController.setInt(XposedKey.LSCLOCK_STYLE, styleOf(index))
                        navController.navigate(NavRoutes.MainGraph.Xposed.Lockscreen.ClockEditor)
                    }
                ) {
                    Icon(
                        imageVector = Iconsax.Outline.Edit,
                        contentDescription = stringResource(R.string.lsclock_editor_title),
                        modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize)
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LockscreenClockPreviewPreview() {
    PreviewComposable {
        LockscreenClockPreview(null)
    }
}
