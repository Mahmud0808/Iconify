package com.drdisagree.iconify.utils.color

import android.content.Context
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.color.monet.hct.Hct
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeContent
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeExpressive
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeFidelity
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeFruitSalad
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeMonochrome
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeNeutral
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeRainbow
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeTonalSpot
import com.drdisagree.iconify.utils.color.monet.scheme.SchemeVibrant

object ColorSchemeUtils {

    private val tones = intArrayOf(100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0)

    fun generateColorPalette(context: Context, style: String?, color: Int): List<MutableList<Any>> {
        val palette: MutableList<MutableList<Any>> = ArrayList()
        val systemAccent1: MutableList<Any> = ArrayList()
        val systemAccent2: MutableList<Any> = ArrayList()
        val systemAccent3: MutableList<Any> = ArrayList()
        val systemNeutral1: MutableList<Any> = ArrayList()
        val systemNeutral2: MutableList<Any> = ArrayList()

        when (style) {
            context.resources.getString(R.string.monet_neutral) -> {
                val schemeNeutral = SchemeNeutral(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeNeutral.primaryPalette.tone(tone))
                    systemAccent2.add(schemeNeutral.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeNeutral.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeNeutral.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeNeutral.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_monochrome) -> {
                val schemeMonochrome =
                    SchemeMonochrome(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeMonochrome.primaryPalette.tone(tone))
                    systemAccent2.add(schemeMonochrome.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeMonochrome.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeMonochrome.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeMonochrome.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_tonalspot) -> {
                val schemeTonalSpot =
                    SchemeTonalSpot(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeTonalSpot.primaryPalette.tone(tone))
                    systemAccent2.add(schemeTonalSpot.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeTonalSpot.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeTonalSpot.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeTonalSpot.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_vibrant) -> {
                val schemeVibrant = SchemeVibrant(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeVibrant.primaryPalette.tone(tone))
                    systemAccent2.add(schemeVibrant.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeVibrant.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeVibrant.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeVibrant.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_rainbow) -> {
                val schemeRainbow = SchemeRainbow(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeRainbow.primaryPalette.tone(tone))
                    systemAccent2.add(schemeRainbow.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeRainbow.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeRainbow.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeRainbow.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_expressive) -> {
                val schemeExpressive =
                    SchemeExpressive(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeExpressive.primaryPalette.tone(tone))
                    systemAccent2.add(schemeExpressive.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeExpressive.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeExpressive.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeExpressive.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_fidelity) -> {
                val schemeFidelity =
                    SchemeFidelity(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeFidelity.primaryPalette.tone(tone))
                    systemAccent2.add(schemeFidelity.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeFidelity.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeFidelity.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeFidelity.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_content) -> {
                val schemeContent = SchemeContent(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeContent.primaryPalette.tone(tone))
                    systemAccent2.add(schemeContent.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeContent.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeContent.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeContent.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }

            context.resources.getString(R.string.monet_fruitsalad) -> {
                val schemeFruitSalad =
                    SchemeFruitSalad(Hct.fromInt(color), SystemUtils.isDarkMode, 5.0)

                for (tone in tones) {
                    systemAccent1.add(schemeFruitSalad.primaryPalette.tone(tone))
                    systemAccent2.add(schemeFruitSalad.secondaryPalette.tone(tone))
                    systemAccent3.add(schemeFruitSalad.tertiaryPalette.tone(tone))
                    systemNeutral1.add(schemeFruitSalad.neutralPalette.tone(tone))
                    systemNeutral2.add(schemeFruitSalad.neutralVariantPalette.tone(tone))
                }

                palette.add(systemAccent1)
                palette.add(systemAccent2)
                palette.add(systemAccent3)
                palette.add(systemNeutral1)
                palette.add(systemNeutral2)
            }
        }

        return palette
    }
}
