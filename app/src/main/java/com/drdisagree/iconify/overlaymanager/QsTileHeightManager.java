package com.drdisagree.iconify.overlaymanager;

import com.drdisagree.iconify.utils.compiler.QsTileHeightCompiler;

import java.io.IOException;

public class QsTileHeightManager {

    public static boolean enableOverlay(int portNonex, int portEx, int landNonex, int landEx) throws IOException {
        String resourcesPortrait = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "   <dimen name=\"qs_tile_height\">" + portEx + "dip</dimen>\n" +
                "   <dimen name=\"qs_quick_tile_size\">" + portNonex + "dip</dimen>\n" +
                "</resources>\n";

        String resourcesLanscape = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "   <dimen name=\"qs_tile_height\">" + landEx + "dip</dimen>\n" +
                "   <dimen name=\"qs_quick_tile_size\">" + landNonex + "dip</dimen>\n" +
                "</resources>\n";

        return QsTileHeightCompiler.buildOverlay(new String[]{resourcesPortrait, resourcesLanscape});
    }
}