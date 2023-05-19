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
open class PortraitBatteryDrawableMx(private val context: Context, frameColor: Int) :
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
        val fillTop = if (batteryLevel >= 95) fillRect.top
        else fillRect.top + (fillRect.height() * (1 - fillFraction))

        levelRect.top = Math.floor(fillTop.toDouble()).toFloat()
        levelPath.addRect(levelRect, Path.Direction.CCW)

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

    private fun loadPaths() {
        val pathString =
            "M4.39,0.15C4.1,0.28,4.02,0.35,3.89,0.59C3.68,0.98,3.78,1.55,4.11,1.83C4.4,2.09,4.55,2.1,6.44,2.1C8.33,2.1,8.47,2.09,8.76,1.83C9.1,1.55,9.2,0.98,8.98,0.59C8.85,0.35,8.78,0.28,8.49,0.15C8.3,0.06,8.15,0.05,6.44,0.05C4.72,0.05,4.58,0.06,4.39,0.15ZM4.39,0.15M3.18,2.6C2.68,2.74,2.34,2.95,1.92,3.35C1.35,3.89,1.04,4.54,1.04,5.16L1.04,5.4L3.59,5.4L3.86,5.13C4.39,4.61,4.64,4.55,6.46,4.55C8.29,4.55,8.54,4.61,9.07,5.13L9.34,5.4L11.94,5.4L11.94,5.17C11.94,4.55,11.62,3.89,11.06,3.35C10.63,2.93,10.3,2.74,9.77,2.59C9.45,2.5,9.23,2.5,6.46,2.5C3.76,2.5,3.48,2.52,3.18,2.6ZM3.18,2.6M0.93,11L0.93,15.65L3.27,15.65L3.27,6.35L0.93,6.35ZM0.93,11M9.66,11L9.66,15.65L11.99,15.65L11.99,6.35L9.66,6.35ZM9.66,11M1.04,16.86C1.04,17.45,1.37,18.11,1.92,18.65C2.35,19.06,2.76,19.3,3.25,19.43C3.7,19.54,9.28,19.54,9.73,19.43C10.23,19.3,10.63,19.06,11.06,18.65C11.61,18.11,11.94,17.45,11.94,16.86L11.94,16.65L9.35,16.65L9.07,16.92C8.52,17.45,8.29,17.5,6.46,17.5C4.64,17.5,4.4,17.45,3.86,16.92L3.58,16.65L1.04,16.65ZM1.04,16.86"
        perimeterPath.set(PathParser.createPathFromPathData(pathString))
        perimeterPath.computeBounds(RectF(), true)

        val errorPathString =
            "M4.39,0.15C4.1,0.28,4.02,0.35,3.89,0.59C3.68,0.98,3.78,1.55,4.11,1.83C4.4,2.09,4.55,2.1,6.44,2.1C8.33,2.1,8.47,2.09,8.76,1.83C9.1,1.55,9.2,0.98,8.98,0.59C8.85,0.35,8.78,0.28,8.49,0.15C8.3,0.06,8.15,0.05,6.44,0.05C4.72,0.05,4.58,0.06,4.39,0.15ZM4.39,0.15M3.18,2.6C2.68,2.74,2.34,2.95,1.92,3.35C1.35,3.89,1.04,4.54,1.04,5.16L1.04,5.4L3.59,5.4L3.86,5.13C4.39,4.61,4.64,4.55,6.46,4.55C8.29,4.55,8.54,4.61,9.07,5.13L9.34,5.4L11.94,5.4L11.94,5.17C11.94,4.55,11.62,3.89,11.06,3.35C10.63,2.93,10.3,2.74,9.77,2.59C9.45,2.5,9.23,2.5,6.46,2.5C3.76,2.5,3.48,2.52,3.18,2.6ZM3.18,2.6M0.93,11L0.93,15.65L3.27,15.65L3.27,6.35L0.93,6.35ZM0.93,11M9.66,11L9.66,15.65L11.99,15.65L11.99,6.35L9.66,6.35ZM9.66,11M1.04,16.86C1.04,17.45,1.37,18.11,1.92,18.65C2.35,19.06,2.76,19.3,3.25,19.43C3.7,19.54,9.28,19.54,9.73,19.43C10.23,19.3,10.63,19.06,11.06,18.65C11.61,18.11,11.94,17.45,11.94,16.86L11.94,16.65L9.35,16.65L9.07,16.92C8.52,17.45,8.29,17.5,6.46,17.5C4.64,17.5,4.4,17.45,3.86,16.92L3.58,16.65L1.04,16.65ZM1.04,16.86"
        errorPerimeterPath.set(PathParser.createPathFromPathData(errorPathString))
        errorPerimeterPath.computeBounds(RectF(), true)

        val fillMaskString =
            "M5.32,6.04C5.09,6.15,4.98,6.25,4.88,6.46C4.78,6.64,4.78,6.82,4.78,11.08C4.78,15.32,4.78,15.52,4.88,15.69C5.11,16.14,5.32,16.2,6.46,16.2C7.61,16.2,7.82,16.14,8.05,15.69C8.14,15.52,8.15,15.32,8.15,11.08C8.15,6.82,8.15,6.64,8.05,6.46C7.94,6.25,7.84,6.14,7.59,6.03C7.36,5.91,5.55,5.91,5.32,6.04ZM5.32,6.04"
        fillMask.set(PathParser.createPathFromPathData(fillMaskString))
        // Set the fill rect so we can calculate the fill properly
        fillMask.computeBounds(fillRect, true)

        val boltPathString = "M5,17.5 V12 H3 L7,4.5 V10 h2 L5,17.5 z"
        boltPath.set(PathParser.createPathFromPathData(boltPathString))

        val plusPathString =
            "M4.39,0.15C4.1,0.28,4.02,0.35,3.89,0.59C3.68,0.98,3.78,1.55,4.11,1.83C4.4,2.09,4.55,2.1,6.44,2.1C8.33,2.1,8.47,2.09,8.76,1.83C9.1,1.55,9.2,0.98,8.98,0.59C8.85,0.35,8.78,0.28,8.49,0.15C8.3,0.06,8.15,0.05,6.44,0.05C4.72,0.05,4.58,0.06,4.39,0.15ZM4.39,0.15M3.18,2.6C2.68,2.74,2.34,2.95,1.92,3.35C1.35,3.89,1.04,4.54,1.04,5.16L1.04,5.4L3.59,5.4L3.86,5.13C4.39,4.61,4.64,4.55,6.46,4.55C8.29,4.55,8.54,4.61,9.07,5.13L9.34,5.4L11.94,5.4L11.94,5.17C11.94,4.55,11.62,3.89,11.06,3.35C10.63,2.93,10.3,2.74,9.77,2.59C9.45,2.5,9.23,2.5,6.46,2.5C3.76,2.5,3.48,2.52,3.18,2.6ZM3.18,2.6M0.93,11L0.93,15.65L3.27,15.65L3.27,6.35L0.93,6.35ZM0.93,11M9.66,11L9.66,15.65L11.99,15.65L11.99,6.35L9.66,6.35ZM9.66,11M1.04,16.86C1.04,17.45,1.37,18.11,1.92,18.65C2.35,19.06,2.76,19.3,3.25,19.43C3.7,19.54,9.28,19.54,9.73,19.43C10.23,19.3,10.63,19.06,11.06,18.65C11.61,18.11,11.94,17.45,11.94,16.86L11.94,16.65L9.35,16.65L9.07,16.92C8.52,17.45,8.29,17.5,6.46,17.5C4.64,17.5,4.4,17.45,3.86,16.92L3.58,16.65L1.04,16.65ZM1.04,16.86"
        plusPath.set(PathParser.createPathFromPathData(plusPathString))

        dualTone = false
    }

    companion object {
        private const val TAG = "PortraitBatteryDrawableMx"
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