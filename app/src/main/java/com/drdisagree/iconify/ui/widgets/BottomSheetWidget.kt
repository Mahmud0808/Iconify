package com.drdisagree.iconify.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.adapters.IconsAdapter
import com.drdisagree.iconify.utils.SystemUtil
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetWidget : RelativeLayout, IconsAdapter.OnItemClickListener {

    private lateinit var container: RelativeLayout
    private lateinit var titleTextView: TextView
    private lateinit var summaryTextView: TextView
    private lateinit var iconImageView: ImageView
    private lateinit var mBottomSheetDialog: BottomSheetDialog
    private var selectedIndex = 0
    private var arrayResId = 0
    private var showSelectedPrefix = true
    private lateinit var mEntries: Array<CharSequence>
    private lateinit var mEntryValues: Array<CharSequence>
    private var mDrawables: Array<Drawable>? = null
    private var mValue: String = selectedIndex.toString()
    private var mAdapter: IconsAdapter? = null
    private var onItemClickListener: OnItemClickListener? = null

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

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetWidget)

        showSelectedPrefix =
            typedArray.getBoolean(R.styleable.BottomSheetWidget_showSelectedPrefix, true)
        setTitle(typedArray.getResourceId(R.styleable.BottomSheetWidget_titleText, 0))
        arrayResId = typedArray.getResourceId(R.styleable.BottomSheetWidget_entries, 0)

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

        buildEntries()

        val icon = typedArray.getResourceId(R.styleable.BottomSheetWidget_icon, 0)
        var iconSpaceReserved =
            typedArray.getBoolean(R.styleable.BottomSheetWidget_iconSpaceReserved, false)

        typedArray.recycle()

        if (icon != 0) {
            iconSpaceReserved = true
            iconImageView.setImageResource(icon)
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE)
        }

        initBottomSheetDialog()

        container.setOnClickListener { mBottomSheetDialog.show() }
    }

    private fun buildEntries() {
        mEntries = resources.getTextArray(arrayResId)

        val mValues: MutableList<String> = ArrayList()
        for (i in mEntries.indices) {
            mValues.add(i.toString())
        }

        mEntryValues = mValues.toTypedArray<CharSequence>()
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

    fun setDrawable(drawable: Array<Drawable>) {
        mDrawables = drawable
        mAdapter?.setDrawables(drawable)
    }

    fun setCurrentValue(currentValue: String) {
        mValue = currentValue
        mAdapter?.setCurrentValue(currentValue)
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
        mValue = idx.toString()

        setSelectedText(list[idx])
        initBottomSheetDialog()
    }

    @SuppressLint("InflateParams")
    private fun initBottomSheetDialog() {
        mBottomSheetDialog = BottomSheetDialog(context)

        val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null)
        val recyclerView = view.findViewById<RecyclerView>(R.id.select_dialog_listview)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar_widget)

        toolbar.setTitle(titleTextView.getText())
        toolbar.isTitleCentered = true
        recyclerView.setLayoutManager(GridLayoutManager(context, 3))

        mAdapter = IconsAdapter(mEntries, mEntryValues, mValue, this)
        mAdapter!!.setDrawables(mDrawables)
        recyclerView.setAdapter(mAdapter)

        mBottomSheetDialog.setContentView(view)
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
            if (SystemUtil.isDarkMode) {
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

        val layoutParams = findViewById<View>(R.id.text_container).layoutParams as LayoutParams
        layoutParams.addRule(END_OF, iconImageView.id)
        findViewById<View>(R.id.text_container).setLayoutParams(layoutParams)
    }

    override fun onDetachedFromWindow() {
        mBottomSheetDialog.dismiss()
        super.onDetachedFromWindow()
    }

    override fun onItemClick(view: View, position: Int) {
        setSelectedIndex(position)
        mBottomSheetDialog.dismiss()
        onItemClickListener?.onItemClick(position)
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    fun interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.selectedIndex = selectedIndex
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        setSelectedIndex(state.selectedIndex)
    }

    private class SavedState : BaseSavedState {

        var selectedIndex = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(`in`: Parcel) : super(`in`) {
            selectedIndex = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(selectedIndex)
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
