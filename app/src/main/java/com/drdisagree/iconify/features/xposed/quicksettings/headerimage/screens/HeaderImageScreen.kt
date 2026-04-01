package com.drdisagree.iconify.features.xposed.quicksettings.headerimage.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.data.common.XposedConst.HEADER_IMAGE_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.helpers.toXposedSharedPath

val headerImagePreferences = preferenceScreen {
    category {
        switch(
            key = XposedKey.CUSTOM_HEADER_IMAGE,
            isMasterSwitch = true,
            title = stringRes(R.string.activity_title_header_image),
        )
    }

    category {
        filePicker(
            key = XposedKey.HEADER_IMAGE_FILE_URI,
            title = stringRes(R.string.header_image_title),
            pickerType = FilePickerType.Image,
            saveFileUri = true,
            onFileSelected = { _, uriString ->
                if (uriString.isNotEmpty()) {
                    uriString.toUri().toXposedSharedPath(HEADER_IMAGE_FILE.name)
                }
            },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_IMAGE) }
        )

        slider(
            key = XposedKey.HEADER_IMAGE_HEIGHT,
            title = stringRes(R.string.header_image_height_title),
            min = 40f,
            max = 400f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_IMAGE) }
        )

        slider(
            key = XposedKey.HEADER_IMAGE_OPACITY,
            title = stringRes(R.string.header_image_opacity),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.toInt()}%" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_IMAGE) }
        )

        slider(
            key = XposedKey.HEADER_IMAGE_BOTTOM_FADE_AMOUNT,
            title = stringRes(R.string.header_image_bottom_fade),
            min = 0f,
            max = 120f,
            valueLabel = { "${it.toInt()}dp" },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_IMAGE) }
        )

        switch(
            key = XposedKey.HEADER_IMAGE_ZOOM_TO_FIT,
            title = stringRes(R.string.header_image_zoom_to_fit_title),
            summary = { _, _ -> stringRes(R.string.header_image_zoom_to_fit_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_IMAGE) }
        )

        switch(
            key = XposedKey.HEADER_IMAGE_HIDE_IN_LANDSCAPE,
            title = stringRes(R.string.header_image_hide_in_landscape_title),
            summary = { _, _ -> stringRes(R.string.header_image_hide_in_landscape_desc) },
            isEnabled = { it.getBoolean(XposedKey.CUSTOM_HEADER_IMAGE) }
        )
    }

    category {
        info(
            key = "header_image_info",
            text = stringRes(R.string.hig_res_image_footer_info),
        )
    }
}

@Composable
fun HeaderImageScreen() {
    PreferenceScreen(
        items = headerImagePreferences,
        title = stringResource(R.string.activity_title_header_image),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun HeaderImageScreenPreview() {
    PreviewComposable {
        HeaderImageScreen()
    }
}