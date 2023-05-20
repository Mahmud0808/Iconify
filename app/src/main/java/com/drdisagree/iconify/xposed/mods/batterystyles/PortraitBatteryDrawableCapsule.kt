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
open class PortraitBatteryDrawableCapsule(private val context: Context, frameColor: Int) :
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
        intrinsicHeight = (HEIGHT * density).toInt()
        intrinsicWidth = (WIDTH * density).toInt()

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
        val fillTop = if (batteryLevel >= 95) fillRect.top
        else fillRect.top + (fillRect.height() * (1 - fillFraction))

        levelRect.top = Math.floor(fillTop.toDouble()).toFloat()
        //levelPath.addRect(levelRect, Path.Direction.CCW)
        levelPath.addRoundRect(
            levelRect, floatArrayOf(
                8.0f, 8.0f, 8.0f, 8.0f, 8.0f, 8.0f, 8.0f, 8.0f
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
                0f,
                bounds.bottom - bounds.height() * fillFraction,
                bounds.right.toFloat(),
                bounds.bottom.toFloat()
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
            textPaint.textSize = bounds.height() * 0.38f
            val textHeight = -textPaint.fontMetrics.ascent
            val pctX = bounds.width() * 0.5f
            val pctY = (bounds.height() + textHeight) * 0.5f

            textPaint.color = fillColor
            c.drawText(batteryLevel.toString(), pctX, pctY, textPaint)

            textPaint.color = fillColor.toInt().inv() or 0xFF000000.toInt()
            c.save()
            c.clipRect(
                fillRect.left,
                fillRect.top + (fillRect.height() * (1 - fillFraction)),
                fillRect.right,
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

    @SuppressLint("RestrictedApi")
    private fun loadPaths() {
        val pathString =
            "M3.61,5.65L8.39,5.65A3.59 3.59 0 0 1 11.98,9.24L11.98,10.76A3.59 3.59 0 0 1 8.39,14.35L3.61,14.35A3.59 3.59 0 0 1 0.02,10.76L0.02,9.24A3.59 3.59 0 0 1 3.61,5.65zM1.32,9.13L1.32,10.87A2.17 2.17 0 0 0 3.50,13.04L8.50,13.04A2.17 2.17 0 0 0 10.68,10.87L10.68,9.13A2.17 2.17 0 0 0 8.50,6.96L3.50,6.96A2.17 2.17 0 0 0 1.32,9.13z"
        perimeterPath.set(PathParser.createPathFromPathData(pathString))
        perimeterPath.computeBounds(RectF(), true)

        val errorPathString =
            "M3.61,5.65L8.39,5.65A3.59 3.59 0 0 1 11.98,9.24L11.98,10.76A3.59 3.59 0 0 1 8.39,14.35L3.61,14.35A3.59 3.59 0 0 1 0.02,10.76L0.02,9.24A3.59 3.59 0 0 1 3.61,5.65zM1.32,9.13L1.32,10.87A2.17 2.17 0 0 0 3.50,13.04L8.50,13.04A2.17 2.17 0 0 0 10.68,10.87L10.68,9.13A2.17 2.17 0 0 0 8.50,6.96L3.50,6.96A2.17 2.17 0 0 0 1.32,9.13z"
        errorPerimeterPath.set(PathParser.createPathFromPathData(errorPathString))
        errorPerimeterPath.computeBounds(RectF(), true)

        val fillMaskString =
            "M2.25,8.00L9.75,8.00A0.00 0.00 0 0 1 9.75,8.00L9.75,12.00A0.00 0.00 0 0 1 9.75,12.00L2.25,12.00A0.00 0.00 0 0 1 2.25,12.00L2.25,8.00A0.00 0.00 0 0 1 2.25,8.00z"
        fillMask.set(PathParser.createPathFromPathData(fillMaskString))
        // Set the fill rect so we can calculate the fill properly
        fillMask.computeBounds(fillRect, true)

        val boltPathString =
            "M5.61,10.76L5.60,12.15C5.58,12.42,5.95,12.49,6.14,12.15L7.25,10.09C7.44,9.66,7.07,9.24,6.39,9.24L6.40,7.85C6.42,7.58,6.05,7.51,5.86,7.85L4.75,9.91C4.56,10.34,4.93,10.76,5.61,10.76"
        boltPath.set(PathParser.createPathFromPathData(boltPathString))

        val plusPathString =
            "M3.61,5.65L8.39,5.65A3.59 3.59 0 0 1 11.98,9.24L11.98,10.76A3.59 3.59 0 0 1 8.39,14.35L3.61,14.35A3.59 3.59 0 0 1 0.02,10.76L0.02,9.24A3.59 3.59 0 0 1 3.61,5.65zM1.32,9.13L1.32,10.87A2.17 2.17 0 0 0 3.50,13.04L8.50,13.04A2.17 2.17 0 0 0 10.68,10.87L10.68,9.13A2.17 2.17 0 0 0 8.50,6.96L3.50,6.96A2.17 2.17 0 0 0 1.32,9.13z"
        plusPath.set(PathParser.createPathFromPathData(plusPathString))

        dualTone = false
    }

    companion object {
        private const val TAG = "PortraitBatteryDrawableCapsule"
        private const val WIDTH = 12f
        private const val HEIGHT = 20f
        private const val CRITICAL_LEVEL = 15

        // On a 12x20 grid, how wide to make the fill protection stroke.
        // Scales when our size changes
        private const val PROTECTION_STROKE_WIDTH = 3f

        // Arbitrarily chosen for visibility at small sizes
        private const val PROTECTION_MIN_STROKE_WIDTH = 6f
    }
}