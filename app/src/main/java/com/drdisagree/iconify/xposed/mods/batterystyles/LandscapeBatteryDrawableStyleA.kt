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
open class LandscapeBatteryDrawableStyleA(private val context: Context, frameColor: Int) :
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
        val fillTop = if (batteryLevel >= 95) fillRect.left
        else fillRect.left + (fillRect.width() * (1 - fillFraction))

        levelRect.left = Math.floor(fillTop.toDouble()).toFloat()
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
                bounds.left - bounds.width() * fillFraction,
                0f,
                bounds.right.toFloat(),
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
                fillRect.right,
                fillRect.top,
                fillRect.left + (fillRect.width() * (1 - fillFraction)),
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
            "M2.25,6L2.25,9.01L2.16,9C1.82,8.97,1.54,8.88,1.25,8.69C1.1,8.59,1.01,8.52,0.88,8.38C0.56,8.06,0.34,7.68,0.18,7.18C-0.22,5.94,0.06,4.47,0.86,3.64C1.24,3.25,1.67,3.04,2.15,3L2.25,3L2.25,6Z,M20.26,0.01C20.58,0.04,20.91,0.12,21.2,0.25C21.65,0.44,22.07,0.76,22.38,1.16C22.74,1.62,22.96,2.19,23,2.78C23.02,2.95,23.02,9.07,23,9.23C22.97,9.62,22.87,9.98,22.7,10.33C22.41,10.92,21.96,11.37,21.38,11.67C21.12,11.81,20.86,11.9,20.58,11.95C20.29,12,20.78,12,13,12C7.06,12,5.77,12,5.68,11.99C4.64,11.87,3.76,11.25,3.31,10.33C3.15,10,3.06,9.68,3.02,9.32C3,9.15,3,2.85,3.02,2.68C3.06,2.32,3.15,2,3.32,1.66C3.56,1.18,3.9,0.79,4.35,0.49C4.78,0.21,5.24,0.06,5.77,0.01C5.91,-0,20.12,-0,20.26,0.01ZM5.96,1.26C5.33,1.33,4.78,1.7,4.48,2.26C4.41,2.39,4.35,2.54,4.32,2.68C4.26,2.93,4.26,2.74,4.26,6C4.26,7.97,4.26,9,4.27,9.06C4.35,9.76,4.8,10.36,5.45,10.62C5.57,10.67,5.7,10.7,5.84,10.73L5.96,10.75L13.01,10.75L20.06,10.75L20.19,10.73C21.04,10.57,21.68,9.89,21.76,9.04C21.77,8.97,21.77,8.02,21.77,5.93L21.77,2.92L21.74,2.81C21.64,2.28,21.35,1.85,20.91,1.56C20.67,1.4,20.37,1.29,20.08,1.26C19.96,1.25,6.08,1.25,5.96,1.26Z"
        perimeterPath.set(PathParser.createPathFromPathData(pathString))
        perimeterPath.computeBounds(RectF(), true)

        val errorPathString =
            "M2.25,6L2.25,9.01L2.16,9C1.82,8.97,1.54,8.88,1.25,8.69C1.1,8.59,1.01,8.52,0.88,8.38C0.56,8.06,0.34,7.68,0.18,7.18C-0.22,5.94,0.06,4.47,0.86,3.64C1.24,3.25,1.67,3.04,2.15,3L2.25,3L2.25,6Z,M20.26,0.01C20.58,0.04,20.91,0.12,21.2,0.25C21.65,0.44,22.07,0.76,22.38,1.16C22.74,1.62,22.96,2.19,23,2.78C23.02,2.95,23.02,9.07,23,9.23C22.97,9.62,22.87,9.98,22.7,10.33C22.41,10.92,21.96,11.37,21.38,11.67C21.12,11.81,20.86,11.9,20.58,11.95C20.29,12,20.78,12,13,12C7.06,12,5.77,12,5.68,11.99C4.64,11.87,3.76,11.25,3.31,10.33C3.15,10,3.06,9.68,3.02,9.32C3,9.15,3,2.85,3.02,2.68C3.06,2.32,3.15,2,3.32,1.66C3.56,1.18,3.9,0.79,4.35,0.49C4.78,0.21,5.24,0.06,5.77,0.01C5.91,-0,20.12,-0,20.26,0.01ZM5.96,1.26C5.33,1.33,4.78,1.7,4.48,2.26C4.41,2.39,4.35,2.54,4.32,2.68C4.26,2.93,4.26,2.74,4.26,6C4.26,7.97,4.26,9,4.27,9.06C4.35,9.76,4.8,10.36,5.45,10.62C5.57,10.67,5.7,10.7,5.84,10.73L5.96,10.75L13.01,10.75L20.06,10.75L20.19,10.73C21.04,10.57,21.68,9.89,21.76,9.04C21.77,8.97,21.77,8.02,21.77,5.93L21.77,2.92L21.74,2.81C21.64,2.28,21.35,1.85,20.91,1.56C20.67,1.4,20.37,1.29,20.08,1.26C19.96,1.25,6.08,1.25,5.96,1.26Z"
        errorPerimeterPath.set(PathParser.createPathFromPathData(errorPathString))
        errorPerimeterPath.computeBounds(RectF(), true)

        val fillMaskString =
            "M19.67,1.75C19.84,1.77,19.98,1.8,20.13,1.85C20.73,2.07,21.15,2.58,21.24,3.21C21.26,3.35,21.26,8.67,21.24,8.79C21.13,9.49,20.64,10.04,19.96,10.2C19.77,10.25,20.15,10.25,13,10.25C7.45,10.25,6.29,10.25,6.21,10.24C5.7,10.16,5.26,9.87,4.99,9.42C4.89,9.26,4.8,9.02,4.77,8.82C4.75,8.72,4.75,8.52,4.75,6.01C4.75,3.8,4.76,3.29,4.77,3.21C4.86,2.58,5.28,2.07,5.88,1.85C5.98,1.81,6.06,1.79,6.18,1.77C6.26,1.76,6.79,1.75,12.95,1.75C16.63,1.75,19.65,1.75,19.67,1.75Z"
        fillMask.set(PathParser.createPathFromPathData(fillMaskString))
        // Set the fill rect so we can calculate the fill properly
        fillMask.computeBounds(fillRect, true)

        val boltPathString =
            "M10.45,6.77L12.7,6.77L11.53,9.81C11.36,10.26,11.82,10.49,12.12,10.13L15.77,5.78C15.84,5.7,15.88,5.61,15.88,5.52C15.88,5.35,15.75,5.23,15.56,5.23L13.31,5.23L14.48,2.19C14.65,1.75,14.19,1.51,13.9,1.87L10.25,6.2C10.17,6.3,10.13,6.39,10.13,6.48C10.13,6.65,10.27,6.77,10.45,6.77Z"
        boltPath.set(PathParser.createPathFromPathData(boltPathString))

        val plusPathString =
            "M2.25,6L2.25,9.01L2.16,9C1.82,8.97,1.54,8.88,1.25,8.69C1.1,8.59,1.01,8.52,0.88,8.38C0.56,8.06,0.34,7.68,0.18,7.18C-0.22,5.94,0.06,4.47,0.86,3.64C1.24,3.25,1.67,3.04,2.15,3L2.25,3L2.25,6Z,M20.26,0.01C20.58,0.04,20.91,0.12,21.2,0.25C21.65,0.44,22.07,0.76,22.38,1.16C22.74,1.62,22.96,2.19,23,2.78C23.02,2.95,23.02,9.07,23,9.23C22.97,9.62,22.87,9.98,22.7,10.33C22.41,10.92,21.96,11.37,21.38,11.67C21.12,11.81,20.86,11.9,20.58,11.95C20.29,12,20.78,12,13,12C7.06,12,5.77,12,5.68,11.99C4.64,11.87,3.76,11.25,3.31,10.33C3.15,10,3.06,9.68,3.02,9.32C3,9.15,3,2.85,3.02,2.68C3.06,2.32,3.15,2,3.32,1.66C3.56,1.18,3.9,0.79,4.35,0.49C4.78,0.21,5.24,0.06,5.77,0.01C5.91,-0,20.12,-0,20.26,0.01ZM5.96,1.26C5.33,1.33,4.78,1.7,4.48,2.26C4.41,2.39,4.35,2.54,4.32,2.68C4.26,2.93,4.26,2.74,4.26,6C4.26,7.97,4.26,9,4.27,9.06C4.35,9.76,4.8,10.36,5.45,10.62C5.57,10.67,5.7,10.7,5.84,10.73L5.96,10.75L13.01,10.75L20.06,10.75L20.19,10.73C21.04,10.57,21.68,9.89,21.76,9.04C21.77,8.97,21.77,8.02,21.77,5.93L21.77,2.92L21.74,2.81C21.64,2.28,21.35,1.85,20.91,1.56C20.67,1.4,20.37,1.29,20.08,1.26C19.96,1.25,6.08,1.25,5.96,1.26Z"
        plusPath.set(PathParser.createPathFromPathData(plusPathString))

        dualTone = false
    }

    companion object {
        private const val TAG = "LandscapeBatteryDrawableStyleA"
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