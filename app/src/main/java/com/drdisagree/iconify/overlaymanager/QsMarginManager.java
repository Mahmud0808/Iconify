package com.drdisagree.iconify.overlaymanager;

import com.drdisagree.iconify.utils.compiler.QsMarginCompiler;

import java.io.IOException;

public class QsMarginManager {

    public static boolean enableOverlay(int portQqs, int portQs, int landQqs, int landQs, boolean enable) throws IOException {
        String resourcesPortraitFramework = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "   <dimen name=\"quick_qs_offset_height\">" + portQqs + "dip</dimen>\n" +
                "   <dimen name=\"quick_qs_total_height\">" + portQs + "dip</dimen>\n" +
                "</resources>\n";

        String resourcesLanscapeFramework = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "   <dimen name=\"quick_qs_offset_height\">" + landQqs + "dip</dimen>\n" +
                "   <dimen name=\"quick_qs_total_height\">" + landQs + "dip</dimen>\n" +
                "</resources>\n";

        String resourcesPortraitSystemui = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "   <dimen name=\"qqs_layout_margin_top\">" + portQqs + "dip</dimen>\n" +
                "   <dimen name=\"qs_header_row_min_height\">" + portQqs + "dip</dimen>\n" +
                "   <dimen name=\"qs_panel_padding_top\">" + portQs + "dip</dimen>\n" +
                "   <dimen name=\"qs_panel_padding_top_combined_headers\">" + portQs + "dip</dimen>\n" +
                "</resources>\n";

        String resourcesLanscapeSystemui = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "   <dimen name=\"qqs_layout_margin_top\">" + landQqs + "dip</dimen>\n" +
                "   <dimen name=\"qs_header_row_min_height\">" + landQqs + "dip</dimen>\n" +
                "   <dimen name=\"qs_panel_padding_top\">" + landQs + "dip</dimen>\n" +
                "   <dimen name=\"qs_panel_padding_top_combined_headers\">" + landQs + "dip</dimen>\n" +
                "</resources>\n";

        return QsMarginCompiler.buildOverlay(new Object[]{new String[]{resourcesPortraitFramework, resourcesLanscapeFramework}, new String[]{resourcesPortraitSystemui, resourcesLanscapeSystemui}}, enable);
    }
}