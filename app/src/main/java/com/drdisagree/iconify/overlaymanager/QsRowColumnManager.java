package com.drdisagree.iconify.overlaymanager;

import com.drdisagree.iconify.utils.QsRowColumnCompilerUtil;

import java.io.IOException;

public class QsRowColumnManager {

    public static boolean buildOverlay(int portQqsRow, int portQsRow, int portQsColumn, int landQqsRow, int landQsRow, int landQsColumn) throws IOException {
        String resourcesPortrait = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <integer name=\"quick_qs_panel_max_rows\">" + portQqsRow + "</integer>\n" +
                "    <integer name=\"quick_qs_panel_max_tiles\">" + (portQqsRow * portQsColumn) + "</integer>\n" +
                "    <integer name=\"quick_settings_max_rows\">" + portQsRow + "</integer>\n" +
                "    <integer name=\"quick_settings_num_columns\">" + portQsColumn + "</integer>\n" +
                "    <integer name=\"quick_settings_min_num_tiles\">" + (portQqsRow * portQsColumn) + "</integer>\n" +
                "</resources>";

        String resourcesLanscape = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <integer name=\"quick_qs_panel_max_rows\">" + landQqsRow + "</integer>\n" +
                "    <integer name=\"quick_qs_panel_max_tiles\">" + (landQqsRow * landQsColumn) + "</integer>\n" +
                "    <integer name=\"quick_settings_max_rows\">" + landQsRow + "</integer>\n" +
                "    <integer name=\"quick_settings_num_columns\">" + landQsColumn + "</integer>\n" +
                "    <integer name=\"quick_settings_min_num_tiles\">" + (landQqsRow * landQsColumn) + "</integer>\n" +
                "</resources>";

        return QsRowColumnCompilerUtil.buildOverlay(new String[]{resourcesPortrait, resourcesLanscape});
    }
}