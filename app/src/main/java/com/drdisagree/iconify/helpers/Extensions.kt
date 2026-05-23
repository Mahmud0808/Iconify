package com.drdisagree.iconify.helpers

import androidx.compose.ui.graphics.Color

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

fun String?.maskKey(): String {
    if (isNullOrEmpty()) return ""
    if (length <= 8) return "*".repeat(length)

    val start = take(4)
    val end = takeLast(4)
    val stars = "*".repeat(length - 8)

    return "$start$stars$end"
}