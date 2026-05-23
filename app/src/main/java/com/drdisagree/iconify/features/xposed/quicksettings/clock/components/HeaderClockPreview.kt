package com.drdisagree.iconify.features.xposed.quicksettings.clock.components

import android.view.Gravity
import android.view.ViewGroup
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
import com.drdisagree.iconify.features.xposed.quicksettings.clock.viewmodels.HeaderClockViewModel
import kotlinx.coroutines.flow.flowOf

@Composable
fun HeaderClockPreview(
    headerClockViewModel: HeaderClockViewModel? = hiltViewModel()
) {
    val resources = LocalResources.current
    val prefController = LocalPreferenceController.current
    val isClockEnabled by prefController.observe(XposedKey.CUSTOM_HEADER_CLOCK.name, false)
    val startPageIndex by prefController.observe(XposedKey.HEADER_CLOCK_STYLE.name, 0)

    LaunchedEffect(Unit) {
        headerClockViewModel?.loadClockLayouts(resources)
    }

    val layoutIds by (headerClockViewModel?.clockLayoutIds ?: flowOf(emptyList()))
        .collectAsStateWithLifecycle(initialValue = emptyList())

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
        showPunchHole = false,
        horizontalPaddingToIgnore = 16.dp,
        androidViewHeight = ViewGroup.LayoutParams.WRAP_CONTENT,
        androidViewGravity = Gravity.START,
        paddingTopPx = 24,
        paddingHorizontalPx = 28,
        onSelect = { index ->
            prefController.setInt(XposedKey.HEADER_CLOCK_STYLE, index)
        }
    ) {
        FakeQuickSettings(
            modifier = Modifier.padding(top = 108.dp, start = 8.dp, end = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderClockPreviewPreview() {
    PreviewComposable {
        HeaderClockPreview(null)
    }
}