package com.drdisagree.iconify.utils.overlay.manager

import com.drdisagree.iconify.utils.overlay.compiler.RoundnessCompiler
import java.io.IOException
import kotlin.math.max

object RoundnessManager {

    @JvmStatic
    @Throws(IOException::class)
    fun buildOverlay(cornerRadius: Int, force: Boolean): Boolean {
        val frameworkResources = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="config_buttonCornerRadius">${cornerRadius}dip</dimen>
    <dimen name="config_dialogCornerRadius">${cornerRadius}dip</dimen>
    <dimen name="control_corner_material">${cornerRadius}dip</dimen>
    <dimen name="corner_size">${cornerRadius}dip</dimen>
    <dimen name="dialog_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="tooltip_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="config_progressBarCornerRadius">${cornerRadius}dip</dimen>
    <dimen name="progress_bar_corner_material">${cornerRadius}dip</dimen>
    <dimen name="harmful_app_name_padding_top">${cornerRadius}dip</dimen>
    <dimen name="harmful_app_name_padding_right">${
            max(
                (cornerRadius - 2).toDouble(),
                0.0
            )
        }dip</dimen>
    <dimen name="harmful_app_name_padding_left">${
            max(
                (cornerRadius - 4).toDouble(),
                0.0
            )
        }dip</dimen>
    <dimen name="harmful_app_name_padding_bottom">${
            max(
                (cornerRadius - 6).toDouble(),
                0.0
            )
        }dip</dimen>
</resources>
""".trimIndent()

        val systemuiResources = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="global_actions_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="notification_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="notification_scrim_corner_radius">${cornerRadius + 4}dip</dimen>
    <dimen name="ongoing_appops_chip_bg_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="ongoing_call_chip_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="qs_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="qs_footer_action_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="qs_security_footer_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="rounded_slider_background_rounded_corner">${cornerRadius + 4}dip</dimen>
    <dimen name="rounded_slider_corner_radius">${cornerRadius}dip</dimen>
    <dimen name="settingslib_dialogCornerRadius">${cornerRadius}dip</dimen>
    <dimen name="split_divider_corner_size">${cornerRadius}dip</dimen>
    <dimen name="volume_dialog_panel_width_half">${cornerRadius}dip</dimen>
    <dimen name="volume_ringer_drawer_item_size_half">${
            max(
                (cornerRadius - 7).toDouble(),
                0.0
            )
        }dip</dimen>
    <dimen name="volume_dialog_slider_corner_radius">${
            max(
                (cornerRadius - 7).toDouble(),
                0.0
            )
        }dip</dimen>
    <dimen name="volume_dialog_track_corner_radius">${
            max(
                (cornerRadius - 7).toDouble(),
                0.0
            )
        }dip</dimen>
    <dimen name="abc_star_small">${
            max(
                (cornerRadius - 11).toDouble(),
                0.0
            )
        }dip</dimen>
</resources>
""".trimIndent()

        return RoundnessCompiler.buildOverlay(arrayOf(frameworkResources, systemuiResources), force)
    }
}