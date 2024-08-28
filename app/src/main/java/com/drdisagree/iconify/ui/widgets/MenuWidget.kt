package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.SystemUtils

class MenuWidget : RelativeLayout {

    private lateinit var container: RelativeLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var iconImageView: ImageView
    private lateinit var endArrowImageView: ImageView

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
        inflate(context, R.layout.view_widget_menu, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuWidget)

        setTitle(typedArray.getString(R.styleable.MenuWidget_titleText))
        setSummary(typedArray.getString(R.styleable.MenuWidget_summaryText))
        val icon = typedArray.getResourceId(R.styleable.MenuWidget_icon, 0)
        var iconSpaceReserved =
            typedArray.getBoolean(R.styleable.MenuWidget_iconSpaceReserved, false)
        val showEndArrow = typedArray.getBoolean(R.styleable.MenuWidget_showEndArrow, false)

        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView.setImageResource(icon)
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE)
        }

        if (showEndArrow) {
            endArrowImageView.setVisibility(VISIBLE)
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
    }

    fun setSummary(summary: String?) {
        summaryTextView.text = summary
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

    fun setEndArrowVisibility(visibility: Int) {
        endArrowImageView.setVisibility(visibility)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        container.setOnClickListener(l)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        container.setOnLongClickListener(l)
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
            endArrowImageView.setImageTintList(
                ColorStateList.valueOf(
                    context.getColor(R.color.text_color_primary)
                )
            )
        } else {
            if (SystemUtils.isDarkMode) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY))
                endArrowImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY))
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY))
                endArrowImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY))
            }
        }

        container.setEnabled(enabled)
        iconImageView.setEnabled(enabled)
        titleTextView.setEnabled(enabled)
        summaryTextView.setEnabled(enabled)
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        iconImageView = findViewById(R.id.icon)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        endArrowImageView = findViewById(R.id.end_arrow)
        container.setId(generateViewId())
        iconImageView.setId(generateViewId())
        titleTextView.setId(generateViewId())
        summaryTextView.setId(generateViewId())
        endArrowImageView.setId(generateViewId())

        val layoutParams = findViewById<View>(R.id.text_container).layoutParams as LayoutParams
        layoutParams.addRule(START_OF, endArrowImageView.id)
        layoutParams.addRule(END_OF, iconImageView.id)
        findViewById<View>(R.id.text_container).setLayoutParams(layoutParams)
    }
}
