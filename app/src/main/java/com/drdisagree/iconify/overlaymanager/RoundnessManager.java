package com.drdisagree.iconify.overlaymanager;

import com.drdisagree.iconify.utils.compilerutil.RoundnessCompilerUtil;

import java.io.IOException;

public class RoundnessManager {

    public static boolean enable_roundness(int n) throws IOException {
        int cornerRadius = n + 8;

        String resources = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n" +
                "    <dimen name=\"config_buttonCornerRadius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"config_dialogCornerRadius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"control_corner_material\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"corner_size\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"dialog_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"tooltip_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"config_progressBarCornerRadius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"progress_bar_corner_material\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_right\">" + (cornerRadius - 2) + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_left\">" + (cornerRadius - 4) + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_bottom\">" + (cornerRadius - 6) + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_top\">" + cornerRadius + "dip</dimen>\n" +
                "</resources>\n";

        return RoundnessCompilerUtil.buildOverlay(resources);
    }
}