package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.SystemUtils
import com.google.android.material.materialswitch.MaterialSwitch

class SwitchWidget : RelativeLayout {

    private lateinit var container: RelativeLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var iconImageView: ImageView
    private lateinit var materialSwitch: MaterialSwitch
    private var beforeSwitchChangeListener: BeforeSwitchChangeListener? = null

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
        inflate(context, R.layout.view_widget_switch, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchWidget)

        setTitle(typedArray.getString(R.styleable.SwitchWidget_titleText))
        setSummary(typedArray.getString(R.styleable.SwitchWidget_summaryText))
        isSwitchChecked = typedArray.getBoolean(R.styleable.SwitchWidget_isChecked, false)
        val icon = typedArray.getResourceId(R.styleable.SwitchWidget_icon, 0)
        var iconSpaceReserved =
            typedArray.getBoolean(R.styleable.SwitchWidget_iconSpaceReserved, false)

        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView.setImageResource(icon)
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE)
        }

        container.setOnClickListener {
            if (materialSwitch.isEnabled) {
                beforeSwitchChangeListener?.beforeSwitchChanged()
                materialSwitch.toggle()
            }
        }
    }

    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
    }

    fun setSummary(summaryResId: Int) {
        summaryTextView.setText(summaryResId)
        if (summaryResId == 0) {
            summaryTextView.visibility = GONE
        } else {
            summaryTextView.visibility = VISIBLE
        }
    }

    fun setSummary(summary: String?) {
        summaryTextView.text = summary
        if (summary.isNullOrEmpty()) {
            summaryTextView.visibility = GONE
        } else {
            summaryTextView.visibility = VISIBLE
        }
    }

    fun setIcon(icon: Int) {
        iconImageView.setImageResource(icon)
        iconImageView.setVisibility(VISIBLE)
    }

    fun setIcon(drawable: Drawable?) {
        iconImageView.setImageDrawable(drawable)
        iconImageView.setVisibility(VISIBLE)
    }

    fun setIconVisibility(visibility: Int) {
        iconImageView.setVisibility(visibility)
    }

    var isSwitchChecked: Boolean
        get() = materialSwitch.isChecked
        set(isChecked) {
            materialSwitch.setChecked(isChecked)
        }

    fun setSwitchChangeListener(listener: CompoundButton.OnCheckedChangeListener?) {
        materialSwitch.setOnCheckedChangeListener(listener)
    }

    fun setBeforeSwitchChangeListener(listener: BeforeSwitchChangeListener?) {
        beforeSwitchChangeListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (enabled) {
            val typedValue = TypedValue()
            val a = context.obtainStyledAttributes(
                typedValue.data, intArrayOf(com.google.android.material.R.attr.colorPrimary)
            )
            val color = a.getColor(0, 0)
            a.recycle()

            iconImageView.setImageTintList(ColorStateList.valueOf(color))
        } else {
            if (SystemUtils.isDarkMode) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY))
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY))
            }
        }

        container.setEnabled(enabled)
        iconImageView.setEnabled(enabled)
        titleTextView.setEnabled(enabled)
        summaryTextView.setEnabled(enabled)
        materialSwitch.setEnabled(enabled)
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        iconImageView = findViewById(R.id.icon)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        materialSwitch = findViewById(R.id.switch_widget)
        container.setId(generateViewId())
        iconImageView.setId(generateViewId())
        titleTextView.setId(generateViewId())
        summaryTextView.setId(generateViewId())
        materialSwitch.setId(generateViewId())

        val layoutParams = findViewById<View>(R.id.text_container).layoutParams as LayoutParams
        layoutParams.addRule(START_OF, materialSwitch.id)
        layoutParams.addRule(END_OF, iconImageView.id)
        findViewById<View>(R.id.text_container).setLayoutParams(layoutParams)
    }

    fun interface BeforeSwitchChangeListener {
        fun beforeSwitchChanged()
    }
}
