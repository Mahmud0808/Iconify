package com.drdisagree.iconify.utils.overlay;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OverlayUtil {

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponent' | sed -E 's/^....//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^.x..IconifyComponent' | sed -E 's/^.x..//'").exec().getOut();
    }

    public static List<String> getDisabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^. ..IconifyComponent' | sed -E 's/^. ..//'").exec().getOut();
    }

    public static boolean isOverlayEnabled(String pkgName) {
        return Shell.cmd("[[ $(cmd overlay list | grep -o '\\[x\\] " + pkgName + "') ]] && echo 1 || echo 0").exec().getOut().get(0).equals("1");
    }

    public static boolean isOverlayDisabled(String pkgName) {
        return Shell.cmd("[[ $(cmd overlay list | grep -o '\\[ \\] " + pkgName + "') ]] && echo 1 || echo 0").exec().getOut().get(0).equals("1");
    }

    static boolean isOverlayInstalled(List<String> enabledOverlays, String pkgName) {
        for (String line : enabledOverlays) {
            if (line.equals(pkgName)) return true;
        }
        return false;
    }

    public static void enableOverlay(String pkgName) {
        Prefs.putBoolean(pkgName, true);
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName + " highest").submit();
    }

    public static void enableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, true);
            command.append("cmd overlay enable --user current ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void enableOverlayExclusiveInCategory(String pkgName) {
        Prefs.putBoolean(pkgName, true);
        Shell.cmd("cmd overlay enable-exclusive --user current --category " + pkgName, "cmd overlay set-priority " + pkgName + " highest").submit();
    }

    public static void enableOverlaysExclusiveInCategory(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, true);
            command.append("cmd overlay enable-exclusive --user current --category ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void disableOverlay(String pkgName) {
        Prefs.putBoolean(pkgName, false);
        Shell.cmd("cmd overlay disable --user current " + pkgName).submit();
    }

    public static void disableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, false);
            command.append("cmd overlay disable --user current ").append(pkgName).append("; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void changeOverlayState(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even.");
        }

        StringBuilder command = new StringBuilder();

        for (int i = 0; i < args.length; i += 2) {
            String pkgName = (String) args[i];
            boolean state = (boolean) args[i + 1];

            Prefs.putBoolean(pkgName, state);

            if (state) {
                command.append("cmd overlay enable --user current ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
            } else {
                command.append("cmd overlay disable --user current ").append(pkgName).append("; ");
            }
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static boolean overlayExists() {
        List<String> list = Shell.cmd("[ -f /system/product/overlay/IconifyComponentAMGC.apk ] && echo \"found\" || echo \"not found\"").exec().getOut();
        return Objects.equals(list.get(0), "found");
    }

    public static boolean matchOverlayAgainstAssets() {
        try {
            String[] packages = Iconify.getAppContext().getAssets().list("Overlays");
            int numberOfOverlaysInAssets = 0;

            assert packages != null;
            for (String overlay : packages) {
                numberOfOverlaysInAssets += Objects.requireNonNull(Iconify.getAppContext().getAssets().list("Overlays/" + overlay)).length;
            }

            int numberOfOverlaysInstalled = Integer.parseInt(Shell.cmd("find /" + Resources.OVERLAY_DIR + "/ -maxdepth 1 -type f -print| wc -l").exec().getOut().get(0));
            return numberOfOverlaysInAssets <= numberOfOverlaysInstalled;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getCategory(String pkgName) {
        String category = BuildConfig.APPLICATION_ID.replace(".debug", "") + ".category.";
        pkgName = pkgName.replace("IconifyComponent", "");

        if (pkgName.contains("MPIP")) {
            pkgName = keepFirstDigit(pkgName);
            category += "media_player_icon_pack_" + pkgName.toLowerCase();
        } else {
            pkgName = removeAllDigits(pkgName);

            switch (pkgName) {
                case "AMAC", "AMGC" -> category += "stock_monet_colors";
                case "BBN", "BBP" -> category += "brightness_bar_style";
                case "MPA", "MPB", "MPS" -> category += "media_player_style";
                case "NFN", "NFP" -> category += "notification_style";
                case "QSNT", "QSPT" -> category += "qs_tile_text_style";
                case "QSSN", "QSSP" -> category += "qs_shape_style";
                case "IPAS" -> category += "icon_pack_android_style";
                case "IPSUI" -> category += "icon_pack_sysui_style";
                default -> category += "iconify_component_" + pkgName.toLowerCase();
            }
        }

        return category;
    }

    private static String removeAllDigits(String input) {
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }

    private static String keepFirstDigit(String input) {
        StringBuilder output = new StringBuilder();
        boolean firstDigitFound = false;

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                if (!firstDigitFound) {
                    output.append(c);
                    firstDigitFound = true;
                }
            } else {
                output.append(c);
            }
        }

        return output.toString();
    }
}
