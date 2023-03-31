package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.Preferences.USE_LIGHT_ACCENT;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.ColorUtil;
import com.drdisagree.iconify.utils.compiler.MonetCompiler;

import java.io.IOException;
import java.util.List;

public class MonetEngineManager {

    public static boolean enableOverlay(List<List<Object>> palette, List<List<Object>> palette_night) throws IOException {
        String[][] colors = ColorUtil.getColorNames();

        StringBuilder resources = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
        StringBuilder resources_night = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");

        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                resources.append("    <color name=\"").append(colors[i][j]).append("\">").append(ColorUtil.ColorToHex((int) palette.get(i).get(j), false, true)).append("</color>\n");
                resources_night.append("    <color name=\"").append(colors[i][j]).append("\">").append(ColorUtil.ColorToHex((int) palette_night.get(i).get(j), false, true)).append("</color>\n");
            }
        }

        resources.append("    <color name=\"holo_blue_light\">").append(ColorUtil.ColorToHex((int) palette.get(0).get(Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? 5 : 8), false, true)).append("</color>\n");
        resources.append("    <color name=\"holo_green_light\">").append(ColorUtil.ColorToHex((int) palette.get(2).get(Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? 5 : 8), false, true)).append("</color>\n");
        resources.append("    <color name=\"holo_blue_dark\">").append(ColorUtil.ColorToHex((int) palette.get(1).get(10), false, true)).append("</color>\n");
        resources.append("    <color name=\"accent_device_default\">@*android:color/holo_blue_light</color>\n");
        resources.append("    <color name=\"accent_device_default_dark\">@*android:color/holo_blue_light</color>\n");
        resources.append("    <color name=\"accent_device_default_light\">@*android:color/holo_blue_light</color>\n");
        resources.append("    <color name=\"accent_material_dark\">@*android:color/holo_blue_light</color>\n");
        resources.append("    <color name=\"accent_material_light\">@*android:color/holo_blue_light</color>\n");
        resources.append("    <color name=\"accent_primary_device_default\">@*android:color/holo_blue_light</color>\n");
        resources.append("    <color name=\"accent_secondary_device_default\">@*android:color/system_accent2_").append(Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? 300 : 600).append("</color>\n");
        resources.append("    <color name=\"accent_tertiary_device_default\">@*android:color/system_accent3_").append(Prefs.getBoolean(USE_LIGHT_ACCENT, false) ? 300 : 600).append("</color>\n");
        resources.append("    <color name=\"autofill_background_material_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"background_dark\">@*android:color/system_neutral1_900</color>\n");
        resources.append("    <color name=\"background_light\">@*android:color/system_neutral1_50</color>\n");
        resources.append("    <color name=\"background_device_default_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"background_device_default_light\">@*android:color/background_light</color>\n");
        resources.append("    <color name=\"background_floating_device_default_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"background_floating_device_default_light\">@*android:color/background_light</color>\n");
        resources.append("    <color name=\"background_floating_material_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"background_floating_material_light\">@*android:color/background_light</color>\n");
        resources.append("    <color name=\"background_leanback_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"background_material_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"background_material_light\">@*android:color/background_light</color>\n");
        resources.append("    <color name=\"BottomBarBackground\">@*android:color/background_material_light</color>\n");
        resources.append("    <color name=\"button_material_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"floating_popup_divider_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"GM2_grey_800\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"holo_light_button_normal\">@*android:color/background_material_light</color>\n");
        resources.append("    <color name=\"holo_primary_dark\">@*android:color/background_material_dark</color>\n");
        resources.append("    <color name=\"list_divider_color_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"material_blue_grey_800\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"material_blue_grey_900\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"material_blue_grey_950\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"material_grey_800\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"material_grey_850\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"material_grey_900\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_dark_device_default_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_dark_device_default_settings\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_dark_material_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_dark_material_settings\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_device_default_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_device_default_settings\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_material_dark\">@*android:color/background_dark</color>\n");
        resources.append("    <color name=\"primary_material_settings\">@*android:color/background_dark</color>\n");
        resources.append("</resources>\n");

        resources_night.append("    <color name=\"holo_blue_light\">").append(ColorUtil.ColorToHex((int) palette_night.get(0).get(5), false, true)).append("</color>\n");
        resources_night.append("    <color name=\"holo_green_light\">").append(ColorUtil.ColorToHex((int) palette_night.get(2).get(5), false, true)).append("</color>\n");
        resources_night.append("    <color name=\"holo_blue_dark\">").append(ColorUtil.ColorToHex((int) palette_night.get(1).get(10), false, true)).append("</color>\n");
        resources_night.append("    <color name=\"accent_device_default\">@*android:color/holo_blue_light</color>\n");
        resources_night.append("    <color name=\"accent_device_default_dark\">@*android:color/holo_blue_light</color>\n");
        resources_night.append("    <color name=\"accent_device_default_light\">@*android:color/holo_blue_light</color>\n");
        resources_night.append("    <color name=\"accent_material_dark\">@*android:color/holo_blue_light</color>\n");
        resources_night.append("    <color name=\"accent_material_light\">@*android:color/holo_blue_light</color>\n");
        resources_night.append("    <color name=\"accent_primary_device_default\">@*android:color/holo_blue_light</color>\n");
        resources_night.append("    <color name=\"accent_secondary_device_default\">@*android:color/system_accent2_300</color>\n");
        resources_night.append("    <color name=\"accent_tertiary_device_default\">@*android:color/system_accent3_300</color>\n");
        resources_night.append("    <color name=\"autofill_background_material_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"background_dark\">@*android:color/system_neutral1_900</color>\n");
        resources_night.append("    <color name=\"background_light\">@*android:color/system_neutral1_50</color>\n");
        resources_night.append("    <color name=\"background_device_default_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"background_device_default_light\">@*android:color/background_light</color>\n");
        resources_night.append("    <color name=\"background_floating_device_default_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"background_floating_device_default_light\">@*android:color/background_light</color>\n");
        resources_night.append("    <color name=\"background_floating_material_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"background_floating_material_light\">@*android:color/background_light</color>\n");
        resources_night.append("    <color name=\"background_leanback_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"background_material_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"background_material_light\">@*android:color/background_light</color>\n");
        resources_night.append("    <color name=\"BottomBarBackground\">@*android:color/background_material_light</color>\n");
        resources_night.append("    <color name=\"button_material_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"floating_popup_divider_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"GM2_grey_800\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"holo_light_button_normal\">@*android:color/background_material_light</color>\n");
        resources_night.append("    <color name=\"holo_primary_dark\">@*android:color/background_material_dark</color>\n");
        resources_night.append("    <color name=\"list_divider_color_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"material_blue_grey_800\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"material_blue_grey_900\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"material_blue_grey_950\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"material_grey_800\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"material_grey_850\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"material_grey_900\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_dark_device_default_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_dark_device_default_settings\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_dark_material_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_dark_material_settings\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_device_default_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_device_default_settings\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_material_dark\">@*android:color/background_dark</color>\n");
        resources_night.append("    <color name=\"primary_material_settings\">@*android:color/background_dark</color>\n");
        resources_night.append("</resources>\n");

        return MonetCompiler.buildOverlay(new String[]{resources.toString(), resources_night.toString()});
    }
}
