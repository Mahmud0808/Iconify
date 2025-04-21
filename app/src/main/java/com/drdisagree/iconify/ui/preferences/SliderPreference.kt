package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.HapticUtils.weakVibrate
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.RangeSlider
import java.io.StringReader
import java.io.StringWriter
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Locale
import java.util.Scanner
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/*
* From Siavash79/rangesliderpreference
* https://github.com/siavash79/rangesliderpreference
*/

class SliderPreference(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : Preference(context, attrs, defStyleAttr) {

    private var valueFrom: Float
    private var valueTo: Float
    private val tickVisible: Boolean
    private val tickInterval: Float
    private val showResetButton: Boolean
    private val showController: Boolean
    private val defaultValue: MutableList<Float> = ArrayList()
    private var mSlider: RangeSlider? = null
    private var mTitleView: TextView? = null
    private var mSummaryView: TextView? = null
    private var mMinusButton: MaterialButton? = null
    private var mPlusButton: MaterialButton? = null
    private var mResetButton: MaterialButton? = null

    @Suppress("unused")
    private var sliderValue: TextView? = null
    private var valueCount: Int
    private var valueFormat: String? = null
    private val outputScale: Float
    private val isDecimalFormat: Boolean
    private val showDefaultIndicator: Boolean
    private var decimalFormat: String? = "#.#"

    var updateConstantly: Boolean
    var showValueLabel: Boolean

    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        isSelectable = false
        layoutResource = R.layout.custom_preference_slider

        context.obtainStyledAttributes(attrs, R.styleable.SliderPreference).apply {
            updateConstantly = getBoolean(R.styleable.SliderPreference_updatesContinuously, false)
            valueCount = getInt(R.styleable.SliderPreference_valueCount, 1)
            valueFrom = getFloat(R.styleable.SliderPreference_minVal, 0.0f)
            valueTo = getFloat(R.styleable.SliderPreference_maxVal, 100.0f)
            tickInterval = getFloat(R.styleable.SliderPreference_tickInterval, 0.5f)
            showResetButton = getBoolean(R.styleable.SliderPreference_showResetButton, false)
            showValueLabel = getBoolean(R.styleable.SliderPreference_showValueLabel, true)
            valueFormat = getString(R.styleable.SliderPreference_valueFormat)
            isDecimalFormat = getBoolean(R.styleable.SliderPreference_isDecimalFormat, false)
            tickVisible = getBoolean(
                R.styleable.SliderPreference_tickVisible,
                abs(valueTo - valueFrom) <= 25
            )
            showController = getBoolean(R.styleable.SliderPreference_showController, false)
            decimalFormat = if (hasValue(R.styleable.SliderPreference_decimalFormat)) {
                getString(R.styleable.SliderPreference_decimalFormat)
            } else {
                "#.#" // Default decimal format
            }
            outputScale = getFloat(R.styleable.SliderPreference_outputScale, 0.5f)
            showDefaultIndicator =
                getBoolean(R.styleable.SliderPreference_showDefaultIndicator, false)
            val defaultValStr = getString(androidx.preference.R.styleable.Preference_defaultValue)

            if (valueFormat == null) valueFormat = ""

            try {
                val scanner = Scanner(defaultValStr)
                scanner.useDelimiter(",")
                scanner.useLocale(Locale.ENGLISH)

                while (scanner.hasNext()) {
                    defaultValue.add(scanner.nextFloat())
                }
            } catch (ignored: Exception) {
                Log.e(
                    TAG,
                    String.format("SliderPreference: Error parsing default values for key: $key")
                )
            }

            if (defaultValue.isEmpty()) {
                defaultValue.add(valueFrom)
            }

            recycle()
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        mTitleView = holder.itemView.findViewById(android.R.id.title)
        mSummaryView = holder.itemView.findViewById(android.R.id.summary)
        mSlider = holder.itemView.findViewById(R.id.slider)

        mSlider!!.tag = key
        mSlider!!.addOnSliderTouchListener(sliderTouchListener)
        mSlider!!.addOnChangeListener(changeListener)
        mSlider!!.setLabelFormatter(labelFormatter)

        mMinusButton = holder.itemView.findViewById(R.id.minus_button)
        mPlusButton = holder.itemView.findViewById(R.id.plus_button)
        mResetButton = holder.itemView.findViewById(R.id.reset_button)

        if (showResetButton && defaultValue.isNotEmpty()) {
            mResetButton!!.visibility = View.VISIBLE
            mResetButton!!.isEnabled = isEnabled && !defaultValue.containsAll(mSlider!!.values)

            mResetButton!!.setOnClickListener { v: View ->
                handleResetButton()
                v.weakVibrate()

                mSlider!!.values = defaultValue
                mResetButton!!.isEnabled = false

                mSummaryView!!.apply {
                    text = mSlider!!.values.joinToString(separator = " - ") { sliderValue ->
                        labelFormatter.getFormattedValue(sliderValue)
                    }
                }

                if (showController) updateControllerButtons()

                savePrefs()
            }
        } else {
            mResetButton!!.visibility = View.GONE
        }

        if (showController) {
            mMinusButton!!.visibility = View.VISIBLE
            mPlusButton!!.visibility = View.VISIBLE

            mMinusButton!!.setOnClickListener { v: View ->
                v.weakVibrate()
                val currentValue = mSlider?.values?.get(0) ?: return@setOnClickListener
                if (currentValue <= valueFrom) return@setOnClickListener

                val newValue = (currentValue - tickInterval).coerceAtLeast(valueFrom)
                mSlider!!.values = listOf(newValue)
                mSummaryView!!.text =
                    mSlider!!.values.joinToString(separator = " - ") { sliderValue ->
                        labelFormatter.getFormattedValue(sliderValue)
                    }

                updateControllerButtons()
                handleResetButton()
                savePrefs()
            }

            mPlusButton!!.setOnClickListener { v: View ->
                v.weakVibrate()
                val currentValue = mSlider?.values?.get(0) ?: return@setOnClickListener
                if (currentValue >= valueTo) return@setOnClickListener

                val newValue = (currentValue + tickInterval).coerceAtMost(valueTo)
                mSlider!!.values = listOf(newValue)
                mSummaryView!!.text =
                    mSlider!!.values.joinToString(separator = " - ") { sliderValue ->
                        labelFormatter.getFormattedValue(sliderValue)
                    }

                updateControllerButtons()
                handleResetButton()
                savePrefs()
            }
        } else {
            mMinusButton!!.visibility = View.GONE
            mPlusButton!!.visibility = View.GONE
        }

        sliderValue = holder.itemView.findViewById(androidx.preference.R.id.seekbar_value)

        mSlider!!.valueFrom = valueFrom
        mSlider!!.valueTo = valueTo
        mSlider!!.stepSize = tickInterval
        mSlider!!.isTickVisible = tickVisible

        syncState()

        mSummaryView!!.apply {
            text = mSlider!!.values.joinToString(separator = " - ") { sliderValue ->
                labelFormatter.getFormattedValue(sliderValue)
            }
            visibility = if (showValueLabel) View.VISIBLE else View.GONE
        }

        if (showController) updateControllerButtons()

        handleResetButton()
    }

    fun setMin(value: Float) {
        valueFrom = value
        if (mSlider != null) mSlider!!.valueFrom = value
    }

    fun setMax(value: Float) {
        valueTo = value
        if (mSlider != null) mSlider!!.valueTo = value
    }

    fun setValues(values: List<Float>) {
        defaultValue.clear()
        defaultValue.addAll(values)
        if (mSlider != null) mSlider!!.values = values
    }

    private fun handleResetButton() {
        if (mResetButton == null) return

        if (showResetButton) {
            mResetButton!!.visibility = View.VISIBLE

            if (mSlider!!.values.isNotEmpty()) {
                mResetButton!!.isEnabled = isEnabled && !defaultValue.containsAll(mSlider!!.values)
            }
        } else {
            mResetButton!!.visibility = View.GONE
        }
    }

    private fun syncState() {
        var needsCommit = false

        var values: MutableList<Float> = getValues(sharedPreferences!!, key, valueFrom)

        // float and double are not accurate when it comes to decimal points
        val step = BigDecimal(mSlider!!.stepSize.toString())

        for (i in values.indices) {
            val round = BigDecimal(Math.round(values[i] / mSlider!!.stepSize))
            val value = min(
                max(step.multiply(round).toDouble(), mSlider!!.valueFrom.toDouble()),
                mSlider!!.valueTo.toDouble()
            )
            if (value != values[i].toDouble()) {
                values[i] = value.toFloat()
                needsCommit = true
            }
        }

        if (values.size < valueCount) {
            needsCommit = true
            values = defaultValue
            while (values.size < valueCount) {
                values.add(valueFrom)
            }
        } else if (values.size > valueCount) {
            needsCommit = true
            while (values.size > valueCount) {
                values.removeAt(values.size - 1)
            }
        }

        try {
            mSlider!!.values = values
            if (needsCommit) savePrefs()
        } catch (_: Throwable) {
        }
    }

    private fun updateControllerButtons() {
        val currentValue = mSlider?.values?.get(0) ?: valueFrom
        mMinusButton?.isEnabled = isEnabled && currentValue > valueFrom
        mPlusButton?.isEnabled = isEnabled && currentValue < valueTo
    }

    var labelFormatter: LabelFormatter = LabelFormatter {
        val formattedValues = mSlider!!.values.joinToString(separator = " - ") { sliderValue ->
            if (valueFormat != null && (valueFormat!!.isBlank() || valueFormat!!.isEmpty())) {
                if (!isDecimalFormat) {
                    (sliderValue / outputScale).toInt().toString()
                } else {
                    DecimalFormat(decimalFormat).format((sliderValue / outputScale).toDouble())
                }
            } else {
                if (!isDecimalFormat) {
                    (sliderValue / 1f).toInt().toString()
                } else {
                    DecimalFormat(decimalFormat).format((sliderValue / outputScale).toDouble())
                }
            }
        }

        if (showDefaultIndicator &&
            defaultValue.isNotEmpty() &&
            defaultValue.containsAll(mSlider!!.values)
        ) {
            getContext().getString(
                R.string.opt_selected3,
                formattedValues,
                valueFormat,
                getContext().getString(R.string.opt_default)
            )
        } else {
            getContext().getString(
                R.string.opt_selected2,
                formattedValues,
                valueFormat
            )
        }
    }

    private var changeListener: RangeSlider.OnChangeListener =
        RangeSlider.OnChangeListener { slider: RangeSlider, _: Float, fromUser: Boolean ->
            if (key != slider.tag) return@OnChangeListener
            if (updateConstantly && fromUser) {
                savePrefs()
            }
        }

    private var sliderTouchListener: RangeSlider.OnSliderTouchListener =
        object : RangeSlider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: RangeSlider) {
                slider.setLabelFormatter(labelFormatter)
            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                if (key != slider.tag) return

                val summary =
                    (slider.parent.parent as ViewGroup).findViewById<TextView>(android.R.id.summary)
                summary.text = labelFormatter.getFormattedValue(slider.values[0])
                summary.visibility =
                    if (showValueLabel) View.VISIBLE else View.GONE

                if (showController) updateControllerButtons()

                handleResetButton()

                if (!updateConstantly) {
                    savePrefs()
                }
            }
        }

    fun savePrefs() {
        setValues(sharedPreferences!!, key, mSlider!!.values)
        setValues(sharedPreferences!!, key, mSlider!!.values)
    }

    companion object {
        private val TAG: String = SliderPreference::class.java.simpleName

        fun setValues(
            sharedPreferences: SharedPreferences,
            key: String?,
            values: List<Float>
        ): Boolean {
            try {
                val writer = StringWriter()
                val jsonWriter = JsonWriter(writer)
                jsonWriter.beginObject()
                jsonWriter.name("")
                jsonWriter.beginArray()

                for (value in values) {
                    jsonWriter.value(value.toDouble())
                }
                jsonWriter.endArray()
                jsonWriter.endObject()
                jsonWriter.close()
                val jsonString = writer.toString()

                sharedPreferences.edit().putString(key, jsonString).apply()

                return true
            } catch (ignored: Exception) {
                return false
            }
        }

        fun getValues(
            prefs: SharedPreferences,
            key: String?,
            defaultValue: Float
        ): MutableList<Float> {
            var values: MutableList<Float>

            try {
                val jsonString = prefs.getString(key, "")!!
                values = getValues(jsonString)
            } catch (ignored: Exception) {
                try {
                    val value = prefs.getFloat(key, defaultValue)
                    values = mutableListOf(value)
                } catch (ignored2: Exception) {
                    try {
                        val value = prefs.getInt(key, Math.round(defaultValue))
                        values = mutableListOf(value.toFloat())
                    } catch (ignored3: Exception) {
                        values = mutableListOf(defaultValue)
                    }
                }
            }

            return values
        }

        @Throws(Exception::class)
        fun getValues(jsonString: String): MutableList<Float> {
            val values: MutableList<Float> = ArrayList()

            if (jsonString.trim { it <= ' ' }.isEmpty()) return values

            JsonReader(StringReader(jsonString)).apply {
                beginObject()
                try {
                    nextName()
                    beginArray()
                } catch (ignored: Exception) {
                }

                while (hasNext()) {
                    try {
                        nextName()
                    } catch (ignored: Exception) {
                    }
                    values.add(nextDouble().toFloat())
                }
            }

            return values
        }

        fun getSingleFloatValue(
            prefs: SharedPreferences,
            key: String?,
            defaultValue: Float
        ): Float {
            var result = defaultValue

            try {
                result = getValues(prefs, key, defaultValue)[0]
            } catch (ignored: Throwable) {
            }

            return result
        }

        fun getSingleIntValue(prefs: SharedPreferences, key: String?, defaultValue: Int): Int {
            return Math.round(getSingleFloatValue(prefs, key, defaultValue.toFloat()))
        }
    }
}
