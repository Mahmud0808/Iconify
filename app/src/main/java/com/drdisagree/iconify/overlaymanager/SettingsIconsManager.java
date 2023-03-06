package com.drdisagree.iconify.overlaymanager;

import com.drdisagree.iconify.utils.compiler.SettingsIconsCompiler;

import java.io.IOException;

public class SettingsIconsManager {

    public static boolean enableOverlay(int iconSet, int backgroundStyle, int iconColor) throws IOException {
        String resources = "";

        if (iconSet >= 1 && iconSet <= 4) {
            resources += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<resources>\n" +
                    "    <color name=\"holo_blue_light\">@*android:color/holo_blue_light</color>\n" +
                    "    <color name=\"holo_green_light\">@*android:color/holo_green_light</color>\n" +
                    "    <dimen name=\"dashboard_tile_foreground_image_inset\">-1.0dip</dimen>\n" +
                    "    <dimen name=\"dashboard_tile_foreground_image_size\">20.0dip</dimen>\n" +
                    "    <dimen name=\"dashboard_tile_image_size\">38.0dip</dimen>\n";

            switch (backgroundStyle) {
                case 2:
                case 3:
                case 4:
                    resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.5</item>\n";
                    break;
                case 1:
                    resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.65</item>\n";
                    break;
            }

            switch (backgroundStyle) {
                case 1:
                    resources += "    <color name=\"bg_end_color\">#00000000</color>\n" +
                            "    <color name=\"bg_start_color\">#00000000</color>\n" +
                            "    <color name=\"outline_color\">#00000000</color>\n" +
                            "    <color name=\"outline_end_color\">#00000000</color>\n" +
                            "    <color name=\"outline_start_color\">#00000000</color>\n" +
                            "    <dimen name=\"bg_inset\">0.0dip</dimen>\n" +
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n" +
                            "    <dimen name=\"bg_roundness\">@*android:dimen/harmful_app_name_padding_top</dimen>\n" +
                            "    <dimen name=\"outline_roundness\">@*android:dimen/harmful_app_name_padding_top</dimen>\n";
                    break;
                case 2:
                    resources += "    <color name=\"bg_end_color\">@color/holo_green_light</color>\n" +
                            "    <color name=\"bg_start_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_color\">#00000000</color>\n" +
                            "    <color name=\"outline_end_color\">#00000000</color>\n" +
                            "    <color name=\"outline_start_color\">#00000000</color>\n" +
                            "    <dimen name=\"bg_inset\">0.0dip</dimen>\n" +
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n" +
                            "    <dimen name=\"bg_roundness\">@*android:dimen/harmful_app_name_padding_top</dimen>\n" +
                            "    <dimen name=\"outline_roundness\">@*android:dimen/harmful_app_name_padding_top</dimen>\n";
                    break;
                case 3:
                    resources += "    <color name=\"bg_end_color\">?android:colorBackground</color>\n" +
                            "    <color name=\"bg_start_color\">?android:colorBackground</color>\n" +
                            "    <color name=\"outline_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_end_color\">@color/holo_green_light</color>\n" +
                            "    <color name=\"outline_start_color\">@color/holo_blue_light</color>\n" +
                            "    <dimen name=\"bg_inset\">2.0dip</dimen>\n" +
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n" +
                            "    <dimen name=\"bg_roundness\">@*android:dimen/harmful_app_name_padding_right</dimen>\n" +
                            "    <dimen name=\"outline_roundness\">@*android:dimen/harmful_app_name_padding_top</dimen>\n";
                    break;
                case 4:
                    resources += "    <color name=\"bg_end_color\">@color/holo_green_light</color>\n" +
                            "    <color name=\"bg_start_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_end_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_start_color\">@color/holo_green_light</color>\n" +
                            "    <dimen name=\"bg_inset\">4.0dip</dimen>\n" +
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n" +
                            "    <dimen name=\"bg_roundness\">@*android:dimen/harmful_app_name_padding_left</dimen>\n" +
                            "    <dimen name=\"outline_roundness\">@*android:dimen/harmful_app_name_padding_top</dimen>\n";
                    break;
            }

            switch (iconColor) {
                case 1:
                    resources += "    <color name=\"icon_tint\">?android:textColorPrimary</color>\n";
                    break;
                case 2:
                    resources += "    <color name=\"icon_tint\">?android:textColorPrimaryInverse</color>\n";
                    break;
                case 3:
                    resources += "    <color name=\"icon_tint\">@*android:color/holo_blue_light</color>\n";
                    break;
            }

            resources += "</resources>";
        }

        return SettingsIconsCompiler.buildOverlay(iconSet, backgroundStyle, resources);
    }
}