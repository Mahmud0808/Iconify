package com.drdisagree.iconify.helpers

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.ui.graphics.Color
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_FOLDER_NAME

fun String.replaceAll(vararg replacements: Pair<String, Any>): String {
    var result = this
    for ((old, new) in replacements) {
        result = result.replace(old, new.toString())
    }
    return result
}

fun Color.Companion.fromHex(hex: String): Color {
    val clean = hex.removePrefix("#")

    val argb = when (clean.length) {
        3 -> { // RGB (12-bit)
            val r = clean[0].digitToInt(16) * 17
            val g = clean[1].digitToInt(16) * 17
            val b = clean[2].digitToInt(16) * 17
            (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        6 -> { // RRGGBB
            (0xFF shl 24) or clean.toLong(16).toInt()
        }

        8 -> { // AARRGGBB
            clean.toLong(16).toInt()
        }

        else -> error("Invalid hex color: $hex")
    }

    return Color(argb)
}

fun Color.Companion.fromHexSafe(hex: String): Color? = runCatching {
    fromHex(hex)
}.getOrNull()

@SuppressLint("SetWorldReadable")
fun Uri.toXposedSharedPath(customFileName: String? = null): String? {
    val context = appContext
    val resolver = context.contentResolver

    return try {
        val fileName =
            customFileName ?: resolver.query(this, null, null, null, null)?.use { cursor ->
                val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (col != -1 && cursor.moveToFirst()) cursor.getString(col) else null
            } ?: lastPathSegment ?: "unknown_file"

        val relativePath = "Download/$XPOSED_RESOURCE_FOLDER_NAME"

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection =
            "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf(fileName, "Download/$XPOSED_RESOURCE_FOLDER_NAME%")

        resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                val existingUri = ContentUris.withAppendedId(collection, id)
                try {
                    resolver.delete(existingUri, null, null)
                } catch (e: Exception) {
                    Log.e("FilePicker", "Could not delete existing file: $existingUri", e)
                }
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val itemUri = resolver.insert(collection, values) ?: return null

        resolver.openOutputStream(itemUri, "wt")?.use { output ->
            resolver.openInputStream(this)?.use { input ->
                input.copyTo(output)
            }
        }

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(itemUri, values, null, null)

        resolver.query(itemUri, arrayOf(MediaStore.Downloads.DATA), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
    } catch (e: Exception) {
        Log.e("FilePicker", "toXposedSharedPath failed", e)
        null
    }
}

fun String?.maskKey(): String {
    if (isNullOrEmpty()) return ""
    if (length <= 8) return "*".repeat(length)

    val start = take(4)
    val end = takeLast(4)
    val stars = "*".repeat(length - 8)

    return "$start$stars$end"
}