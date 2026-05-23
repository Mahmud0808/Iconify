package com.drdisagree.iconify.features.xposed.lockscreen.clock.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.pagers.DevicePreviewPager
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.lockscreen.clock.viewmodels.LockscreenClockViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun LockscreenClockPreview(
    lockscreenClockViewModel: LockscreenClockViewModel? = hiltViewModel()
) {
    val resources = LocalResources.current
    val prefController = LocalPreferenceController.current
    val isClockEnabled by prefController.observe(XposedKey.CUSTOM_LOCKSCREEN_CLOCK.name, false)
    val startPageIndex by prefController.observe(XposedKey.LSCLOCK_STYLE.name, 0)

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

    val names by remember(layoutIds, clockNone, clockStyleName) {
        mutableStateOf(
            List(layoutIds.size) { index ->
                if (index == 0) clockNone
                else clockStyleName.format(index)
            }
        )
    }

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
            prefController.setInt(XposedKey.LSCLOCK_STYLE, index)
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun LockscreenClockPreviewPreview() {
    PreviewComposable {
        LockscreenClockPreview(null)
    }
}