package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.preference.ListPreference
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.adapters.ListPreferenceAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog

class BottomSheetListPreference : ListPreference {

    private var mEntryIcons: IntArray? = null
    private var mEntryDrawables: Array<Drawable>? = null
    private var mHasImages = false
    private var mAdapter: ListPreferenceAdapter? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var recyclerView: RecyclerView? = null

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initResource()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initResource()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initResource()
    }

    constructor(context: Context) : super(context) {
        initResource()
    }

    private fun initResource() {
        layoutResource = R.layout.custom_preference_list
    }

    fun setDrawables(@DrawableRes drawables: IntArray) {
        mHasImages = true
        mEntryIcons = drawables
        mAdapter?.updateIcons(drawables)
    }

    fun setDrawables(drawables: Array<Drawable>) {
        mHasImages = true
        mEntryDrawables = drawables
        mAdapter?.updateDrawables(drawables)
    }

    fun setHasImages(hasImages: Boolean) {
        mHasImages = hasImages
    }

    fun setAdapter(adapter: ListPreferenceAdapter?) {
        mAdapter = adapter
    }

    fun getAdapter(): ListPreferenceAdapter? {
        return mAdapter
    }

    fun setAdapterType(type: Int) {
        mAdapter?.type = type
    }

    fun setImages(images: List<String>) {
        mAdapter?.setImages(images)
    }

    fun setDefaultAdapterListener() {
        mAdapter?.setListener(object : ListPreferenceAdapter.OnItemClickListener {

            override fun onItemClick(view: View?, position: Int) {
                if (callChangeListener(entryValues[position].toString())) {
                    setValueIndex(position)
                }
                bottomSheetDialog?.dismiss()
            }
        })
    }

    protected override fun onClick() {
        bottomSheetDialog = BottomSheetDialog(context)

        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.view_bottom_sheet_dialog_layout, null as ViewGroup?)
        recyclerView = view.findViewById(R.id.select_dialog_listview)
        val toolbarPref = view.findViewById<MaterialToolbar>(R.id.toolbar_preference)
        toolbarPref.setTitle(title)
        toolbarPref.isTitleCentered = true
        if (mAdapter != null && mAdapter!!.type == ListPreferenceAdapter.TYPE_BATTERY_ICONS) {
            recyclerView!!.setLayoutManager(GridLayoutManager(context, 3))
        } else {
            recyclerView!!.setLayoutManager(LinearLayoutManager(context))
        }
        if (mAdapter == null) {
            mAdapter = ListPreferenceAdapter(
                entries,
                entryValues,
                mEntryIcons,
                key,
                mHasImages,
                object : ListPreferenceAdapter.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        if (callChangeListener(entryValues[position].toString())) {
                            setValueIndex(position)
                        }
                    }
                }
            )
        }
        recyclerView!!.setAdapter(mAdapter)
        bottomSheetDialog!!.setContentView(view)
        bottomSheetDialog!!.show()
    }

    private fun getValueIndex(): Int {
        return findIndexOfValue(value)
    }

    override fun setValueIndex(index: Int) {
        setValue(entryValues[index].toString())
    }

    fun createDefaultAdapter() {
        mAdapter = ListPreferenceAdapter(
            entries,
            entryValues,
            mEntryIcons,
            key,
            mHasImages,
            object : ListPreferenceAdapter.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (callChangeListener(entryValues[position].toString())) {
                        setValueIndex(position)
                    }
                }
            }
        )
    }

    fun createDefaultAdapter(drawables: Array<Drawable>) {
        setDrawables(drawables)
        mAdapter = ListPreferenceAdapter(
            entries,
            entryValues,
            mEntryDrawables!!,
            key,
            mHasImages,
            object : ListPreferenceAdapter.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (callChangeListener(entryValues[position].toString())) {
                        setValueIndex(position)
                    }
                }
            }
        )
    }

    fun createDefaultAdapter(drawables: Array<Drawable>, listener: OnItemClickListener?) {
        setDrawables(drawables)
        mAdapter = ListPreferenceAdapter(
            entries,
            entryValues,
            mEntryDrawables!!,
            key,
            mHasImages,
            object : ListPreferenceAdapter.OnItemClickListener {
                override fun onItemClick(view: View?, position: Int) {
                    if (callChangeListener(entryValues[position].toString())) {
                        setValueIndex(position)
                    }
                    listener?.onItemClick(position)
                }
            }
        )
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}