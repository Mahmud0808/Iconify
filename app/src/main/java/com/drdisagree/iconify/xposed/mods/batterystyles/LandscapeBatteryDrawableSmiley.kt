package com.drdisagree.iconify.xposed.mods.batterystyles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import androidx.core.graphics.PathParser
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils

@SuppressLint("DiscouragedApi")
open class LandscapeBatteryDrawableSmiley(private val context: Context, frameColor: Int) :
    BatteryDrawable() {

    // Need to load:
    // 1. perimeter shape
    // 2. fill mask (if smaller than perimeter, this would create a fill that
    //    doesn't touch the walls
    private val perimeterPath = Path()
    private val scaledPerimeter = Path()
    private val errorPerimeterPath = Path()
    private val scaledErrorPerimeter = Path()
    private val scaledSmileyHigh = Path()
    private val scaledSmileyMid = Path()
    private val scaledSmileyLow = Path()

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

    // Smiley sign (used based on level)
    private val smileyHighPath = Path()
    private val smileyMidPath = Path()
    private val smileyLowPath = Path()

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

    // Only used if charging
    private val fillPaintCharging = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = 0xFF34C759.toInt()
        p.alpha = 255
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
            c.drawPath(scaledPerimeter, fillPaint)
            if (charging) c.drawPath(levelPath, fillPaintCharging)
            else c.drawPath(levelPath, dualToneBackgroundFill)
            fillPaint.color = levelColor

            // Show colorError below this level
            if (batteryLevel <= Companion.CRITICAL_LEVEL && !charging) {
                c.save()
                c.clipPath(scaledFill)
                c.drawPath(levelPath, fillPaint)
                c.restore()
            }

            if (charging || batteryLevel >= 75) c.drawPath(scaledSmileyHigh, fillPaint)
            else if (batteryLevel > 25) c.drawPath(scaledSmileyMid, fillPaint)
            else c.drawPath(scaledSmileyLow, fillPaint)
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
        smileyHighPath.transform(scaleMatrix, scaledSmileyHigh)
        smileyMidPath.transform(scaleMatrix, scaledSmileyMid)
        smileyLowPath.transform(scaleMatrix, scaledSmileyLow)

        // It is expected that this view only ever scale by the same factor in each dimension, so
        // just pick one to scale the strokeWidths
        val scaledStrokeWidth =

            Math.max(b.right / WIDTH * PROTECTION_STROKE_WIDTH, PROTECTION_MIN_STROKE_WIDTH)

        fillColorStrokePaint.strokeWidth = scaledStrokeWidth
        fillColorStrokeProtection.strokeWidth = scaledStrokeWidth
    }

    private fun loadPaths() {
        val pathString =
            "M3.76,0.62L18.03,0.62A2.74 2.74 0 0 1 20.78,3.36L20.78,8.64A2.74 2.74 0 0 1 18.03,11.38L3.76,11.38A2.74 2.74 0 0 1 1.02,8.64L1.02,3.36A2.74 2.74 0 0 1 3.76,0.62zM21.66,7.79C23.42,7.68,23.42,4.32,21.66,4.21L21.66,7.79zM1.93,3.37L1.93,8.66A1.85 1.85 0 0 0 3.78,10.51L18.04,10.51A1.85 1.85 0 0 0 19.89,8.66L19.89,3.37A1.85 1.85 0 0 0 18.04,1.52L3.78,1.52A1.85 1.85 0 0 0 1.93,3.37z"
        perimeterPath.set(PathParser.createPathFromPathData(pathString))
        perimeterPath.computeBounds(RectF(), true)

        val errorPathString =
            "M3.76,0.62L18.03,0.62A2.74 2.74 0 0 1 20.78,3.36L20.78,8.64A2.74 2.74 0 0 1 18.03,11.38L3.76,11.38A2.74 2.74 0 0 1 1.02,8.64L1.02,3.36A2.74 2.74 0 0 1 3.76,0.62zM21.66,7.79C23.42,7.68,23.42,4.32,21.66,4.21L21.66,7.79zM1.93,3.37L1.93,8.66A1.85 1.85 0 0 0 3.78,10.51L18.04,10.51A1.85 1.85 0 0 0 19.89,8.66L19.89,3.37A1.85 1.85 0 0 0 18.04,1.52L3.78,1.52A1.85 1.85 0 0 0 1.93,3.37z"
        errorPerimeterPath.set(PathParser.createPathFromPathData(errorPathString))
        errorPerimeterPath.computeBounds(RectF(), true)

        val fillMaskString =
            "M2.83,3.78L2.83,8.21A1.38 1.38 0 0 0 4.21,9.59L17.59,9.59A1.38 1.38 0 0 0 18.97,8.21L18.97,3.78A1.38 1.38 0 0 0 17.59,2.40L4.21,2.40A1.38 1.38 0 0 0 2.83,3.78z"
        fillMask.set(PathParser.createPathFromPathData(fillMaskString))
        // Set the fill rect so we can calculate the fill properly
        fillMask.computeBounds(fillRect, true)

        val boltPathString = ""
        boltPath.set(PathParser.createPathFromPathData(boltPathString))

        val plusPathString =
            "M3.76,0.62L18.03,0.62A2.74 2.74 0 0 1 20.78,3.36L20.78,8.64A2.74 2.74 0 0 1 18.03,11.38L3.76,11.38A2.74 2.74 0 0 1 1.02,8.64L1.02,3.36A2.74 2.74 0 0 1 3.76,0.62zM21.66,7.79C23.42,7.68,23.42,4.32,21.66,4.21L21.66,7.79zM1.93,3.37L1.93,8.66A1.85 1.85 0 0 0 3.78,10.51L18.04,10.51A1.85 1.85 0 0 0 19.89,8.66L19.89,3.37A1.85 1.85 0 0 0 18.04,1.52L3.78,1.52A1.85 1.85 0 0 0 1.93,3.37z"
        plusPath.set(PathParser.createPathFromPathData(plusPathString))

        val smileyHighPathString =
            "M12.21,6.01C12.05,7.95,9.45,7.95,9.29,6.01C9.34,5.36,10.21,5.36,10.27,6.01C10.22,6.66,11.28,6.66,11.23,6.01C11.29,5.36,12.16,5.36,12.21,6.01zM15.62,5.03C16.16,5.03,16.59,5.47,16.59,6.01C16.59,6.54,16.16,6.98,15.62,6.98C15.08,6.98,14.65,6.54,14.65,6.01C14.65,5.47,15.08,5.03,15.62,5.03zM5.88,5.03C6.42,5.03,6.86,5.47,6.86,6.01C6.86,6.54,6.42,6.98,5.88,6.98C5.34,6.98,4.91,6.54,4.91,6.01C4.91,5.47,5.34,5.03,5.88,5.03z"
        smileyHighPath.set(PathParser.createPathFromPathData(smileyHighPathString))

        val smileyMidPathString =
            "M9.44,6.57L9.44,6.57A0.45 0.45 0 0 0 9.89,7.02L11.81,7.02A0.45 0.45 0 0 0 12.26,6.57L12.26,6.57A0.45 0.45 0 0 0 11.81,6.12L9.89,6.12A0.45 0.45 0 0 0 9.44,6.57zM15.33,5.11C15.83,5.11,16.23,5.51,16.23,6.01C16.23,6.50,15.83,6.90,15.33,6.90C14.84,6.90,14.44,6.50,14.44,6.01C14.44,5.51,14.84,5.11,15.33,5.11zM6.36,5.11C6.86,5.11,7.26,5.51,7.26,6.01C7.26,6.50,6.86,6.90,6.36,6.90C5.87,6.90,5.47,6.50,5.47,6.01C5.47,5.51,5.87,5.11,6.36,5.11z"
        smileyMidPath.set(PathParser.createPathFromPathData(smileyMidPathString))

        val smileyLowPathString =
            "M9.50,6.90C9.65,5.11,12.05,5.11,12.19,6.90C12.15,7.50,11.35,7.50,11.29,6.90C11.33,6.30,10.36,6.30,10.40,6.90C10.35,7.50,9.55,7.50,9.50,6.90zM15.33,5.11C15.83,5.11,16.23,5.51,16.23,6.01C16.23,6.50,15.83,6.90,15.33,6.90C14.84,6.90,14.44,6.50,14.44,6.01C14.44,5.51,14.84,5.11,15.33,5.11zM6.36,5.11C6.86,5.11,7.26,5.51,7.26,6.01C7.26,6.50,6.86,6.90,6.36,6.90C5.87,6.90,5.47,6.50,5.47,6.01C5.47,5.51,5.87,5.11,6.36,5.11z"
        smileyLowPath.set(PathParser.createPathFromPathData(smileyLowPathString))

        dualTone = false
    }

    companion object {
        private const val TAG = "LandscapeBatteryDrawableSmiley"
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