package com.drdisagree.iconify.utils.overlay.manager;

import com.topjohnwu.superuser.Shell;

public class QsResourceManager {

    public static void replaceResourcesIfRequired(String source, String name) {
        String resourceLight, resourceNight;

        if (name.startsWith("QSSN")) {
            resourceLight = resourceQsTileNormalLight;
            resourceNight = resourceQsTileNormalNight;
        } else if (name.startsWith("QSSP")) {
            resourceLight = resourceQsTilePixel;
            resourceNight = resourceQsTilePixel;
        } else if (name.startsWith("QSNT")) {
            if (name.startsWith("QSNT1")) {
                resourceLight = resourceQsTextNormal1;
                resourceNight = resourceQsTextNormal1;
            } else if (name.startsWith("QSNT2")) {
                resourceLight = resourceQsTextNormal2;
                resourceNight = resourceQsTextNormal2;
            } else if (name.startsWith("QSNT3")) {
                resourceLight = resourceQsTextNormalLight3;
                resourceNight = resourceQsTextNormalNight3;
            } else if (name.startsWith("QSNT4")) {
                resourceLight = resourceQsTextNormalLight4;
                resourceNight = resourceQsTextNormalNight4;
            } else {
                return;
            }
        } else if (name.startsWith("QSPT")) {
            if (name.startsWith("QSPT1")) {
                resourceLight = resourceQsTextPixel1;
                resourceNight = resourceQsTextPixel1;
            } else if (name.startsWith("QSPT2")) {
                resourceLight = resourceQsTextPixel2;
                resourceNight = resourceQsTextPixel2;
            } else if (name.startsWith("QSPT3")) {
                resourceLight = resourceQsTextPixelLight3;
                resourceNight = resourceQsTextPixelNight3;
            } else if (name.startsWith("QSPT4")) {
                resourceLight = resourceQsTextPixelLight4;
                resourceNight = resourceQsTextPixelNight4;
            } else {
                return;
            }
        } else {
            return;
        }

        String replaceStart = "<style name=\"Theme.SystemUI.QuickSettings\"";
        String replaceEnd = "<color name=\"control_primary_text\">#e6(f{6}|000000)<\\/color>";

        String command1 = "sed -i -E '/" + replaceStart + "/,/" + replaceEnd + "/ c\\" +
                resourceLight + "' '" + source + "/res/values/iconify.xml'";
        String command2 = "sed -i -E '/" + replaceStart + "/,/" + replaceEnd + "/ c\\" +
                resourceNight + "' '" + source + "/res/values-night/iconify.xml'";

        Shell.cmd(command1, command2).exec();
    }

    private static final String resourceQsTileNormalLight = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="@*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTileNormalNight = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="@*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTilePixel = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_green_dark<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="@*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextNormal1 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">#BFFFFFFF<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextNormal2 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextNormalLight3 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FF000000<\\/item>\
                    <item name="android:textColorSecondaryInverse">#BF000000<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextNormalNight3 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">#BFFFFFFF<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextNormalLight4 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FF000000<\\/item>\
                    <item name="android:textColorSecondaryInverse">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextNormalNight4 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault.SystemUI">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_blue_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_blue_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextPixel1 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">#BFFFFFFF<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_green_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextPixel2 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_green_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextPixelLight3 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FF000000<\\/item>\
                    <item name="android:textColorSecondaryInverse">#BF000000<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_green_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextPixelNight3 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">#BFFFFFFF<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_green_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextPixelLight4 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FF000000<\\/item>\
                    <item name="android:textColorSecondaryInverse">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_green_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";

    private static final String resourceQsTextPixelNight4 = """
            <style name="Theme.SystemUI.QuickSettings" parent="@*android:style\\/Theme.DeviceDefault">\
                    <item name="android:colorBackground">?android:colorBackground<\\/item>\
                    <item name="android:textColorPrimaryInverse">#FFFFFFFF<\\/item>\
                    <item name="android:textColorSecondaryInverse">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:windowIsFloating">true<\\/item>\
                    <item name="android:colorPrimary">@*android:color\\/holo_green_light<\\/item>\
                    <item name="android:colorAccent">#FFFFFFFF<\\/item>\
                    <item name="android:colorSecondary">@*android:color\\/holo_green_dark<\\/item>\
                    <item name="*android:colorAccentPrimary">#FFFFFFFF<\\/item>\
                <\\/style>""";
}
