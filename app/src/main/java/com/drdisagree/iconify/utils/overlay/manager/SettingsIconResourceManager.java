package com.drdisagree.iconify.utils.overlay.manager;

import com.drdisagree.iconify.utils.compiler.SettingsIconsCompiler;

import java.io.IOException;

public class SettingsIconResourceManager {

    public static boolean buildOverlay(int iconSet, int backgroundStyle, int backgroundShape, int iconSize, int iconColor, boolean force) throws IOException {
        String resources = "";

        if (iconSet >= 1 && iconSet <= 4) {
            resources += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<resources>\n" +
                    "    <color name=\"holo_blue_light\">@*android:color/holo_blue_light</color>\n" +
                    "    <color name=\"holo_blue_dark\">@*android:color/holo_blue_dark</color>\n" +
                    "    <dimen name=\"dashboard_tile_foreground_image_inset\">-1.0dip</dimen>\n" +
                    "    <dimen name=\"dashboard_tile_foreground_image_size\">20.0dip</dimen>\n" +
                    "    <dimen name=\"dashboard_tile_image_size\">38.0dip</dimen>\n";

            switch (iconSize) {
                case 1:
                    resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.5</item>\n";
                    break;
                case 2:
                    resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.65</item>\n";
                    break;
                case 3:
                    resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.8</item>\n";
                    break;
                case 4:
                    resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">1.0</item>\n";
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
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n";
                    break;
                case 2:
                    resources += "    <color name=\"bg_end_color\">@color/holo_blue_dark</color>\n" +
                            "    <color name=\"bg_start_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_color\">#00000000</color>\n" +
                            "    <color name=\"outline_end_color\">#00000000</color>\n" +
                            "    <color name=\"outline_start_color\">#00000000</color>\n" +
                            "    <dimen name=\"bg_inset\">0.0dip</dimen>\n" +
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n";
                    break;
                case 3:
                    resources += "    <color name=\"bg_end_color\">?android:colorBackground</color>\n" +
                            "    <color name=\"bg_start_color\">?android:colorBackground</color>\n" +
                            "    <color name=\"outline_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_end_color\">@color/holo_blue_dark</color>\n" +
                            "    <color name=\"outline_start_color\">@color/holo_blue_light</color>\n" +
                            "    <dimen name=\"bg_inset\">2.0dip</dimen>\n" +
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n";
                    break;
                case 4:
                    resources += "    <color name=\"bg_end_color\">@color/holo_blue_dark</color>\n" +
                            "    <color name=\"bg_start_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_end_color\">@color/holo_blue_light</color>\n" +
                            "    <color name=\"outline_start_color\">@color/holo_blue_dark</color>\n" +
                            "    <dimen name=\"bg_inset\">4.0dip</dimen>\n" +
                            "    <dimen name=\"bg_inset2\">0.0dip</dimen>\n";
                    break;
            }

            switch (backgroundShape) {
                case 1:
                    resources += "    <dimen name=\"bg_roundness\">48.0dip</dimen>\n" +
                            "    <dimen name=\"outline_roundness\">48.0dip</dimen>\n";
                    break;
                case 2:
                    switch (backgroundStyle) {
                        case 3:
                            resources += "    <dimen name=\"bg_roundness\">12.0dip</dimen>\n" +
                                    "    <dimen name=\"outline_roundness\">14.0dip</dimen>\n";
                            break;
                        case 4:
                            resources += "    <dimen name=\"bg_roundness\">10.0dip</dimen>\n" +
                                    "    <dimen name=\"outline_roundness\">14.0dip</dimen>\n";
                            break;
                        default:
                            resources += "    <dimen name=\"bg_roundness\">14.0dip</dimen>\n" +
                                    "    <dimen name=\"outline_roundness\">14.0dip</dimen>\n";
                            break;
                    }
                    break;
                case 3:
                    switch (backgroundStyle) {
                        case 3:
                            resources += "    <dimen name=\"bg_roundness\">6.0dip</dimen>\n" +
                                    "    <dimen name=\"outline_roundness\">8.0dip</dimen>\n";
                            break;
                        case 4:
                            resources += "    <dimen name=\"bg_roundness\">4.0dip</dimen>\n" +
                                    "    <dimen name=\"outline_roundness\">8.0dip</dimen>\n";
                            break;
                        default:
                            resources += "    <dimen name=\"bg_roundness\">8.0dip</dimen>\n" +
                                    "    <dimen name=\"outline_roundness\">8.0dip</dimen>\n";
                            break;
                    }
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

        return SettingsIconsCompiler.buildOverlay(iconSet, backgroundStyle, resources, force);
    }
}