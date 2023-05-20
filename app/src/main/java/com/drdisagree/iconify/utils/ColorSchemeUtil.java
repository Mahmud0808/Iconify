package com.drdisagree.iconify.utils;

import android.content.Context;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.monet.hct.Hct;
import com.drdisagree.iconify.utils.monet.scheme.SchemeContent;
import com.drdisagree.iconify.utils.monet.scheme.SchemeExpressive;
import com.drdisagree.iconify.utils.monet.scheme.SchemeFidelity;
import com.drdisagree.iconify.utils.monet.scheme.SchemeMonochrome;
import com.drdisagree.iconify.utils.monet.scheme.SchemeNeutral;
import com.drdisagree.iconify.utils.monet.scheme.SchemeTonalSpot;
import com.drdisagree.iconify.utils.monet.scheme.SchemeVibrant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColorSchemeUtil {

    public static final int[] tones = {100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0};

    public static List<List<Object>> GenerateColorPalette(Context context, String style, int color) {
        List<List<Object>> palette = new ArrayList<>();
        List<Object> system_accent1 = new ArrayList<>();
        List<Object> system_accent2 = new ArrayList<>();
        List<Object> system_accent3 = new ArrayList<>();
        List<Object> system_neutral1 = new ArrayList<>();
        List<Object> system_neutral2 = new ArrayList<>();

        if (Objects.equals(style, context.getResources().getString(R.string.monet_neutral))) {
            SchemeNeutral schemeNeutral = new SchemeNeutral(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);

            for (int tone : tones) {
                system_accent1.add(schemeNeutral.primaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent2.add(schemeNeutral.secondaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent3.add(schemeNeutral.tertiaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral1.add(schemeNeutral.neutralPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral2.add(schemeNeutral.neutralVariantPalette.tone(tone));
            }

            palette.add(system_accent1);
            palette.add(system_accent2);
            palette.add(system_accent3);
            palette.add(system_neutral1);
            palette.add(system_neutral2);
        } else if (Objects.equals(style, context.getResources().getString(R.string.monet_monochrome))) {
            SchemeMonochrome schemeMonochrome = new SchemeMonochrome(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);

            for (int tone : tones) {
                system_accent1.add(schemeMonochrome.primaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent2.add(schemeMonochrome.secondaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent3.add(schemeMonochrome.tertiaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral1.add(schemeMonochrome.neutralPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral2.add(schemeMonochrome.neutralVariantPalette.tone(tone));
            }

            palette.add(system_accent1);
            palette.add(system_accent2);
            palette.add(system_accent3);
            palette.add(system_neutral1);
            palette.add(system_neutral2);
        } else if (Objects.equals(style, context.getResources().getString(R.string.monet_tonalspot))) {
            SchemeTonalSpot schemeTonalSpot = new SchemeTonalSpot(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);

            for (int tone : tones) {
                system_accent1.add(schemeTonalSpot.primaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent2.add(schemeTonalSpot.secondaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent3.add(schemeTonalSpot.tertiaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral1.add(schemeTonalSpot.neutralPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral2.add(schemeTonalSpot.neutralVariantPalette.tone(tone));
            }

            palette.add(system_accent1);
            palette.add(system_accent2);
            palette.add(system_accent3);
            palette.add(system_neutral1);
            palette.add(system_neutral2);
        } else if (Objects.equals(style, context.getResources().getString(R.string.monet_vibrant))) {
            SchemeVibrant schemeVibrant = new SchemeVibrant(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);

            for (int tone : tones) {
                system_accent1.add(schemeVibrant.primaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent2.add(schemeVibrant.secondaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent3.add(schemeVibrant.tertiaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral1.add(schemeVibrant.neutralPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral2.add(schemeVibrant.neutralVariantPalette.tone(tone));
            }

            palette.add(system_accent1);
            palette.add(system_accent2);
            palette.add(system_accent3);
            palette.add(system_neutral1);
            palette.add(system_neutral2);
        } else if (Objects.equals(style, context.getResources().getString(R.string.monet_expressive))) {
            SchemeExpressive schemeExpressive = new SchemeExpressive(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);

            for (int tone : tones) {
                system_accent1.add(schemeExpressive.primaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent2.add(schemeExpressive.secondaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent3.add(schemeExpressive.tertiaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral1.add(schemeExpressive.neutralPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral2.add(schemeExpressive.neutralVariantPalette.tone(tone));
            }

            palette.add(system_accent1);
            palette.add(system_accent2);
            palette.add(system_accent3);
            palette.add(system_neutral1);
            palette.add(system_neutral2);
        } else if (Objects.equals(style, context.getResources().getString(R.string.monet_fidelity))) {
            SchemeFidelity schemeFidelity = new SchemeFidelity(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);

            for (int tone : tones) {
                system_accent1.add(schemeFidelity.primaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent2.add(schemeFidelity.secondaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent3.add(schemeFidelity.tertiaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral1.add(schemeFidelity.neutralPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral2.add(schemeFidelity.neutralVariantPalette.tone(tone));
            }

            palette.add(system_accent1);
            palette.add(system_accent2);
            palette.add(system_accent3);
            palette.add(system_neutral1);
            palette.add(system_neutral2);
        } else if (Objects.equals(style, context.getResources().getString(R.string.monet_content))) {
            SchemeContent schemeContent = new SchemeContent(Hct.fromInt(color), SystemUtil.isDarkMode(), 5);

            for (int tone : tones) {
                system_accent1.add(schemeContent.primaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent2.add(schemeContent.secondaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_accent3.add(schemeContent.tertiaryPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral1.add(schemeContent.neutralPalette.tone(tone));
            }

            for (int tone : tones) {
                system_neutral2.add(schemeContent.neutralVariantPalette.tone(tone));
            }

            palette.add(system_accent1);
            palette.add(system_accent2);
            palette.add(system_accent3);
            palette.add(system_neutral1);
            palette.add(system_neutral2);
        }
        return palette;
    }
}
