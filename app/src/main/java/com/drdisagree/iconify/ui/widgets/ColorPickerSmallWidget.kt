package com.drdisagree.iconify.ui.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.events.ColorDismissedEvent
import com.drdisagree.iconify.ui.events.ColorSelectedEvent
import com.drdisagree.iconify.utils.SystemUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ColorPickerSmallWidget : RelativeLayout {

    private lateinit var container: LinearLayout
    private lateinit var titleTextView: TextView
    private lateinit var colorView: View

    @ColorInt
    private var selectedColor = Color.WHITE
    private var colorPickerDialogId = 0
    private var beforeColorPickerListener: BeforeColorPickerListener? = null
    private var colorPickerListener: OnColorPickerListener? = null
    private var afterColorPickerListener: AfterColorPickerListener? = null

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
        inflate(context, R.layout.view_widget_colorpicker_small, this)
        
        initializeId()
        
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerSmallWidget)
        
        setTitle(typedArray.getString(R.styleable.ColorPickerWidget_titleText))
        val colorResId = typedArray.getResourceId(R.styleable.ColorPickerWidget_previewColor, -1)
        selectedColor = typedArray.getColor(R.styleable.ColorPickerWidget_previewColor, Color.WHITE)
        
        typedArray.recycle()
        
        if (colorResId != -1) {
            previewColor = ContextCompat.getColor(getContext(), colorResId)
        }
    }

    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    fun setTitle(title: String?) {
        titleTextView.text = title
    }

    fun setColorPickerListener(
        activity: FragmentActivity?,
        defaultColor: Int,
        showPresets: Boolean,
        showAlphaSlider: Boolean,
        showColorShades: Boolean
    ) {
        require(activity is MainActivity) { "Activity must be instance of HomePage" }
        previewColor = defaultColor

        container.setOnClickListener {
            beforeColorPickerListener?.onColorPickerShown()

            activity.showColorPickerDialog(
                colorPickerDialogId,
                selectedColor,
                showPresets,
                showAlphaSlider,
                showColorShades
            )
        }
    }

    @get:ColorInt
    var previewColor: Int
        get() = selectedColor
        set(color) {
            var colorCode = color
            selectedColor = colorCode

            if (!isEnabled) {
                colorCode = if (SystemUtil.isDarkMode) {
                    Color.DKGRAY
                } else {
                    Color.LTGRAY
                }
            }

            val drawable = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(colorCode, colorCode)
            )
            drawable.setCornerRadius(resources.getDimension(R.dimen.preview_color_picker_radius) * resources.displayMetrics.density)

            colorView.background = drawable
        }

    private fun onSelectedColorChanged(@ColorInt color: Int) {}

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        container.setEnabled(enabled)
        titleTextView.setEnabled(enabled)
        previewColor = if (enabled) previewColor else Color.GRAY
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private fun initializeId() {
        container = findViewById(R.id.container)
        titleTextView = findViewById(R.id.title)
        colorView = findViewById(R.id.color_widget)
        container.setId(generateViewId())
        titleTextView.setId(generateViewId())
        colorView.setId(generateViewId())
        colorPickerDialogId = colorView.id
    }

    fun setBeforeColorPickerListener(listener: BeforeColorPickerListener?) {
        beforeColorPickerListener = listener
    }

    fun setOnColorSelectedListener(listener: OnColorPickerListener?) {
        colorPickerListener = listener
    }

    fun setAfterColorPickerListener(listener: AfterColorPickerListener?) {
        afterColorPickerListener = listener
    }

    @Suppress("unused")
    @Subscribe
    fun onColorSelected(event: ColorSelectedEvent) {
        if (event.dialogId == colorPickerDialogId) {
            previewColor = event.selectedColor
            colorPickerListener?.onColorSelected(event.selectedColor)
        }
    }

    @Suppress("unused")
    @Subscribe
    fun onDialogDismissed(event: ColorDismissedEvent) {
        if (event.dialogId == colorPickerDialogId) {
            afterColorPickerListener?.onColorPickerDismissed()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.selectedColor = selectedColor
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        previewColor = state.selectedColor
    }

    interface BeforeColorPickerListener {
        fun onColorPickerShown()
    }

    interface OnColorPickerListener {
        fun onColorSelected(color: Int)
    }

    interface AfterColorPickerListener {
        fun onColorPickerDismissed()
    }

    private class SavedState : BaseSavedState {

        var selectedColor = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            selectedColor = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(selectedColor)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
