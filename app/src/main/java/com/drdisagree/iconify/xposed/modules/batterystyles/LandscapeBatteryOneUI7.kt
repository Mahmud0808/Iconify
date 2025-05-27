/*
 * Copyright (C) 2025 The RisingOS Revived Project
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

package com.drdisagree.iconify.xposed.modules.batterystyles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.SystemClock
import android.util.TypedValue
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.graphics.PathParser
import com.drdisagree.iconify.R
import kotlin.math.floor
import kotlin.math.sin

@SuppressLint("DiscouragedApi")
open class LandscapeBatteryOneUI7(private val context: Context, frameColor: Int) :
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

    // New path for battery saver icon
    private val battPath = Path()
    private val scaledBatt = Path()

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
    private var dualTone = true

    private var batteryLevel = 0

    // Animation variables
    private var animationStartTime: Long = 0
    private var animationActive: Boolean = false

    // Animation for icon and percentage slide in/out
    private var slideAnimationStartTime: Long = 0
    private var slideAnimationActive: Boolean = false
    private val interpolator = AccelerateDecelerateInterpolator()

    // Position variables for animated elements
    private var iconXOffset: Float = 0f
    private var textXOffset: Float = 0f
    private var previousState: Int = STATE_NORMAL // Track state changes to trigger animation

    private val invalidateRunnable: () -> Unit = {
        invalidateSelf()
    }

    var charging = false
        set(value) {
            if (field != value) {
                val stateChange = if (value) STATE_CHARGING else STATE_NORMAL
                if (stateChange != previousState) {
                    previousState = stateChange
                    if (value) {
                        startSlideAnimation()
                    }
                }

                field = value
                if (value) {
                    // Start animation when charging begins
                    animationStartTime = SystemClock.uptimeMillis()
                    animationActive = true
                } else {
                    // Stop animation when charging ends
                    animationActive = false
                }
                levelColor = batteryColorForLevel(batteryLevel)
                postInvalidate()
            }
        }

    override fun setChargingEnabled(charging: Boolean) {
        this.charging = charging
    }

    var powerSaveEnabled = false
        set(value) {
            if (field != value) {
                val stateChange = if (value) STATE_POWER_SAVE else STATE_NORMAL
                if (stateChange != previousState) {
                    previousState = stateChange
                    if (value) {
                        startSlideAnimation()
                    }
                }

                field = value
                levelColor = batteryColorForLevel(batteryLevel)
                postInvalidate()
            }
        }

    override fun setPowerSavingEnabled(powerSaveEnabled: Boolean) {
        this.powerSaveEnabled = powerSaveEnabled
    }

    var showPercent = true
        set(value) {
            field = value
            levelColor = batteryColorForLevel(batteryLevel)
            postInvalidate()
        }

    override fun setShowPercentEnabled(showPercent: Boolean) {
        this.showPercent = true
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
        p.color = getColorAttrDefaultColor(context, android.R.attr.colorError)
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
        val n = levels.length()
        colorLevels = IntArray(2 * n)
        for (i in 0 until n) {
            colorLevels[2 * i] = levels.getInt(i, 0)
            if (colors.getType(i) == TypedValue.TYPE_ATTRIBUTE) {
                colorLevels[2 * i + 1] = getColorAttrDefaultColor(
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

    // Time value for animation
    private var animationTime = 0f
    private val animationPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = 0xFF34C759.toInt()  // Charging green color
        p.alpha = 255
        p.isDither = true
        p.style = Paint.Style.FILL_AND_STROKE
    }

    private fun startSlideAnimation() {
        slideAnimationStartTime = android.os.SystemClock.uptimeMillis()
        slideAnimationActive = true
        postInvalidate()
    }

    override fun draw(c: Canvas) {
        c.saveLayer(null, null)
        unifiedPath.reset()
        levelPath.reset()
        levelRect.set(fillRect)
        val fillFraction = batteryLevel / 100f
        val fillTop = if (batteryLevel >= 95) fillRect.right
        else fillRect.right - (fillRect.width() * (1 - fillFraction))

        levelRect.right = floor(fillTop.toDouble()).toFloat()
        levelPath.addRoundRect(
            levelRect, floatArrayOf(
                3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f
            ), Path.Direction.CCW
        )

        // The perimeter should never change
        unifiedPath.addPath(scaledPerimeter)
        // If drawing dual tone, the level is used only to clip the whole drawable path
        if (!dualTone) {
            unifiedPath.op(levelPath, Path.Op.UNION)
        }

        fillPaint.color = levelColor

        val mergedPath = Path()
        mergedPath.reset()

        textPaint.textSize = bounds.width() * 0.42f
        val textHeight = +textPaint.fontMetrics.ascent

        // Calculate animation progress for slide in effect
        var slideProgress = 1.0f // Default to fully shown (no animation)
        if (slideAnimationActive) {
            val currentTime = android.os.SystemClock.uptimeMillis()
            val elapsedTime = currentTime - slideAnimationStartTime

            if (elapsedTime < SLIDE_ANIMATION_DURATION_MS) {
                var progress = elapsedTime / SLIDE_ANIMATION_DURATION_MS.toFloat()
                progress = interpolator.getInterpolation(progress)
                slideProgress = progress

                // Schedule next frame
                invalidateSelf()
            } else {
                slideAnimationActive = false
                slideProgress = 1.0f
            }
        }

        // Calculate base positions - where elements should end up
        val baseTextX: Float
        val baseTextY: Float
        val baseIconOffsetX: Float

        // Calculate positions with animation
        if ((charging || powerSaveEnabled) && batteryLevel < 100) {
            // Off-center position with bolt for both charging and power save mode
            baseTextX = (bounds.width() + textHeight) * 0.7f * 0.8f
            baseTextY = bounds.height() * 0.8f
            baseIconOffsetX = 0f // Icon is at its final position

            // Apply animation - text slides in from the edge
            val startX = bounds.width() * 1.5f // Off-screen position
            textXOffset = startX - (startX - baseTextX) * slideProgress
        } else {
            // Center the text when in normal state
            baseTextX = bounds.width() * 0.5f  // Center horizontally
            baseTextY =
                bounds.height() * 0.5f - ((textPaint.descent() + textPaint.ascent()) / 2)  // Center vertically
            baseIconOffsetX = 0f
            textXOffset = baseTextX
        }

        // Apply the animated positions
        val pctX = textXOffset
        val pctY = baseTextY

        val textPath = Path()
        textPath.reset()
        textPaint.getTextPath(
            batteryLevel.toString(), 0, batteryLevel.toString().length, pctX, pctY, textPath
        )

        mergedPath.addPath(textPath)

        // Add appropriate icon to the path based on state
        val iconMatrix = Matrix()

        // Handle icon animations (only forward direction)
        val leftEdgeX = -bounds.width() * 0.5f // Off-screen to the left

        if (charging) {
            // Show charging icon with animation
            // Animate bolt icon in from the left
            iconXOffset = leftEdgeX - (leftEdgeX - baseIconOffsetX) * slideProgress

            iconMatrix.setTranslate(iconXOffset, 0f)
            val animatedBolt = Path()
            scaledBolt.transform(iconMatrix, animatedBolt)
            mergedPath.addPath(animatedBolt)
        } else if (powerSaveEnabled) {
            // Show battery saver icon with animation
            // Animate battery saver icon in from the left
            iconXOffset = leftEdgeX - (leftEdgeX - baseIconOffsetX) * slideProgress

            iconMatrix.setTranslate(iconXOffset, 0f)
            val animatedBatt = Path()
            scaledBatt.transform(iconMatrix, animatedBatt)
            mergedPath.addPath(animatedBatt)
        }

        val xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        textPaint.xfermode = xfermode

        // Deal with unifiedPath clipping before it draws
        if ((charging || powerSaveEnabled) && batteryLevel < 100) {
            // Clip out the bolt shape and text
            unifiedPath.op(mergedPath, Path.Op.DIFFERENCE)

            if (!invertFillIcon) {
                c.drawPath(mergedPath, textPaint)
            }
        } else {
            // Clip out the text path only
            unifiedPath.op(textPath, Path.Op.DIFFERENCE)
            c.drawPath(textPath, textPaint)
        }

        // Handle the glow animation for charging
        if (charging && animationActive) {
            val currentTime = android.os.SystemClock.uptimeMillis()
            val elapsedTime = currentTime - animationStartTime

            if (elapsedTime < ANIMATION_DURATION_MS) {
                // Calculate animation progress (0.0 to 1.0)
                val animationProgress = (elapsedTime % 1000) / 1000f

                // Update animation time for the next frame
                animationTime = animationProgress * 2f * Math.PI.toFloat()

                // Calculate glow alpha based on sine wave for pulsing effect (100-255)
                val glowAlpha = ((sin(animationTime.toDouble()) + 1) / 2 * 155 + 100).toInt()
                animationPaint.alpha = glowAlpha

                // Draw the battery shape with the animated glow
                c.drawPath(unifiedPath, animationPaint)

                // Schedule next frame
                invalidateSelf()
            } else {
                // Animation complete after 10 seconds
                animationActive = false
            }
        }

        // Dual tone means we draw the shape again, clipped to the charge level
        c.drawPath(unifiedPath, dualToneBackgroundFill)
        c.save()
        c.clipRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left + bounds.width() * fillFraction,
            bounds.bottom.toFloat()
        )
        c.drawPath(unifiedPath, fillPaint)
        c.restore()
    }

    private fun batteryColorForLevel(level: Int): Int {
        return when {
            charging -> 0xFF34C759.toInt() // Keep the green color for charging state
            powerSaveEnabled -> 0xFFFFCC0A.toInt() // Yellow color for power save mode
            level > Companion.CRITICAL_LEVEL -> fillColor
            level >= 0 -> 0xFFFF0000.toInt()
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
    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"),
    )
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

        if ((charging && animationActive) || slideAnimationActive) {
            // When charging and animation is active, schedule the next frame
            scheduleSelf(invalidateRunnable, android.os.SystemClock.uptimeMillis() + FRAME_RATE_MS)
        } else {
            // Otherwise, invalidate immediately
            scheduleSelf(invalidateRunnable, 0)
        }
    }

    @Suppress("DEPRECATION")
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
        battPath.transform(scaleMatrix, scaledBatt)

        // It is expected that this view only ever scale by the same factor in each dimension, so
        // just pick one to scale the strokeWidths
        val scaledStrokeWidth =
            (b.right / WIDTH * PROTECTION_STROKE_WIDTH).coerceAtLeast(PROTECTION_MIN_STROKE_WIDTH)

        fillColorStrokePaint.strokeWidth = scaledStrokeWidth
        fillColorStrokeProtection.strokeWidth = scaledStrokeWidth
    }

    @Suppress("DEPRECATION")
    @SuppressLint("RestrictedApi")
    private fun loadPaths() {
        val pathString =
            getResources(context).getString(R.string.config_batterymeterLandPerimeterPathOneUI7)
        perimeterPath.set(PathParser.createPathFromPathData(pathString))
        perimeterPath.computeBounds(RectF(), true)

        val errorPathString =
            getResources(context).getString(R.string.config_batterymeterLandErrorPerimeterPathOneUI7)
        errorPerimeterPath.set(PathParser.createPathFromPathData(errorPathString))
        errorPerimeterPath.computeBounds(RectF(), true)

        val fillMaskString =
            getResources(context).getString(R.string.config_batterymeterLandFillMaskOneUI7)
        fillMask.set(PathParser.createPathFromPathData(fillMaskString))
        // Set the fill rect so we can calculate the fill properly
        fillMask.computeBounds(fillRect, true)

        val boltPathString =
            getResources(context).getString(R.string.config_batterymeterLandBoltPathOneUI7)
        boltPath.set(PathParser.createPathFromPathData(boltPathString))

        val plusPathString =
            getResources(context).getString(R.string.config_batterymeterLandPowersavePathOneUI7)
        plusPath.set(PathParser.createPathFromPathData(plusPathString))

        // Load the new battery saver icon path
        val battPathString =
            getResources(context).getString(R.string.config_batterymeterLandLeafPathOneUI7)
        battPath.set(PathParser.createPathFromPathData(battPathString))

        dualTone = true
    }

    companion object {
        private val TAG = LandscapeBatteryOneUI7::class.java.simpleName
        private const val WIDTH = 24f
        private const val HEIGHT = 12f
        private const val CRITICAL_LEVEL = 15

        // Constants for animation
        private const val FRAME_RATE_MS: Long = 16 // Animation frame rate in milliseconds (60fps)
        private const val ANIMATION_DURATION_MS: Long = 10000 // 10 seconds duration
        private const val SLIDE_ANIMATION_DURATION_MS: Long = 500 // 500ms for slide animation

        // On a 12x20 grid, how wide to make the fill protection stroke.
        // Scales when our size changes
        private const val PROTECTION_STROKE_WIDTH = 3f

        // Arbitrarily chosen for visibility at small sizes
        private const val PROTECTION_MIN_STROKE_WIDTH = 6f

        // State constants to track animation transitions
        private const val STATE_NORMAL = 0
        private const val STATE_CHARGING = 1
        private const val STATE_POWER_SAVE = 2
    }
}