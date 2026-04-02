package com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceScreen
import com.drdisagree.iconify.core.preferences.arrayRes
import com.drdisagree.iconify.core.preferences.preferenceScreen
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.core.utils.AppUtils
import com.drdisagree.iconify.data.common.Const.AI_PLUGIN_PACKAGE
import com.drdisagree.iconify.data.common.Const.AI_PLUGIN_URL
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_BG_FILE
import com.drdisagree.iconify.data.common.XposedConst.DEPTH_WALL_FG_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.components.DepthWallpaperExample
import com.drdisagree.iconify.helpers.toXposedSharedPath
import com.drdisagree.iconify.xposed.modules.extras.utils.BitmapSubjectSegmenter

fun depthWallpaperPreferences(
    context: Context? = null,
    mlKitAvailable: Boolean? = null,
    aiPluginInstalled: Boolean = false
) = preferenceScreen {
    category {
        switch(
            key = XposedKey.LOCKSCREEN_DEPTH_WALLPAPER,
            isMasterSwitch = true,
            title = stringRes(R.string.enable_depth_wallpaper_title),
        )
    }

    category {
        composable(
            key = "depth_wallpaper_example",
            isVisible = { it.getBoolean(XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE) },
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val maxAvailableWidth = maxWidth
                val horizontalGap = 28.dp
                val centerGap = 48.dp
                val availableWidth = maxAvailableWidth - (horizontalGap * 2) - centerGap
                val imageWidth = (availableWidth / 2).coerceAtMost(120.dp)

                DepthWallpaperExample(
                    leftImage = painterResource(id = R.drawable.depth_wallpaper_example_fg),
                    rightImage = painterResource(id = R.drawable.depth_wallpaper_example_bg),
                    leftName = stringResource(R.string.foreground_image_title),
                    rightName = stringResource(R.string.background_image_title),
                    imageWidth = imageWidth,
                    gap = centerGap
                )
            }
        }
    }

    category {
        switch(
            key = XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE,
            title = stringRes(R.string.enable_custom_depth_wallpaper_title),
            summary = { _, _ -> stringRes(R.string.enable_custom_depth_wallpaper_desc) },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER) }
        )

        listPref(
            key = XposedKey.DEPTH_WALLPAPER_AI_MODE,
            title = stringRes(R.string.depth_wallpaper_ai_mode),
            entries = arrayRes(R.array.depth_wallpaper_mode_entries),
            entryValues = arrayRes(R.array.depth_wallpaper_mode_values),
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER) },
            isVisible = { !it.getBoolean(XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE) },
        )

        action(
            key = "xposed_depthwallpaper_aistatus",
            title = stringRes(R.string.depth_wallpaper_ai_status),
            summary = { prefs, _ ->
                if (prefs.getString(XposedKey.DEPTH_WALLPAPER_AI_MODE) == "0") {
                    when (mlKitAvailable) {
                        true -> stringRes(R.string.depth_wallpaper_model_ready)
                        false -> stringRes(R.string.depth_wallpaper_model_not_available)
                        null -> stringRes(R.string.depth_wallpaper_model_checking)
                    }
                } else {
                    if (aiPluginInstalled) {
                        stringRes(R.string.depth_wallpaper_ai_status_plugin_installed)
                    } else {
                        stringRes(R.string.depth_wallpaper_ai_status_plugin_not_installed)
                    }
                }
            },
            onClick = { _, prefs, _ ->
                if (prefs.getString(XposedKey.DEPTH_WALLPAPER_AI_MODE) != "0") {
                    if (aiPluginInstalled) {
                        context?.startActivity(
                            context
                                .packageManager
                                .getLaunchIntentForPackage(AI_PLUGIN_PACKAGE)!!
                        )
                    } else {
                        try {
                            context?.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    AI_PLUGIN_URL.toUri()
                                )
                            )
                        } catch (_: Exception) {
                            context?.let {
                                Toast.makeText(
                                    it,
                                    it.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER) },
            isVisible = { !it.getBoolean(XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE) },
        )

        filePicker(
            key = XposedKey.DEPTH_WALLPAPER_BACKGROUND_IMAGE_FILE_URI,
            title = stringRes(R.string.background_image_title),
            summary = { _, _ -> stringRes(R.string.background_image_desc) },
            pickerType = FilePickerType.Image,
            saveFileUri = true,
            onFileSelected = { _, uriString ->
                if (uriString.isNotEmpty()) {
                    uriString.toUri().toXposedSharedPath(DEPTH_WALL_BG_FILE.name)
                }
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER) },
            isVisible = { it.getBoolean(XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE) },
        )

        filePicker(
            key = XposedKey.DEPTH_WALLPAPER_FOREGROUND_IMAGE_FILE_URI,
            title = stringRes(R.string.foreground_image_title),
            summary = { _, _ -> stringRes(R.string.foreground_image_desc) },
            pickerType = FilePickerType.Image,
            saveFileUri = true,
            onFileSelected = { _, uriString ->
                if (uriString.isNotEmpty()) {
                    uriString.toUri().toXposedSharedPath(DEPTH_WALL_FG_FILE.name)
                }
            },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER) },
            isVisible = { it.getBoolean(XposedKey.DEPTH_WALLPAPER_CUSTOM_IMAGE) },
        )

        slider(
            key = XposedKey.DEPTH_WALLPAPER_FOREGROUND_IMAGE_OPACITY,
            title = stringRes(R.string.foreground_image_alpha_title),
            min = 0f,
            max = 100f,
            valueLabel = { "${it.toInt()}%" },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER) }
        )

        switch(
            key = XposedKey.DEPTH_WALLPAPER_SHOW_ON_AOD,
            title = stringRes(R.string.depth_wallpaper_on_aod_title),
            summary = { _, _ -> stringRes(R.string.depth_wallpaper_on_aod_desc) },
            isEnabled = { it.getBoolean(XposedKey.LOCKSCREEN_DEPTH_WALLPAPER) }
        )
    }

    category {
        info(
            key = "xposed_depth_wallpaper_info",
            text = stringRes(R.string.hig_res_image_footer_info),
        )
    }
}

@Composable
fun DepthWallpaperScreen() {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current

    var mlKitAvailable by remember { mutableStateOf<Boolean?>(null) }
    var aiPluginInstalled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BitmapSubjectSegmenter(context)
            .checkModelAvailability { response ->
                mlKitAvailable = response.areModulesAvailable()
            }
    }

    if (!previewMode) {
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    aiPluginInstalled = AppUtils.isAppInstalled(AI_PLUGIN_PACKAGE)
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    DepthWallpaperScreenContent(
        context = context,
        mlKitAvailable = mlKitAvailable,
        aiPluginInstalled = aiPluginInstalled
    )
}

@Composable
private fun DepthWallpaperScreenContent(
    context: Context? = null,
    mlKitAvailable: Boolean? = null,
    aiPluginInstalled: Boolean = false
) {
    PreferenceScreen(
        items = depthWallpaperPreferences(context, mlKitAvailable, aiPluginInstalled),
        title = stringResource(R.string.activity_title_depth_wallpaper),
        showBackIcon = true
    )
}

@Preview(showBackground = true)
@Composable
fun DepthWallpaperScreenPreview() {
    PreviewComposable {
        DepthWallpaperScreenContent()
    }
}