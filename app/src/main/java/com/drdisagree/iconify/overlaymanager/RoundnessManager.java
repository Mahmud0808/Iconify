package com.drdisagree.iconify.overlaymanager;

import com.drdisagree.iconify.utils.compiler.RoundnessCompiler;

import java.io.IOException;

public class RoundnessManager {

    public static boolean enableOverlay(int cornerRadius) throws IOException {

        String framework_resources = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n" +
                "    <dimen name=\"config_buttonCornerRadius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"config_dialogCornerRadius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"control_corner_material\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"corner_size\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"dialog_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"tooltip_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"config_progressBarCornerRadius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"progress_bar_corner_material\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_top\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_right\">" + (cornerRadius - 2) + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_left\">" + (cornerRadius - 4) + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_bottom\">" + (cornerRadius - 6) + "dip</dimen>\n" +
                "</resources>\n";

        String sysui_resources = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <dimen name=\"global_actions_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"notification_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"notification_scrim_corner_radius\">" + (cornerRadius + 4) + "dip</dimen>\n" +
                "    <dimen name=\"ongoing_appops_chip_bg_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"ongoing_call_chip_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"qs_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"qs_footer_action_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"qs_security_footer_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"rounded_slider_background_rounded_corner\">" + (cornerRadius + 4) + "dip</dimen>\n" +
                "    <dimen name=\"rounded_slider_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"settingslib_dialogCornerRadius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"split_divider_corner_size\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"volume_dialog_panel_width_half\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"volume_ringer_drawer_item_size_half\">" + (cornerRadius - 7) + "dip</dimen>\n" +
                "    <dimen name=\"volume_dialog_slider_corner_radius\">" + (cornerRadius - 7) + "dip</dimen>\n" +
                "    <dimen name=\"volume_dialog_track_corner_radius\">" + (cornerRadius - 7) + "dip</dimen>\n" +
                "    <dimen name=\"abc_star_small\">" + (cornerRadius - 11) + "dip</dimen>\n" +
                "</resources>";

        return RoundnessCompiler.buildOverlay(new String[]{framework_resources, sysui_resources});
    }
}