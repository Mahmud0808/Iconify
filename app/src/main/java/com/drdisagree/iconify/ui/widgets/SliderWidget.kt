package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.HapticUtils.weakVibrate
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.text.DecimalFormat
import java.util.Objects

class SliderWidget : RelativeLayout {

    private lateinit var container: LinearLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var materialSlider: Slider
    private lateinit var resetButton: MaterialButton
    private var valueFormat: String? = ""
    private var defaultValue = 0
    private var outputScale = 1f
    private var isDecimalFormat = false
    private var decimalFormat: String? = "#.#"
    private var resetClickListener: OnLongClickListener? = null
    private var onSliderTouchListener: Slider.OnSliderTouchListener? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        inflate(context, R.layout.view_widget_slider, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SliderWidget)

        valueFormat = typedArray.getString(R.styleable.SliderWidget_valueFormat)
        defaultValue = typedArray.getInt(R.styleable.SliderWidget_sliderDefaultValue, Int.MAX_VALUE)
        setTitle(typedArray.getString(R.styleable.SliderWidget_titleText))
        setSliderValueFrom(typedArray.getInt(R.styleable.SliderWidget_sliderValueFrom, 0))
        setSliderValueTo(typedArray.getInt(R.styleable.SliderWidget_sliderValueTo, 100))
        setSliderStepSize(typedArray.getInt(R.styleable.SliderWidget_sliderStepSize, 1))
        sliderValue = typedArray.getInt(
            R.styleable.SliderWidget_sliderValue,
            typedArray.getInt(R.styleable.SliderWidget_sliderDefaultValue, 50)
        )
        isDecimalFormat = typedArray.getBoolean(R.styleable.SliderWidget_isDecimalFormat, false)
        decimalFormat = typedArray.getString(R.styleable.SliderWidget_decimalFormat)
        outputScale = typedArray.getFloat(R.styleable.SliderWidget_outputScale, 1f)

        typedArray.recycle()

        if (valueFormat == null) {
            valueFormat = ""
        }

        if (decimalFormat == null) {
            decimalFormat = "#.#"
        }

        setSelectedText()
        handleResetButton()
        setOnSliderTouchListener(null)
        setResetClickListener(null)
    }

    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
    }

    fun setSelectedText() {
        summaryTextView.text =
            if (valueFormat!!.isBlank() || valueFormat!!.isEmpty()) context.getString(
                R.string.opt_selected1, (
                        if (!isDecimalFormat) (materialSlider.value / outputScale).toInt() else DecimalFormat(
                            decimalFormat
                        ).format((materialSlider.value / outputScale).toDouble())
                        ).toString()
            ) else context.getString(
                R.string.opt_selected2,
                if (!isDecimalFormat) materialSlider.value.toInt().toString() else DecimalFormat(
                    decimalFormat
                ).format((materialSlider.value / outputScale).toDouble()),
                valueFormat
            )
    }

    fun setSliderStepSize(value: Int) {
        materialSlider.stepSize = value.toFloat()
    }

    var sliderValue: Int
        get() = materialSlider.value.toInt()
        set(value) {
            materialSlider.value = value.toFloat()
            setSelectedText()
            handleResetButton()
        }

    fun setSliderValueFrom(value: Int) {
        materialSlider.valueFrom = value.toFloat()
    }

    fun setSliderValueTo(value: Int) {
        materialSlider.valueTo = value.toFloat()
    }

    fun setIsDecimalFormat(isDecimalFormat: Boolean) {
        this.isDecimalFormat = isDecimalFormat
        setSelectedText()
    }

    fun setDecimalFormat(decimalFormat: String) {
        this.decimalFormat = Objects.requireNonNullElse(decimalFormat, "#.#")
        setSelectedText()
    }

    fun setOutputScale(scale: Float) {
        outputScale = scale
        setSelectedText()
    }

    fun setOnSliderTouchListener(listener: Slider.OnSliderTouchListener?) {
        onSliderTouchListener = listener

        materialSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                notifyOnSliderTouchStarted(slider)
            }

            override fun onStopTrackingTouch(slider: Slider) {
                setSelectedText()
                handleResetButton()
                notifyOnSliderTouchStopped(slider)
            }
        })

        materialSlider.setLabelFormatter {
            if (valueFormat!!.isBlank() || valueFormat!!.isEmpty()) (if (!isDecimalFormat) (materialSlider.value / outputScale).toInt() else DecimalFormat(
                decimalFormat
            )
                .format((materialSlider.value / outputScale).toDouble())).toString() + valueFormat else (if (!isDecimalFormat) materialSlider.value.toInt()
                .toString() else DecimalFormat(decimalFormat)
                .format((materialSlider.value / outputScale).toDouble())) + valueFormat
        }
    }

    fun setOnSliderChangeListener(listener: Slider.OnChangeListener) {
        materialSlider.addOnChangeListener(listener)
    }

    fun setResetClickListener(listener: OnLongClickListener?) {
        resetClickListener = listener

        resetButton.setOnClickListener { v: View ->
            if (defaultValue == Int.MAX_VALUE) {
                return@setOnClickListener
            }

            sliderValue = defaultValue

            handleResetButton()
            notifyOnResetClicked(v)
        }
    }

    fun resetSlider() {
        resetButton.performLongClick()
    }

    private fun notifyOnSliderTouchStarted(slider: Slider) {
        onSliderTouchListener?.onStartTrackingTouch(slider)
    }

    private fun notifyOnSliderTouchStopped(slider: Slider) {
        onSliderTouchListener?.onStopTrackingTouch(slider)
    }

    private fun notifyOnResetClicked(v: View) {
        v.weakVibrate()
        resetClickListener?.onLongClick(v)
    }

    private fun handleResetButton() {
        if (defaultValue != Int.MAX_VALUE) {
            resetButton.visibility = VISIBLE
            resetButton.isEnabled = materialSlider.value != defaultValue.toFloat()
        } else {
            resetButton.visibility = GONE
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        container.isEnabled = enabled
        titleTextView.isEnabled = enabled
        summaryTextView.isEnabled = enabled
        resetButton.isEnabled = enabled
        materialSlider.isEnabled = enabled
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        materialSlider = findViewById(R.id.slider_widget)
        resetButton = findViewById(R.id.reset_button)
        container.id = generateViewId()
        titleTextView.id = generateViewId()
        summaryTextView.id = generateViewId()
        materialSlider.id = generateViewId()
        resetButton.id = generateViewId()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, materialSlider.value)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        materialSlider.value = state.sliderValue
        setSelectedText()
        handleResetButton()
    }

    @Parcelize
    class SavedState(
        private val parentState: @RawValue Parcelable?,
        val sliderValue: Float
    ) : BaseSavedState(parentState)
}
