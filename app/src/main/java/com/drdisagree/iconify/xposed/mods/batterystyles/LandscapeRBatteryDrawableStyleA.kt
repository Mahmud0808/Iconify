/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.drdisagree.iconify.xposed.mods.batterystyles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import androidx.core.graphics.PathParser
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils

/**
 * A battery meter drawable that respects paths configured in
 * frameworks/base/core/res/res/values/config.xml to allow for an easily overrideable battery icon
 */
@SuppressLint("DiscouragedApi")
open class LandscapeRBatteryDrawableStyleA(private val context: Context, frameColor: Int) :
    BatteryDrawable() {

    // Need to load:
    // 1. perimeter shape
    // 2. fill mask (if smaller than perimeter, this would create a fill that
    //    doesn't touch the walls
    private val perimeterPath = Path()
    private val scaledPerimeter = Path()
    private val errorPerimeterPath = Path()
    private val scaledErrorPerimeter = Path()

    // Fill will cover the whole bounding rect of the fillMask, and be masked by the path
    private val fillMask = Path()
    private val scaledFill = Path()

    // Based off of the mask, the fill will interpolate across this space
    private val fillRect = RectF()

    // Top of this rect changes based on level, 100% == fillRect
    private val levelRect = RectF()
    private val levelPath = Path()

    // Updates the transform of the paths when our bounds change
    private val scaleMatrix = Matrix()
    private val padding = Rect()

    // The net result of fill + perimeter paths
    private val unifiedPath = Path()

    // Bolt path (used while charging)
    private val boltPath = Path()
    private val scaledBolt = Path()

    // Plus sign (used for power save mode)
    private val plusPath = Path()
    private val scaledPlus = Path()

    private var intrinsicHeight: Int
    private var intrinsicWidth: Int

    // To implement hysteresis, keep track of the need to invert the interior icon of the battery
    private var invertFillIcon = false

    // Colors can be configured based on battery level (see res/values/arrays.xml)
    private var colorLevels: IntArray

    private var fillColor: Int = Color.WHITE
    private var backgroundColor: Int = Color.WHITE

    // updated whenever level changes
    private var levelColor: Int = Color.WHITE

    // Dual tone implies that battery level is a clipped overlay over top of the whole shape
    private var dualTone = false

    private var batteryLevel = 0

    private val invalidateRunnable: () -> Unit = {
        invalidateSelf()
    }

    open var criticalLevel: Int = 5

    var charging = false
        set(value) {
            field = value
            postInvalidate()
        }

    override fun setChargingEnabled(charging: Boolean) {
        this.charging = charging
        postInvalidate()
    }

    var powerSaveEnabled = false
        set(value) {
            field = value
            postInvalidate()
        }

    override fun setPowerSavingEnabled(powerSaveEnabled: Boolean) {
        this.powerSaveEnabled = powerSaveEnabled
        postInvalidate()
    }

    var showPercent = false
        set(value) {
            field = value
            postInvalidate()
        }

    override fun setShowPercentEnabled(showPercent: Boolean) {
        this.showPercent = showPercent
        postInvalidate()
    }

    private val fillColorStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
        p.alpha = 255
        p.isDither = true
        p.strokeWidth = 5f
        p.style = Paint.Style.STROKE
        p.blendMode = BlendMode.SRC
        p.strokeMiter = 5f
        p.strokeJoin = Paint.Join.ROUND
    }

    private val fillColorStrokeProtection = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.isDither = true
        p.strokeWidth = 5f
        p.style = Paint.Style.STROKE
        p.blendMode = BlendMode.CLEAR
        p.strokeMiter = 5f
        p.strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
        p.alpha = 255
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
    }

    private val errorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = context.resources.getColorStateList(
            context.resources.getIdentifier(
                "batterymeter_plus_color", "color", context.packageName
            ), context.theme
        ).defaultColor
        p.alpha = 255
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    // Only used if dualTone is set to true
    private val dualToneBackgroundFill = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
        p.alpha = 85 // ~0.3 alpha by default
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        p.textAlign = Paint.Align.CENTER
    }

    init {
        val density = context.resources.displayMetrics.density
        intrinsicHeight = (Companion.HEIGHT * density).toInt()
        intrinsicWidth = (Companion.WIDTH * density).toInt()

        val res = context.resources
        val levels = res.obtainTypedArray(
            res.getIdentifier(
                "batterymeter_color_levels", "array", context.packageName
            )
        )
        val colors = res.obtainTypedArray(
            res.getIdentifier(
                "batterymeter_color_values", "array", context.packageName
            )
        )
        val N = levels.length()
        colorLevels = IntArray(2 * N)
        for (i in 0 until N) {
            colorLevels[2 * i] = levels.getInt(i, 0)
            if (colors.getType(i) == TypedValue.TYPE_ATTRIBUTE) {
                colorLevels[2 * i + 1] = SettingsLibUtils.getColorAttrDefaultColor(
                    colors.getResourceId(i, 0), context
                )
            } else {
                colorLevels[2 * i + 1] = colors.getColor(i, 0)
            }
        }
        levels.recycle()
        colors.recycle()

        loadPaths()
    }

    override fun draw(c: Canvas) {
        c.saveLayer(null, null)
        unifiedPath.reset()
        levelPath.reset()
        levelRect.set(fillRect)
        val fillFraction = batteryLevel / 100f
        val fillTop = if (batteryLevel >= 95) fillRect.right
        else fillRect.right - (fillRect.width() * (1 - fillFraction))

        levelRect.right = Math.floor(fillTop.toDouble()).toFloat()
        //levelPath.addRect(levelRect, Path.Direction.CCW)
        levelPath.addRoundRect(
            levelRect, floatArrayOf(
                4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f, 4.0f
            ), Path.Direction.CCW
        )

        // The perimeter should never change
        unifiedPath.addPath(scaledPerimeter)
        // If drawing dual tone, the level is used only to clip the whole drawable path
        if (!dualTone) {
            unifiedPath.op(levelPath, Path.Op.UNION)
        }

        fillPaint.color = levelColor

        // Deal with unifiedPath clipping before it draws
        if (charging) {
            // Clip out the bolt shape
            unifiedPath.op(scaledBolt, Path.Op.DIFFERENCE)
            if (!invertFillIcon) {
                c.drawPath(scaledBolt, fillPaint)
            }
        }

        if (dualTone) {
            // Dual tone means we draw the shape again, clipped to the charge level
            c.drawPath(unifiedPath, dualToneBackgroundFill)
            c.save()
            c.clipRect(
                bounds.left.toFloat(),
                0f,
                bounds.right + bounds.width() * fillFraction,
                bounds.left.toFloat()
            )
            c.drawPath(unifiedPath, fillPaint)
            c.restore()
        } else {
            // Non dual-tone means we draw the perimeter (with the level fill), and potentially
            // draw the fill again with a critical color
            fillPaint.color = fillColor
            c.drawPath(unifiedPath, fillPaint)
            fillPaint.color = levelColor

            // Show colorError below this level
            if (batteryLevel <= Companion.CRITICAL_LEVEL && !charging) {
                c.save()
                c.clipPath(scaledFill)
                c.drawPath(levelPath, fillPaint)
                c.restore()
            }
        }

        if (charging) {
            c.clipOutPath(scaledBolt)
            if (invertFillIcon) {
                c.drawPath(scaledBolt, fillColorStrokePaint)
            } else {
                c.drawPath(scaledBolt, fillColorStrokeProtection)
            }
        } else if (powerSaveEnabled) {
            // If power save is enabled draw the perimeter path with colorError
            c.drawPath(scaledErrorPerimeter, errorPaint)
            // And draw the plus sign on top of the fill
            if (!showPercent) {
                c.drawPath(scaledPlus, errorPaint)
            }
        }
        c.restore()

        if (!charging && batteryLevel < 100 && showPercent) {
            textPaint.textSize = bounds.width() * 0.38f
            val textHeight = +textPaint.fontMetrics.ascent
            val pctX = (bounds.width() + textHeight) * 0.7f
            val pctY = bounds.height() * 0.8f

            textPaint.color = fillColor
            c.drawText(batteryLevel.toString(), pctX, pctY, textPaint)

            textPaint.color = fillColor.toInt().inv() or 0xFF000000.toInt()
            c.save()
            c.clipRect(
                fillRect.left,
                fillRect.top,
                fillRect.right - (fillRect.width() * (1 - fillFraction)),
                fillRect.bottom
            )
            c.drawText(batteryLevel.toString(), pctX, pctY, textPaint)
            c.restore()
        }
    }

    private fun batteryColorForLevel(level: Int): Int {
        return when {
            charging || powerSaveEnabled -> fillColor
            else -> getColorForLevel(level)
        }
    }

    private fun getColorForLevel(level: Int): Int {
        var thresh: Int
        var color = 0
        var i = 0
        while (i < colorLevels.size) {
            thresh = colorLevels[i]
            color = colorLevels[i + 1]
            if (level <= thresh) {

                // Respect tinting for "normal" level
                return if (i == colorLevels.size - 2) {
                    fillColor
                } else {
                    color
                }
            }
            i += 2
        }
        return color
    }

    /**
     * Alpha is unused internally, and should be defined in the colors passed to {@link setColors}.
     * Further, setting an alpha for a dual tone battery meter doesn't make sense without bounds
     * defining the minimum background fill alpha. This is because fill + background must be equal
     * to the net alpha passed in here.
     */
    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        fillColorStrokePaint.colorFilter = colorFilter
        dualToneBackgroundFill.colorFilter = colorFilter
    }

    /**
     * Deprecated, but required by Drawable
     */
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun getIntrinsicHeight(): Int {
        return intrinsicHeight
    }

    override fun getIntrinsicWidth(): Int {
        return intrinsicWidth
    }

    /**
     * Set the fill level
     */
    override fun setBatteryLevel(l: Int) {
        invertFillIcon = if (l >= 67) true else if (l <= 33) false else invertFillIcon
        batteryLevel = l
        levelColor = batteryColorForLevel(batteryLevel)
        invalidateSelf()
    }

    fun getBatteryLevel(): Int {
        return batteryLevel
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateSize()
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        padding.left = left
        padding.top = top
        padding.right = right
        padding.bottom = bottom

        updateSize()
    }

    override fun setColors(fgColor: Int, bgColor: Int, singleToneColor: Int) {
        fillColor = if (dualTone) fgColor else singleToneColor

        fillPaint.color = fillColor
        fillColorStrokePaint.color = fillColor

        backgroundColor = bgColor
        dualToneBackgroundFill.color = bgColor

        // Also update the level color, since fillColor may have changed
        levelColor = batteryColorForLevel(batteryLevel)

        invalidateSelf()
    }

    private fun postInvalidate() {
        unscheduleSelf(invalidateRunnable)
        scheduleSelf(invalidateRunnable, 0)
    }

    private fun updateSize() {
        val b = bounds
        if (b.isEmpty) {
            scaleMatrix.setScale(1f, 1f)
        } else {
            scaleMatrix.setScale((b.right / WIDTH), (b.bottom / HEIGHT))
        }

        perimeterPath.transform(scaleMatrix, scaledPerimeter)
        errorPerimeterPath.transform(scaleMatrix, scaledErrorPerimeter)
        fillMask.transform(scaleMatrix, scaledFill)
        scaledFill.computeBounds(fillRect, true)
        boltPath.transform(scaleMatrix, scaledBolt)
        plusPath.transform(scaleMatrix, scaledPlus)

        // It is expected that this view only ever scale by the same factor in each dimension, so
        // just pick one to scale the strokeWidths
        val scaledStrokeWidth =

            Math.max(b.right / WIDTH * PROTECTION_STROKE_WIDTH, PROTECTION_MIN_STROKE_WIDTH)

        fillColorStrokePaint.strokeWidth = scaledStrokeWidth
        fillColorStrokeProtection.strokeWidth = scaledStrokeWidth
    }

    private fun loadPaths() {
        val pathString =
            "M20.74,6L20.74,9.01L20.83,9C21.17,8.98,21.45,8.88,21.74,8.69C21.89,8.59,21.98,8.52,22.11,8.38C22.43,8.06,22.65,7.68,22.81,7.19C23.22,5.94,22.93,4.48,22.13,3.64C21.75,3.25,21.32,3.04,20.84,3L20.74,3L20.74,6Z,M2.74,0.01C2.41,0.04,2.08,0.12,1.79,0.25C1.34,0.44,0.92,0.76,0.61,1.16C0.26,1.62,0.03,2.19,-0.01,2.78C-0.02,2.95,-0.02,9.07,-0.01,9.23C0.02,9.62,0.12,9.98,0.29,10.33C0.59,10.92,1.03,11.38,1.61,11.67C1.87,11.81,2.13,11.9,2.42,11.95C2.7,12.01,2.21,12,9.99,12C15.93,12,17.22,12,17.31,11.9C18.35,11.87,19.23,11.25,19.68,10.33C19.84,10,19.93,9.68,19.98,9.33C19.99,9.16,19.99,2.85,19.98,2.68C19.93,2.32,19.84,2,19.67,1.66C19.44,1.18,19.09,0.79,18.64,0.49C18.21,0.21,17.75,0.06,17.22,0.01C17.08,-0,2.87,-0,2.74,0.01ZM17.03,1.26C17.67,1.33,18.21,1.7,18.51,2.26C18.58,2.39,18.64,2.54,18.67,2.68C18.74,2.93,18.73,2.74,18.73,6C18.73,7.97,18.73,9,18.72,9.06C18.64,9.76,18.19,10.36,17.54,10.62C17.42,10.67,17.3,10.7,17.15,10.73L17.03,10.75L9.98,10.75L2.93,10.75L2.8,10.73C1.95,10.58,1.32,9.9,1.23,9.05C1.22,8.97,1.22,8.02,1.22,5.93L1.22,2.93L1.25,2.82C1.35,2.28,1.64,1.85,2.09,1.56C2.32,1.4,2.63,1.29,2.91,1.26C3.03,1.25,16.91,1.25,17.03,1.26Z"
        perimeterPath.set(PathParser.createPathFromPathData(pathString))
        perimeterPath.computeBounds(RectF(), true)

        val errorPathString =
            "M20.74,6L20.74,9.01L20.83,9C21.17,8.98,21.45,8.88,21.74,8.69C21.89,8.59,21.98,8.52,22.11,8.38C22.43,8.06,22.65,7.68,22.81,7.19C23.22,5.94,22.93,4.48,22.13,3.64C21.75,3.25,21.32,3.04,20.84,3L20.74,3L20.74,6Z,M2.74,0.01C2.41,0.04,2.08,0.12,1.79,0.25C1.34,0.44,0.92,0.76,0.61,1.16C0.26,1.62,0.03,2.19,-0.01,2.78C-0.02,2.95,-0.02,9.07,-0.01,9.23C0.02,9.62,0.12,9.98,0.29,10.33C0.59,10.92,1.03,11.38,1.61,11.67C1.87,11.81,2.13,11.9,2.42,11.95C2.7,12.01,2.21,12,9.99,12C15.93,12,17.22,12,17.31,11.9C18.35,11.87,19.23,11.25,19.68,10.33C19.84,10,19.93,9.68,19.98,9.33C19.99,9.16,19.99,2.85,19.98,2.68C19.93,2.32,19.84,2,19.67,1.66C19.44,1.18,19.09,0.79,18.64,0.49C18.21,0.21,17.75,0.06,17.22,0.01C17.08,-0,2.87,-0,2.74,0.01ZM17.03,1.26C17.67,1.33,18.21,1.7,18.51,2.26C18.58,2.39,18.64,2.54,18.67,2.68C18.74,2.93,18.73,2.74,18.73,6C18.73,7.97,18.73,9,18.72,9.06C18.64,9.76,18.19,10.36,17.54,10.62C17.42,10.67,17.3,10.7,17.15,10.73L17.03,10.75L9.98,10.75L2.93,10.75L2.8,10.73C1.95,10.58,1.32,9.9,1.23,9.05C1.22,8.97,1.22,8.02,1.22,5.93L1.22,2.93L1.25,2.82C1.35,2.28,1.64,1.85,2.09,1.56C2.32,1.4,2.63,1.29,2.91,1.26C3.03,1.25,16.91,1.25,17.03,1.26Z"
        errorPerimeterPath.set(PathParser.createPathFromPathData(errorPathString))
        errorPerimeterPath.computeBounds(RectF(), true)

        val fillMaskString =
            "M3.32,1.76C3.15,1.77,3.01,1.8,2.86,1.85C2.26,2.07,1.84,2.59,1.75,3.22C1.73,3.35,1.73,8.67,1.75,8.8C1.86,9.49,2.35,10.04,3.03,10.21C3.22,10.25,2.84,10.25,9.99,10.25C15.54,10.25,16.7,10.25,16.78,10.24C17.29,10.16,17.73,9.87,18,9.43C18.1,9.26,18.19,9.02,18.22,8.82C18.24,8.72,18.24,8.52,18.24,6.01C18.24,3.8,18.24,3.29,18.22,3.22C18.13,2.59,17.71,2.07,17.11,1.85C17.01,1.82,16.93,1.8,16.81,1.77C16.73,1.76,16.2,1.75,10.04,1.75C6.36,1.75,3.34,1.75,3.32,1.76Z"
        fillMask.set(PathParser.createPathFromPathData(fillMaskString))
        // Set the fill rect so we can calculate the fill properly
        fillMask.computeBounds(fillRect, true)

        val boltPathString =
            "M7.43,6.77L9.68,6.77L8.51,9.81C8.34,10.26,8.8,10.49,9.1,10.13L12.75,5.78C12.82,5.7,12.86,5.61,12.86,5.52C12.86,5.35,12.73,5.23,12.54,5.23L10.29,5.23L11.46,2.19C11.63,1.75,11.17,1.52,10.88,1.87L7.23,6.21C7.15,6.3,7.11,6.39,7.11,6.48C7.11,6.65,7.25,6.77,7.43,6.77Z"
        boltPath.set(PathParser.createPathFromPathData(boltPathString))

        val plusPathString =
            "M20.74,6L20.74,9.01L20.83,9C21.17,8.98,21.45,8.88,21.74,8.69C21.89,8.59,21.98,8.52,22.11,8.38C22.43,8.06,22.65,7.68,22.81,7.19C23.22,5.94,22.93,4.48,22.13,3.64C21.75,3.25,21.32,3.04,20.84,3L20.74,3L20.74,6Z,M2.74,0.01C2.41,0.04,2.08,0.12,1.79,0.25C1.34,0.44,0.92,0.76,0.61,1.16C0.26,1.62,0.03,2.19,-0.01,2.78C-0.02,2.95,-0.02,9.07,-0.01,9.23C0.02,9.62,0.12,9.98,0.29,10.33C0.59,10.92,1.03,11.38,1.61,11.67C1.87,11.81,2.13,11.9,2.42,11.95C2.7,12.01,2.21,12,9.99,12C15.93,12,17.22,12,17.31,11.9C18.35,11.87,19.23,11.25,19.68,10.33C19.84,10,19.93,9.68,19.98,9.33C19.99,9.16,19.99,2.85,19.98,2.68C19.93,2.32,19.84,2,19.67,1.66C19.44,1.18,19.09,0.79,18.64,0.49C18.21,0.21,17.75,0.06,17.22,0.01C17.08,-0,2.87,-0,2.74,0.01ZM17.03,1.26C17.67,1.33,18.21,1.7,18.51,2.26C18.58,2.39,18.64,2.54,18.67,2.68C18.74,2.93,18.73,2.74,18.73,6C18.73,7.97,18.73,9,18.72,9.06C18.64,9.76,18.19,10.36,17.54,10.62C17.42,10.67,17.3,10.7,17.15,10.73L17.03,10.75L9.98,10.75L2.93,10.75L2.8,10.73C1.95,10.58,1.32,9.9,1.23,9.05C1.22,8.97,1.22,8.02,1.22,5.93L1.22,2.93L1.25,2.82C1.35,2.28,1.64,1.85,2.09,1.56C2.32,1.4,2.63,1.29,2.91,1.26C3.03,1.25,16.91,1.25,17.03,1.26Z"
        plusPath.set(PathParser.createPathFromPathData(plusPathString))

        dualTone = false
    }

    companion object {
        private const val TAG = "LandscapeRBatteryDrawableStyleA"
        private const val WIDTH = 24f
        private const val HEIGHT = 12f
        private const val CRITICAL_LEVEL = 15

        // On a 12x20 grid, how wide to make the fill protection stroke.
        // Scales when our size changes
        private const val PROTECTION_STROKE_WIDTH = 3f

        // Arbitrarily chosen for visibility at small sizes
        private const val PROTECTION_MIN_STROKE_WIDTH = 6f
    }
}