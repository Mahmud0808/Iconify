package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs

class ParallaxImageView : AppCompatImageView, SensorEventListener {

    private var mMovementMultiplier = DEFAULT_MOVEMENT_MULTIPLIER.toFloat()
    private var mSensorDelay = DEFAULT_SENSOR_DELAY
    private var mMinMovedPixelsToUpdate = DEFAULT_MIN_MOVED_PIXELS
    private var mMinSensibility = DEFAULT_MIN_SENSIBILITY

    private var mSensorX = 0f
    private var mSensorY = 0f

    private var mFirstSensorX: Float? = null
    private var mFirstSensorY: Float? = null

    private var mPreviousSensorX: Float? = null
    private var mPreviousSensorY: Float? = null

    private var mTranslationX = 0f
    private var mTranslationY = 0f

    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null

    enum class SensorDelay {
        FASTEST,
        GAME,
        UI,
        NORMAL
    }

    constructor(context: Context) : super(
        context
    )

    constructor(context: Context, attrs: AttributeSet?) : super(
        context, attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        mSensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun setNewPosition() {
        val deltaX = (mFirstSensorX!! - mSensorX) * mMovementMultiplier
        val deltaY = (mFirstSensorY!! - mSensorY) * mMovementMultiplier

        calculateTranslation(deltaX, deltaY)
    }

    private fun calculateTranslation(deltaX: Float, deltaY: Float) {
        if (mTranslationX + mMinMovedPixelsToUpdate < deltaX) {
            mTranslationX++
        } else if (mTranslationX - mMinMovedPixelsToUpdate > deltaX) {
            mTranslationX--
        }

        if (mTranslationY + mMinMovedPixelsToUpdate < deltaY) {
            mTranslationY++
        } else if (mTranslationY - mMinMovedPixelsToUpdate > deltaY) {
            mTranslationY--
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        translationX = mTranslationX
        translationY = mTranslationY

        invalidate()
    }

    private fun resetPosition() {
        mTranslationX = 0f
        mTranslationY = 0f

        invalidate()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            mSensorX = event.values[0]
            mSensorY = -event.values[1]

            manageSensorValues()
        }
    }

    private fun manageSensorValues() {
        if (mFirstSensorX == null) {
            setFirstSensorValues()
        }

        if (mPreviousSensorX == null || isSensorValuesMovedEnough) {
            setNewPosition()
            setPreviousSensorValues()
        }
    }

    private fun setFirstSensorValues() {
        mFirstSensorX = mSensorX
        mFirstSensorY = mSensorY
    }

    private fun setPreviousSensorValues() {
        mPreviousSensorX = mSensorX
        mPreviousSensorY = mSensorY
    }

    private val isSensorValuesMovedEnough: Boolean
        get() = abs(mSensorX - mPreviousSensorX!!) > mMinSensibility ||
                abs(mSensorY - mPreviousSensorY!!) > mMinSensibility

    fun registerSensorListener() {
        if (mAccelerometer != null) {
            mSensorManager?.registerListener(this, mAccelerometer, mSensorDelay)
        }
    }

    fun registerSensorListener(sensorDelay: SensorDelay) {
        mSensorDelay = when (sensorDelay) {
            SensorDelay.FASTEST -> SensorManager.SENSOR_DELAY_FASTEST
            SensorDelay.GAME -> SensorManager.SENSOR_DELAY_GAME
            SensorDelay.UI -> SensorManager.SENSOR_DELAY_UI
            SensorDelay.NORMAL -> SensorManager.SENSOR_DELAY_NORMAL
        }

        registerSensorListener()
    }

    fun unregisterSensorListener() {
        mSensorManager?.unregisterListener(this)

        resetPosition()
    }

    fun setMovementMultiplier(multiplier: Float) {
        mMovementMultiplier = multiplier
    }

    fun setMinimumMovedPixelsToUpdate(minMovedPixelsToUpdate: Int) {
        mMinMovedPixelsToUpdate = minMovedPixelsToUpdate
    }

    fun setMinimumSensibility(minSensibility: Int) {
        mMinSensibility = minSensibility.toFloat()
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}

    companion object {
        private const val DEFAULT_SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST
        const val DEFAULT_MOVEMENT_MULTIPLIER = 3
        const val DEFAULT_MIN_MOVED_PIXELS = 1
        private const val DEFAULT_MIN_SENSIBILITY = 0f
    }
}