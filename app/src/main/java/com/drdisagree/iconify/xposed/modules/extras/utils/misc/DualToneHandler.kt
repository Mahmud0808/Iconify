package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt

/**
 * Utility object for handling dual-tone icon colors.
 *
 * This handler provides color values for:
 *  - single-tone icons
 *  - background layers
 *  - fill layers
 *
 * Colors are resolved based on either:
 *  - a normalized dark intensity value, or
 *  - the luminance of a provided color.
 *
 * It is primarily used to ensure icons remain visible and consistent across
 * different themes and background shades.
 */
object DualToneHandler {

    private val darkColor = Colors(
        single = Color.BLACK,
        background = "#3D000000".toColorInt(),
        fill = "#7A000000".toColorInt()
    )

    private val lightColor = Colors(
        single = Color.WHITE,
        background = "#4DFFFFFF".toColorInt(),
        fill = "#FFFFFF".toColorInt()
    )

    /**
     * Returns the single-tone icon color based on the provided dark intensity.
     *
     * @param darkIntensity A value in the range [0, 1] representing how dark
     * the background is. Values greater than 0.5 select the dark variant;
     * otherwise the light variant is used.
     * @return The resolved single-tone color.
     */
    fun getSingleColor(darkIntensity: Float): Int =
        if (darkIntensity > 0.5f) darkColor.single else lightColor.single

    /**
     * Returns a tinted single-tone icon color based on dark intensity.
     *
     * The light or dark variant is selected using [darkIntensity], and the
     * alpha value of that resolved base color is applied to the provided tint.
     *
     * @param darkIntensity A value in the range [0, 1] representing how dark
     * the background is.
     * @param color The tint color whose RGB components are preserved.
     * @return The tinted single-tone color with adjusted alpha.
     */
    fun getSingleColorWithTint(darkIntensity: Float, color: Int): Int {
        val baseColor = getSingleColor(darkIntensity)
        val alpha = Color.alpha(baseColor)
        return ColorUtils.setAlphaComponent(color, alpha)
    }

    /**
     * Returns the single-tone icon color based on the luminance of a color.
     *
     * @param color The color whose luminance determines whether the light or
     * dark variant is selected.
     * @return The resolved single-tone color.
     */
    fun getSingleColor(color: Int): Int =
        if (ColorUtils.calculateLuminance(color) > 0.5)
            lightColor.single
        else
            darkColor.single

    /**
     * Returns a tinted single-tone icon color based on luminance.
     *
     * The light or dark variant is selected using the luminance of [color],
     * and the alpha value of that resolved base color is applied to the same
     * tint color.
     *
     * @param color The tint color whose luminance determines the variant.
     * @return The tinted single-tone color with adjusted alpha.
     */
    fun getSingleColorWithTint(color: Int): Int {
        val baseColor = getSingleColor(color)
        val alpha = Color.alpha(baseColor)
        return ColorUtils.setAlphaComponent(color, alpha)
    }

    /**
     * Returns the background layer color based on the provided dark intensity.
     *
     * @param darkIntensity A value in the range [0, 1] representing how dark
     * the background is. Values greater than 0.5 select the dark variant;
     * otherwise the light variant is used.
     * @return The resolved background color.
     */
    fun getBackgroundColor(darkIntensity: Float): Int =
        if (darkIntensity > 0.5f) darkColor.background else lightColor.background

    /**
     * Returns a tinted background layer color based on dark intensity.
     *
     * The background variant is selected using [darkIntensity], and the alpha
     * value of that resolved background color is applied to the provided tint.
     *
     * @param darkIntensity A value in the range [0, 1] representing how dark
     * the background is.
     * @param color The tint color whose RGB components are preserved.
     * @return The tinted background color with adjusted alpha.
     */
    fun getBackgroundColorWithTint(darkIntensity: Float, color: Int): Int {
        val baseColor = getBackgroundColor(darkIntensity)
        val alpha = Color.alpha(baseColor)
        return ColorUtils.setAlphaComponent(color, alpha)
    }

    /**
     * Returns the background layer color based on the luminance of a color.
     *
     * @param color The color whose luminance determines whether the light or
     * dark background variant is selected.
     * @return The resolved background color.
     */
    fun getBackgroundColor(color: Int): Int =
        if (ColorUtils.calculateLuminance(color) > 0.5)
            lightColor.background
        else
            darkColor.background

    /**
     * Returns a tinted background layer color based on luminance.
     *
     * The background variant is selected using the luminance of [color], and
     * the alpha value of the resolved base color is applied to the tint.
     *
     * @param color The tint color whose luminance determines the variant.
     * @return The tinted background color with adjusted alpha.
     */
    fun getBackgroundColorWithTint(color: Int): Int {
        val baseColor = getBackgroundColor(color)
        val alpha = Color.alpha(baseColor)
        return ColorUtils.setAlphaComponent(color, alpha)
    }

    /**
     * Returns the fill layer color based on the provided dark intensity.
     *
     * @param darkIntensity A value in the range [0, 1] representing how dark
     * the background is. Values greater than 0.5 select the dark variant;
     * otherwise the light variant is used.
     * @return The resolved fill color.
     */
    fun getFillColor(darkIntensity: Float): Int =
        if (darkIntensity > 0.5f) darkColor.fill else lightColor.fill

    /**
     * Returns a tinted fill layer color based on dark intensity.
     *
     * The fill variant is selected using [darkIntensity], and the alpha value
     * of that resolved fill color is applied to the provided tint.
     *
     * @param darkIntensity A value in the range [0, 1] representing how dark
     * the background is.
     * @param color The tint color whose RGB components are preserved.
     * @return The tinted fill color with adjusted alpha.
     */
    fun getFillColorWithTint(darkIntensity: Float, color: Int): Int {
        val baseColor = getFillColor(darkIntensity)
        val alpha = Color.alpha(baseColor)
        return ColorUtils.setAlphaComponent(color, alpha)
    }

    /**
     * Returns the fill layer color based on the luminance of a color.
     *
     * @param color The color whose luminance determines whether the light or
     * dark fill variant is selected.
     * @return The resolved fill color.
     */
    fun getFillColor(color: Int): Int =
        if (ColorUtils.calculateLuminance(color) > 0.5)
            lightColor.fill
        else
            darkColor.fill

    /**
     * Returns a tinted fill layer color based on luminance.
     *
     * The fill variant is selected using the luminance of [color], and the
     * alpha value of the resolved base color is applied to the tint.
     *
     * @param color The tint color whose luminance determines the variant.
     * @return The tinted fill color with adjusted alpha.
     */
    fun getFillColorWithTint(color: Int): Int {
        val baseColor = getFillColor(color)
        val alpha = Color.alpha(baseColor)
        return ColorUtils.setAlphaComponent(color, alpha)
    }

    /**
     * Internal container for a theme's color variants.
     *
     * @property single Single-tone icon color.
     * @property background Background layer color.
     * @property fill Fill layer color.
     */
    private data class Colors(
        val single: Int,
        val background: Int,
        val fill: Int
    )
}