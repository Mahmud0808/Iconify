package com.drdisagree.iconify.features.xposed.statusbar.logo.components

import android.content.ContentUris
import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringArrayResource
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.data.common.XposedConst.STATUSBAR_LOGO_FILE
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_FOLDER_NAME
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.statusbar.logo.models.StatusbarLogoItem
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toCircularDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

@Composable
fun rememberStatusbarLogoItems(context: Context, reloadKey: Int): List<StatusbarLogoItem> {
    val prefController = LocalPreferenceController.current

    val customImageUri by prefController.observe(
        XposedKey.STATUSBAR_LOGO_FILE_URI.name,
        XposedKey.STATUSBAR_LOGO_FILE_URI.default as String
    )
    val showCustomImage by remember { derivedStateOf { customImageUri.isNotEmpty() } }

    val labels = stringArrayResource(R.array.status_bar_logo_style_entries)
    val values = stringArrayResource(R.array.status_bar_logo_style_values)
    val logoColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val logoItems = remember { mutableStateOf<List<StatusbarLogoItem>>(emptyList()) }

    LaunchedEffect(reloadKey) {
        val drawables = loadStatusbarLogoDrawables(context, logoColor, showCustomImage)
        logoItems.value = labels.mapIndexed { index, label ->
            StatusbarLogoItem(
                label = label,
                value = values.getOrNull(index) ?: index.toString(),
                drawable = drawables.getOrNull(index)
            )
        }
    }

    return logoItems.value
}

private suspend fun loadStatusbarLogoDrawables(
    context: Context,
    logoColor: Int,
    showCustomImage: Boolean
): List<Drawable> =
    withContext(Dispatchers.IO) {
        val predefinedLogos = arrayOf(
            R.drawable.ic_android_logo,
            R.drawable.ic_adidas,
            R.drawable.ic_alien,
            R.drawable.ic_apple_logo,
            R.drawable.ic_avengers,
            R.drawable.ic_batman,
            R.drawable.ic_batman_tdk,
            R.drawable.ic_beats,
            R.drawable.ic_biohazard,
            R.drawable.ic_blackberry,
            R.drawable.ic_cannabis,
            R.drawable.ic_emoticon_cool,
            R.drawable.ic_emoticon_devil,
            R.drawable.ic_fire,
            R.drawable.ic_heart,
            R.drawable.ic_nike,
            R.drawable.ic_pac_man,
            R.drawable.ic_puma,
            R.drawable.ic_rog,
            R.drawable.ic_spiderman,
            R.drawable.ic_superman,
            R.drawable.ic_windows,
            R.drawable.ic_xbox,
            R.drawable.ic_ghost,
            R.drawable.ic_ninja,
            R.drawable.ic_robot,
            R.drawable.ic_ironman,
            R.drawable.ic_captain_america,
            R.drawable.ic_flash,
            R.drawable.ic_tux_logo,
            R.drawable.ic_ubuntu_logo,
            R.drawable.ic_mint_logo,
            R.drawable.ic_amogus
        )

        val logoDrawables = predefinedLogos.mapNotNull { drawableRes ->
            ContextCompat.getDrawable(context, drawableRes)?.apply { setTint(logoColor) }
        }.toMutableList()

        val customDrawable = try {
            if (!showCustomImage) throw IllegalStateException("Custom image URI is empty")
            getCustomLogoFromMediaStore(context).apply { setTint(logoColor) }
        } catch (_: Throwable) {
            ContextCompat.getDrawable(context, R.drawable.ic_upload_file)
                ?.apply { setTint(logoColor) }
        }

        customDrawable?.let { logoDrawables.add(it) }

        logoDrawables
    }

private fun getCustomLogoFromMediaStore(context: Context): Drawable {
    val resolver = context.contentResolver
    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

    val projection = arrayOf(MediaStore.Downloads._ID)
    val selection =
        "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
    val selectionArgs =
        arrayOf(STATUSBAR_LOGO_FILE.name, "Download/$XPOSED_RESOURCE_FOLDER_NAME%")

    val cursor = resolver.query(collection, projection, selection, selectionArgs, null)
        ?: throw IllegalStateException("MediaStore query failed")

    cursor.use {
        if (!it.moveToFirst()) {
            throw FileNotFoundException("Custom status bar logo not found in MediaStore")
        }

        val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Downloads._ID))
        val uri = ContentUris.withAppendedId(collection, id)

        val source = ImageDecoder.createSource(resolver, uri)
        return ImageDecoder.decodeDrawable(source).toCircularDrawable(context)
    }
}