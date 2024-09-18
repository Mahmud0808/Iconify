package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.dialogs.RadioDialog
import com.drdisagree.iconify.utils.SystemUtils
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

class RadioDialogWidget : RelativeLayout, RadioDialog.RadioDialogListener {

    private lateinit var container: RelativeLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var iconImageView: ImageView
    private lateinit var radioDialog: RadioDialog
    private var radioDialogId = 0
    private var selectedIndex = 0
    private var titleResId = 0
    private var arrayResId = 0
    private var showSelectedPrefix = true
    private var radioDialogListener: RadioDialogListener? = null

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
        inflate(context, R.layout.view_widget_radiodialog, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadioDialogWidget)
        showSelectedPrefix =
            typedArray.getBoolean(R.styleable.RadioDialogWidget_showSelectedPrefix, true)
        titleResId = typedArray.getResourceId(R.styleable.RadioDialogWidget_titleText, 0)
        if (titleResId != 0) {
            setTitle(titleResId)
        } else {
            val title = typedArray.getString(R.styleable.RadioDialogWidget_titleText)
            if (title != null) {
                setTitle(title)
            }
        }
        arrayResId = typedArray.getResourceId(R.styleable.RadioDialogWidget_entries, 0)
        if (arrayResId != 0) {
            try {
                setSelectedText(typedArray.getResources().getStringArray(arrayResId)[0])
            } catch (e: Exception) {
                try {
                    setSelectedText(typedArray.getResources().getIntArray(arrayResId)[0])
                } catch (e1: Exception) {
                    setSelectedText(typedArray.getResources().getTextArray(arrayResId)[0] as String)
                }
            }
        }
        val icon = typedArray.getResourceId(R.styleable.RadioDialogWidget_icon, 0)
        var iconSpaceReserved =
            typedArray.getBoolean(R.styleable.RadioDialogWidget_iconSpaceReserved, false)

        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView.setImageResource(icon)
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE)
        }

        initRadioDialog()

        container.setOnClickListener {
            radioDialog.show(
                titleResId,
                arrayResId,
                summaryTextView,
                showSelectedPrefix
            )
        }
    }

    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
    }

    private fun setSelectedText(summaryResId: Int) {
        setSelectedText(context.getString(summaryResId))
    }

    private fun setSelectedText(summary: String) {
        summaryTextView.text = if (showSelectedPrefix) context.getString(
            R.string.opt_selected1,
            summary
        ) else summary
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

    fun getSelectedIndex(): Int {
        return selectedIndex
    }

    fun setSelectedIndex(selectedIndex: Int) {
        var idx = selectedIndex
        val list = listOf(*resources.getStringArray(arrayResId))

        if (idx < 0 || idx >= list.size) {
            idx = 0
        }

        this.selectedIndex = idx

        setSelectedText(list[idx])
        initRadioDialog()
    }

    private fun initRadioDialog() {
        radioDialog = RadioDialog(
            context,
            radioDialogId,
            selectedIndex
        )
        radioDialog.setRadioDialogListener(this)
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
        titleTextView.setEnabled(enabled)
        summaryTextView.setEnabled(enabled)
        iconImageView.setEnabled(enabled)
    }

    fun setOnItemSelectedListener(listener: RadioDialogListener?) {
        radioDialogListener = listener
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        titleTextView = findViewById(R.id.title)
        summaryTextView = findViewById(R.id.summary)
        iconImageView = findViewById(R.id.icon)
        container.setId(generateViewId())
        titleTextView.setId(generateViewId())
        summaryTextView.setId(generateViewId())
        iconImageView.setId(generateViewId())
        radioDialogId = container.id

        val layoutParams = findViewById<View>(R.id.text_container).layoutParams as LayoutParams
        layoutParams.addRule(END_OF, iconImageView.id)
        findViewById<View>(R.id.text_container).setLayoutParams(layoutParams)
    }

    override fun onDetachedFromWindow() {
        radioDialog.dismiss()
        super.onDetachedFromWindow()
    }

    override fun onItemSelected(dialogId: Int, selectedIndex: Int) {
        if (dialogId == radioDialogId) {
            setSelectedIndex(selectedIndex)
            radioDialogListener?.onItemSelected(selectedIndex)
        }
    }

    fun interface RadioDialogListener {
        fun onItemSelected(index: Int)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, selectedIndex)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        setSelectedIndex(state.selectedIndex)
    }

    @Parcelize
    class SavedState(
        private val parentState: @RawValue Parcelable?,
        val selectedIndex: Int
    ) : BaseSavedState(parentState)
}
