package com.drdisagree.iconify.xposed.modules.batterystyles

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.Typeface
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.PathParser
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.modules.utils.AlphaRefreshedPaint

@Suppress("UNUSED_PARAMETER")
open class CircleBattery(private val mContext: Context, frameColor: Int) : BatteryDrawable() {

    private var mChargingColor = -0xcb38a7
    private var mPowerSaveColor = -0x5b00
    private var mShowPercentage = false
    private var mDiameter = 0
    private val mFrame = RectF()
    private var mFGColor = Color.WHITE
    private val mTextPaint: Paint = AlphaRefreshedPaint(Paint.ANTI_ALIAS_FLAG)
    private val mFramePaint: Paint = AlphaRefreshedPaint(Paint.ANTI_ALIAS_FLAG)
    private val mBatteryPaint: Paint = AlphaRefreshedPaint(Paint.ANTI_ALIAS_FLAG)
    private val mWarningTextPaint: Paint = AlphaRefreshedPaint(Paint.ANTI_ALIAS_FLAG)
    private val mBoltPaint: Paint = AlphaRefreshedPaint(Paint.ANTI_ALIAS_FLAG)
    private val mBoltAlphaAnimator: ValueAnimator
    private var mShadeColors: IntArray? = null
    private var mShadeLevels: FloatArray? = null
    private var mBoltPath: Path? = null
    private var mAlphaPct = 0f
    private var powerSaveEnabled = false
    private var charging = false
    private var batteryLevel = 0
    private var batteryColors: IntArray? = null
    private var batteryLevels: List<Int>? = null

    override fun setShowPercentEnabled(showPercent: Boolean) {
        mShowPercentage = showPercent
        postInvalidate()
    }

    override fun setChargingEnabled(charging: Boolean) {
        this.charging = charging
        postInvalidate()
    }

    override fun setBatteryLevel(mLevel: Int) {
        batteryLevel = mLevel
        invalidateSelf()
    }

    fun setMeterStyle(batteryStyle: Int) {
        mFramePaint.setPathEffect(if (batteryStyle == Preferences.BATTERY_STYLE_DOTTED_CIRCLE) DASH_PATH_EFFECT else null)
        mBatteryPaint.setPathEffect(if (batteryStyle == Preferences.BATTERY_STYLE_DOTTED_CIRCLE) DASH_PATH_EFFECT else null)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        updateSize()
    }

    override fun setPowerSavingEnabled(powerSaveEnabled: Boolean) {
        this.powerSaveEnabled = powerSaveEnabled
        postInvalidate()
    }

    override fun setColors(fgColor: Int, bgColor: Int, singleToneColor: Int) {
        mFGColor = fgColor
        mBoltPaint.setColor(mFGColor)
        mFramePaint.setColor(bgColor)
        mTextPaint.setColor(mFGColor)
        initColors()
        invalidateSelf()
    }

    private fun initColors() {
        customBlendColor = try {
            Xprefs?.getBoolean(Preferences.CUSTOM_BATTERY_BLEND_COLOR, false) ?: false
        } catch (ignored: Throwable) {
            getBoolean(Preferences.CUSTOM_BATTERY_BLEND_COLOR, false)
        }

        mChargingColor = if (customBlendColor && chargingColor != Color.BLACK) {
            chargingColor
        } else {
            -0xcb38a7
        }

        mPowerSaveColor = if (customBlendColor && powerSaveFillColor != Color.BLACK) {
            powerSaveFillColor
        } else {
            getColorAttrDefaultColor(android.R.attr.colorError, mContext)
        }

        @ColorInt val fillColor = customFillColor
        @ColorInt val fillGradColor = customFillGradColor

        batteryColors = if (customBlendColor) {
            if (fillColor != Color.BLACK && fillGradColor != Color.BLACK) {
                intArrayOf(fillGradColor, ColorUtils.blendARGB(fillColor, Color.WHITE, 0.4f))
            } else if (fillColor != Color.BLACK) {
                intArrayOf(Color.RED, ColorUtils.blendARGB(fillColor, Color.WHITE, 0.4f))
            } else if (fillGradColor != Color.BLACK) {
                intArrayOf(fillGradColor, Color.YELLOW)
            } else {
                intArrayOf(Color.RED, Color.YELLOW)
            }
        } else {
            intArrayOf(mFGColor, mFGColor)
        }

        batteryLevels = if (customBlendColor) {
            listOf(10, 30)
        } else {
            listOf(0, 0)
        }
    }

    private fun refreshShadeColors() {
        if (batteryColors == null || batteryLevels == null) return

        initColors()

        mShadeColors = IntArray(batteryLevels!!.size * 2 + 2)
        mShadeLevels = FloatArray(mShadeColors!!.size)

        var lastPCT = 0f
        for (i in batteryLevels!!.indices) {
            val rangeLength = batteryLevels!![i] - lastPCT
            val pointer = 2 * i
            mShadeLevels!![pointer] = (lastPCT + rangeLength * 0.3f) / 100
            mShadeColors!![pointer] = batteryColors!![i]
            mShadeLevels!![pointer + 1] = (batteryLevels!![i] - rangeLength * 0.3f) / 100
            mShadeColors!![pointer + 1] = batteryColors!![i]
            lastPCT = batteryLevels!![i].toFloat()
        }

        @ColorInt val fillColor = customFillColor
        mShadeLevels!![mShadeLevels!!.size - 2] =
            (batteryLevels!![batteryLevels!!.size - 1] + (100 - batteryLevels!![batteryLevels!!.size - 1] * 0.3f)) / 100
        mShadeColors!![mShadeColors!!.size - 2] =
            if (customBlendColor) if (fillColor != Color.BLACK) fillColor else Color.GREEN else mFGColor
        mShadeLevels!![mShadeLevels!!.size - 1] = 1f
        mShadeColors!![mShadeColors!!.size - 1] =
            if (customBlendColor) if (fillColor != Color.BLACK) fillColor else Color.GREEN else mFGColor
    }

    override fun draw(canvas: Canvas) {
        if (batteryLevel < 0 || mDiameter == 0) return

        refreshShadeColors()
        setLevelBasedColors(mBatteryPaint, mFrame.centerX(), mFrame.centerY())

        if (charging && batteryLevel < 100) {
            if (!mBoltAlphaAnimator.isStarted) {
                mBoltAlphaAnimator.start()
            }
            mBoltPaint.setAlpha(Math.round(mBoltAlphaAnimator.getAnimatedValue() as Int * mAlphaPct))
            canvas.drawPath(mBoltPath!!, mBoltPaint)
        } else if (mBoltAlphaAnimator.isStarted) {
            mBoltAlphaAnimator.end()
        }

        canvas.drawArc(mFrame, 270f, 360f, false, mFramePaint)

        if (batteryLevel > 0) {
            canvas.drawArc(mFrame, 270f, 3.6f * batteryLevel, false, mBatteryPaint)
        }

        if (!charging && batteryLevel < 100 && mShowPercentage) {
            val pctText =
                if (batteryLevel > CRITICAL_LEVEL) batteryLevel.toString() else WARNING_STRING
            val textHeight = -mTextPaint.getFontMetrics().ascent
            val pctX = mDiameter * .5f
            val pctY = (mDiameter + textHeight) * 0.47f
            canvas.drawText(pctText, pctX, pctY, mTextPaint)
        }
    }

    private fun setLevelBasedColors(paint: Paint, centerX: Float, centerY: Float) {
        paint.setShader(null)

        if (powerSaveEnabled) {
            paint.setColor(mPowerSaveColor)
            return
        } else if (charging) {
            paint.setColor(mChargingColor)
            return
        }

        if (mShadeColors == null) {
            for (i in batteryLevels!!.indices) {
                if (batteryLevel <= batteryLevels!![i]) {
                    if (i > 0) {
                        val range = (batteryLevels!![i] - batteryLevels!![i - 1]).toFloat()
                        val currentPos = (batteryLevel - batteryLevels!![i - 1]).toFloat()
                        val ratio = currentPos / range
                        paint.setColor(
                            ColorUtils.blendARGB(
                                batteryColors!![i - 1],
                                batteryColors!![i],
                                ratio
                            )
                        )
                    } else {
                        paint.setColor(batteryColors!![i])
                    }
                    return
                }
            }
            paint.setColor(mFGColor)
        } else {
            val shader = SweepGradient(centerX, centerY, mShadeColors!!, mShadeLevels)
            val shaderMatrix = Matrix()
            shaderMatrix.preRotate(270f, centerX, centerY)
            shader.setLocalMatrix(shaderMatrix)
            paint.setShader(shader)
        }
    }

    override fun setAlpha(alpha: Int) {
        mAlphaPct = alpha / 255f
        mFramePaint.setAlpha(Math.round(70 * alpha / 255f))
        mTextPaint.setAlpha(alpha)
        mBatteryPaint.setAlpha(alpha)
    }

    @SuppressLint("DiscouragedApi", "RestrictedApi")
    private fun updateSize() {
        val res = mContext.resources
        mDiameter = getBounds().bottom - getBounds().top
        mWarningTextPaint.textSize = mDiameter * 0.75f
        val strokeWidth = mDiameter / 6.5f
        mFramePaint.strokeWidth = strokeWidth
        mBatteryPaint.strokeWidth = strokeWidth
        mTextPaint.textSize = mDiameter * 0.52f
        mFrame[strokeWidth / 2.0f, strokeWidth / 2.0f, mDiameter - strokeWidth / 2.0f] =
            mDiameter - strokeWidth / 2.0f
        @SuppressLint("DiscouragedApi") val unscaledBoltPath = Path()
        unscaledBoltPath.set(
            PathParser.createPathFromPathData(
                res.getString(
                    res.getIdentifier(
                        "android:string/config_batterymeterBoltPath",
                        "string",
                        "android"
                    )
                )
            )
        )

        //Bolt icon
        val scaleMatrix = Matrix()
        val pathBounds = RectF()
        unscaledBoltPath.computeBounds(pathBounds, true)
        val scaleF =
            (getBounds().height() - strokeWidth * 2) * .8f / pathBounds.height() //scale comparing to 80% of icon's inner space
        scaleMatrix.setScale(scaleF, scaleF)
        mBoltPath = Path()
        unscaledBoltPath.transform(scaleMatrix, mBoltPath)
        mBoltPath!!.computeBounds(pathBounds, true)

        //moving it to center
        mBoltPath!!.offset(
            getBounds().centerX() - pathBounds.centerX(),
            getBounds().centerY() - pathBounds.centerY()
        )
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mFramePaint.setColorFilter(colorFilter)
        mBatteryPaint.setColorFilter(colorFilter)
        mWarningTextPaint.setColorFilter(colorFilter)
        mBoltPaint.setColorFilter(colorFilter)
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.UNKNOWN", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    override fun getIntrinsicHeight(): Int {
        return CIRCLE_DIAMETER
    }

    override fun getIntrinsicWidth(): Int {
        return CIRCLE_DIAMETER
    }

    private val invalidateRunnable = Runnable { invalidateSelf() }

    init {
        mFramePaint.isDither = true
        mFramePaint.style = Paint.Style.STROKE
        mTextPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD))
        mTextPaint.textAlign = Align.CENTER
        mWarningTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD))
        mWarningTextPaint.textAlign = Align.CENTER
        mBatteryPaint.isDither = true
        mBatteryPaint.style = Paint.Style.STROKE

        try {
            Xprefs?.let { setMeterStyle(it.getInt(Preferences.CUSTOM_BATTERY_STYLE, 0)) }
        } catch (ignored: Throwable) {
            setMeterStyle(Preferences.BATTERY_STYLE_CIRCLE)
        }

        mBoltAlphaAnimator = ValueAnimator.ofInt(255, 255, 255, 45)
        mBoltAlphaAnimator.setDuration(2000)
        mBoltAlphaAnimator.interpolator = FastOutSlowInInterpolator()
        mBoltAlphaAnimator.repeatMode = ValueAnimator.REVERSE
        mBoltAlphaAnimator.repeatCount = ValueAnimator.INFINITE
        mBoltAlphaAnimator.addUpdateListener { invalidateSelf() }
    }

    private fun postInvalidate() {
        unscheduleSelf(invalidateRunnable)
        scheduleSelf(invalidateRunnable, 0)
    }

    companion object {
        private const val WARNING_STRING = "!"
        private const val CRITICAL_LEVEL = 5
        private const val CIRCLE_DIAMETER =
            45 //relative to dash effect size. Size doesn't matter as finally it gets scaled by parent
        private val DASH_PATH_EFFECT: PathEffect = DashPathEffect(floatArrayOf(3f, 2f), 0f)
    }
}
