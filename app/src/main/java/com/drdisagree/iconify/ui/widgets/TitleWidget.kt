package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.drdisagree.iconify.R

class TitleWidget : RelativeLayout {

    private lateinit var container: LinearLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView

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
        inflate(context, R.layout.view_widget_title, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleWidget)

        setTitle(typedArray.getString(R.styleable.TitleWidget_titleText))
        setSummary(typedArray.getString(R.styleable.TitleWidget_summaryText))
        val iconSpaceReserved =
            typedArray.getBoolean(R.styleable.TitleWidget_iconSpaceReserved, false)

        typedArray.recycle()

        val density = context.resources.displayMetrics.density

        if (iconSpaceReserved) {
            container.apply {
                setPaddingRelative(
                    (60 * density).toInt(),
                    paddingTop,
                    getPaddingEnd(),
                    paddingBottom
                )
            }
        } else {
            container.apply {
                setPaddingRelative(
                    (24 * density).toInt(),
                    paddingTop,
                    getPaddingEnd(),
                    paddingBottom
                )
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
    }

    fun setSummary(summary: String?) {
        summaryTextView.text = summary

        if (summary.isNullOrEmpty()) {
            summaryTextView.visibility = GONE
        } else {
            summaryTextView.visibility = VISIBLE
        }
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        container.setId(generateViewId())
        titleTextView.setId(generateViewId())
        summaryTextView.setId(generateViewId())
    }
}
