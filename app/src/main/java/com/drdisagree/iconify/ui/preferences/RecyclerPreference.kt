package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.utils.CarouselLayoutManager

class RecyclerPreference : Preference {

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mKey: String? = null
    private var mDefaultValue = 0

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    private fun init() {
        isSelectable = false
        layoutResource = R.layout.custom_preference_recyclerview
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        mRecyclerView = (holder.findViewById(R.id.recycler_view) as RecyclerView).apply {
            layoutManager = CarouselLayoutManager(
                context, RecyclerView.HORIZONTAL, false
            ).also {
                it.setMinifyDistance(0.8f)
            }
            adapter = mAdapter
            setHasFixedSize(true);
            scrollToPosition(RPrefs.getInt(mKey, mDefaultValue))
            if (onFlingListener == null) {
                LinearSnapHelper().attachToRecyclerView(this)
            }
            suppressLayout(!isEnabled)
        }

        if (isEnabled) {
            val title = holder.itemView.findViewById<TextView>(android.R.id.title)
            title.setTextColor(ContextCompat.getColor(context, R.color.textColorPrimary))
        }
    }

    fun setPreference(key: String?, defaultValue: Int) {
        mKey = key
        mDefaultValue = defaultValue
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mAdapter = adapter
    }
}
