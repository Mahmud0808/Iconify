package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.dialogs.EditTextDialog
import com.drdisagree.iconify.utils.SystemUtils

class EditTextWidget : RelativeLayout {

    private lateinit var container: RelativeLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var iconImageView: ImageView
    private lateinit var dialogTitle: String
    private lateinit var dialogSubtitle: String
    private lateinit var dialogHint: String
    private lateinit var dialogText: String
    private lateinit var mEditTextDialog: EditTextDialog

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
        inflate(context, R.layout.view_widget_bottomsheet, this)

        initializeId()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextWidget)
        dialogTitle = typedArray.getString(R.styleable.EditTextWidget_dialogTitleText).toString()
        if (TextUtils.isEmpty(dialogTitle) || dialogTitle == "null") dialogTitle = typedArray.getString(R.styleable.EditTextWidget_titleText).toString();
        dialogSubtitle = typedArray.getString(R.styleable.EditTextWidget_dialogSubtitleText).toString()
        if (dialogSubtitle.isEmpty() || dialogSubtitle == "null") dialogSubtitle = typedArray.getString(R.styleable.EditTextWidget_summaryText).toString();
        if (dialogSubtitle == "null") dialogSubtitle = "" // catch null summary
        dialogHint = typedArray.getString(R.styleable.EditTextWidget_dialogHintText).toString()
        dialogText = typedArray.getString(R.styleable.EditTextWidget_dialogText).toString()

        setTitle(typedArray.getString(R.styleable.EditTextWidget_titleText))
        setSummary(typedArray.getString(R.styleable.EditTextWidget_summaryText))

        val icon = typedArray.getResourceId(R.styleable.BottomSheetWidget_icon, 0)
        var iconSpaceReserved =
            typedArray.getBoolean(R.styleable.BottomSheetWidget_iconSpaceReserved, false)

        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView.setImageResource(icon)
        }

        if (!iconSpaceReserved) {
            iconImageView.visibility = View.GONE
        }

        container.setOnClickListener { mEditTextDialog.show(dialogTitle, dialogSubtitle, dialogHint, dialogText) }
    }

    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
    }

    fun setSummary(summary: String?) {
        summaryTextView.text = summary
    }

    /**
     * Set the text for the EditText in the dialog
     * @param text The text to be displayed in the EditText
     */
    fun setEditTextValue(text: String) {
        dialogText = text
    }

    /**
     * Set the hint for the EditText in the dialog
     * @param hint The hint to be displayed in the EditText
     */
    fun setEditTextHint(hint: String) {
        dialogHint = hint
    }

    fun setIcon(icon: Int) {
        iconImageView.setImageResource(icon)
        iconImageView.visibility = View.VISIBLE
    }

    fun setIcon(drawable: Drawable?) {
        iconImageView.setImageDrawable(drawable)
        iconImageView.visibility = View.VISIBLE
    }

    fun setIconVisibility(visibility: Int) {
        iconImageView.visibility = visibility
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

            iconImageView.imageTintList = ColorStateList.valueOf(color)
        } else {
            if (SystemUtils.isDarkMode) {
                iconImageView.imageTintList = ColorStateList.valueOf(Color.DKGRAY)
            } else {
                iconImageView.imageTintList = ColorStateList.valueOf(Color.LTGRAY)
            }
        }

        container.isEnabled = enabled
        titleTextView.isEnabled = enabled
        summaryTextView.isEnabled = enabled
        iconImageView.isEnabled = enabled
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

        mEditTextDialog = EditTextDialog(context, container.id)

        val layoutParams = findViewById<View>(R.id.text_container).layoutParams as LayoutParams
        layoutParams.addRule(END_OF, iconImageView.id)
        findViewById<View>(R.id.text_container).setLayoutParams(layoutParams)
    }

    override fun onDetachedFromWindow() {
        mEditTextDialog.dismiss()
        super.onDetachedFromWindow()
    }

    /**
     * Set the listener for the EditTextDialog
     * @param listener The listener to be set
     * @see EditTextDialog.EditTextDialogListener
     */
    fun setOnEditTextListener(listener: EditTextDialog.EditTextDialogListener?) {
        mEditTextDialog.setDialogListener(listener)
    }

}