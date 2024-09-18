package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.utils.overlay.compiler.SettingsIconsCompiler.buildOverlay
import java.io.IOException

object SettingsIconResourceManager {

    @Throws(IOException::class)
    fun buildOverlay(
        iconSet: Int,
        backgroundStyle: Int,
        backgroundShape: Int,
        iconSize: Int,
        iconColor: Int,
        force: Boolean
    ): Boolean {
        var resources = ""

        if (iconSet in 1..4) {
            resources += """
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <color name="holo_blue_light">@*android:color/holo_blue_light</color>
                        <color name="holo_blue_dark">@*android:color/holo_blue_dark</color>
                        <dimen name="dashboard_tile_foreground_image_inset">-1.0dip</dimen>
                        <dimen name="dashboard_tile_foreground_image_size">20.0dip</dimen>
                        <dimen name="dashboard_tile_image_size">38.0dip</dimen>
                    
                    """.trimIndent()

            when (iconSize) {
                1 -> resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.5</item>\n"
                2 -> resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.65</item>\n"
                3 -> resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">0.8</item>\n"
                4 -> resources += "    <item name=\"icon_scale\" format=\"float\" type=\"dimen\">1.0</item>\n"
            }

            when (backgroundStyle) {
                1 -> resources += """
                            <color name="bg_end_color">#00000000</color>
                            <color name="bg_start_color">#00000000</color>
                            <color name="outline_color">#00000000</color>
                            <color name="outline_end_color">#00000000</color>
                            <color name="outline_start_color">#00000000</color>
                            <dimen name="bg_inset">0.0dip</dimen>
                            <dimen name="bg_inset2">0.0dip</dimen>
                        
                        """.trimIndent()

                2 -> resources += """
                            <color name="bg_end_color">@color/holo_blue_dark</color>
                            <color name="bg_start_color">@color/holo_blue_light</color>
                            <color name="outline_color">#00000000</color>
                            <color name="outline_end_color">#00000000</color>
                            <color name="outline_start_color">#00000000</color>
                            <dimen name="bg_inset">0.0dip</dimen>
                            <dimen name="bg_inset2">0.0dip</dimen>
                        
                        """.trimIndent()

                3 -> resources += """
                            <color name="bg_end_color">?android:colorBackground</color>
                            <color name="bg_start_color">?android:colorBackground</color>
                            <color name="outline_color">@color/holo_blue_light</color>
                            <color name="outline_end_color">@color/holo_blue_dark</color>
                            <color name="outline_start_color">@color/holo_blue_light</color>
                            <dimen name="bg_inset">2.0dip</dimen>
                            <dimen name="bg_inset2">0.0dip</dimen>
                        
                        """.trimIndent()

                4 -> resources += """
                            <color name="bg_end_color">@color/holo_blue_dark</color>
                            <color name="bg_start_color">@color/holo_blue_light</color>
                            <color name="outline_color">@color/holo_blue_light</color>
                            <color name="outline_end_color">@color/holo_blue_light</color>
                            <color name="outline_start_color">@color/holo_blue_dark</color>
                            <dimen name="bg_inset">4.0dip</dimen>
                            <dimen name="bg_inset2">0.0dip</dimen>
                        
                        """.trimIndent()
            }

            when (backgroundShape) {
                1 -> resources += """
                            <dimen name="bg_roundness">48.0dip</dimen>
                            <dimen name="outline_roundness">48.0dip</dimen>
                        
                        """.trimIndent()

                2 -> {
                    resources += when (backgroundStyle) {
                        3 -> """
                                    <dimen name="bg_roundness">12.0dip</dimen>
                                    <dimen name="outline_roundness">14.0dip</dimen>
                                
                                """.trimIndent()

                        4 -> """
                                    <dimen name="bg_roundness">10.0dip</dimen>
                                    <dimen name="outline_roundness">14.0dip</dimen>
                                
                                """.trimIndent()

                        else -> """
                                    <dimen name="bg_roundness">14.0dip</dimen>
                                    <dimen name="outline_roundness">14.0dip</dimen>
                                
                                """.trimIndent()
                    }
                }

                3 -> {
                    resources += when (backgroundStyle) {
                        3 -> """
                                    <dimen name="bg_roundness">6.0dip</dimen>
                                    <dimen name="outline_roundness">8.0dip</dimen>
                                
                                """.trimIndent()

                        4 -> """
                                    <dimen name="bg_roundness">4.0dip</dimen>
                                    <dimen name="outline_roundness">8.0dip</dimen>
                                
                                """.trimIndent()

                        else -> """
                                    <dimen name="bg_roundness">8.0dip</dimen>
                                    <dimen name="outline_roundness">8.0dip</dimen>
                                
                                """.trimIndent()
                    }
                }
            }

            when (iconColor) {
                1 -> resources += "    <color name=\"icon_tint\">?android:textColorPrimary</color>\n"
                2 -> resources += "    <color name=\"icon_tint\">?android:textColorPrimaryInverse</color>\n"
                3 -> resources += "    <color name=\"icon_tint\">@*android:color/holo_blue_light</color>\n"
            }

            resources += "</resources>"
        }

        return buildOverlay(iconSet, backgroundStyle, resources, force)
    }
}