package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.utils.color.ColorUtils.colorNames
import com.drdisagree.iconify.utils.color.ColorUtils.colorToHex
import com.drdisagree.iconify.utils.overlay.compiler.MonetCompiler
import java.io.IOException

object MonetEngineManager {

    @Throws(IOException::class)
    fun buildOverlay(palette: List<List<List<Any>>>, force: Boolean): Boolean {
        val colors = colorNames
        val xmlStart = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n"

        val resources = StringBuilder(xmlStart)
        val resourcesNight = StringBuilder(xmlStart)

        for (i in colors.indices) {
            for (j in colors[i].indices) {
                RPrefs.putString(colors[i][j] + "_day", palette[0][i][j].toString())
                RPrefs.putString(colors[i][j] + "_night", palette[1][i][j].toString())

                resources
                    .append("    <color name=\"${colors[i][j]}\">${colorToHex(palette[0][i][j] as Int)}</color>\n")

                resourcesNight
                    .append("    <color name=\"${colors[i][j]}\">${colorToHex(palette[1][i][j] as Int)}</color>\n")
            }
        }

        resources
            .append("    <color name=\"holo_blue_light\">${colorToHex(palette[0][0][8] as Int)}</color>\n")
            .append("    <color name=\"holo_blue_dark\">${colorToHex(palette[0][2][8] as Int)}</color>\n")
            .append("    <color name=\"holo_green_light\">${colorToHex(palette[0][0][5] as Int)}</color>\n")
            .append("    <color name=\"holo_green_dark\">${colorToHex(palette[0][2][5] as Int)}</color>\n")
            .append("    <color name=\"accent_device_default\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_device_default_dark\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_device_default_light\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_material_dark\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_material_light\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_primary_device_default\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_secondary_device_default\">@*android:color/system_accent2_600</color>\n")
            .append("    <color name=\"accent_tertiary_device_default\">@*android:color/system_accent3_600</color>\n")
            .append("    <color name=\"autofill_background_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_dark\">@*android:color/system_neutral1_900</color>\n")
            .append("    <color name=\"background_light\">@*android:color/system_neutral1_50</color>\n")
            .append("    <color name=\"background_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_device_default_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"background_floating_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_floating_device_default_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"background_floating_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_floating_material_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"background_leanback_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_material_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"BottomBarBackground\">@*android:color/background_material_light</color>\n")
            .append("    <color name=\"button_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"floating_popup_divider_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"GM2_grey_800\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"holo_light_button_normal\">@*android:color/background_material_light</color>\n")
            .append("    <color name=\"holo_primary_dark\">@*android:color/background_material_dark</color>\n")
            .append("    <color name=\"list_divider_color_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_blue_grey_800\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_blue_grey_900\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_blue_grey_950\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_grey_800\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_grey_850\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_grey_900\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_device_default_settings\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_material_settings\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_device_default_settings\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_material_settings\">@*android:color/background_dark</color>\n")
            .append("</resources>\n")

        resourcesNight
            .append("    <color name=\"holo_blue_light\">${colorToHex(palette[1][0][5] as Int)}</color>\n")
            .append("    <color name=\"holo_blue_dark\">${colorToHex(palette[1][2][5] as Int)}</color>\n")
            .append("    <color name=\"holo_green_light\">${colorToHex(palette[1][0][5] as Int)}</color>\n")
            .append("    <color name=\"holo_green_dark\">${colorToHex(palette[1][2][5] as Int)}</color>\n")
            .append("    <color name=\"accent_device_default\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_device_default_dark\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_device_default_light\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_material_dark\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_material_light\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_primary_device_default\">@*android:color/holo_blue_light</color>\n")
            .append("    <color name=\"accent_secondary_device_default\">@*android:color/system_accent2_300</color>\n")
            .append("    <color name=\"accent_tertiary_device_default\">@*android:color/system_accent3_300</color>\n")
            .append("    <color name=\"autofill_background_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_dark\">@*android:color/system_neutral1_900</color>\n")
            .append("    <color name=\"background_light\">@*android:color/system_neutral1_50</color>\n")
            .append("    <color name=\"background_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_device_default_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"background_floating_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_floating_device_default_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"background_floating_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_floating_material_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"background_leanback_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"background_material_light\">@*android:color/background_light</color>\n")
            .append("    <color name=\"BottomBarBackground\">@*android:color/background_material_light</color>\n")
            .append("    <color name=\"button_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"floating_popup_divider_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"GM2_grey_800\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"holo_light_button_normal\">@*android:color/background_material_light</color>\n")
            .append("    <color name=\"holo_primary_dark\">@*android:color/background_material_dark</color>\n")
            .append("    <color name=\"list_divider_color_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_blue_grey_800\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_blue_grey_900\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_blue_grey_950\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_grey_800\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_grey_850\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"material_grey_900\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_device_default_settings\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_dark_material_settings\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_device_default_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_device_default_settings\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_material_dark\">@*android:color/background_dark</color>\n")
            .append("    <color name=\"primary_material_settings\">@*android:color/background_dark</color>\n")
            .append("</resources>\n")

        return MonetCompiler.buildOverlay(
            arrayOf(resources.toString(), resourcesNight.toString()),
            force
        )
    }
}
