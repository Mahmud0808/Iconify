package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.utils.CarouselLayoutManager
import com.drdisagree.iconify.ui.utils.SnapOnScrollListener

class RecyclerPreference(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    Preference(context, attrs, defStyleAttr) {

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mKey: String? = null
    private var mDefaultValue = 0

    init {
        isSelectable = false
        layoutResource = R.layout.custom_preference_recyclerview
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        isSelectable = false
    }

    fun setPreference(key: String?, defaultValue: Int) {
        mKey = key
        mDefaultValue = defaultValue
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        mRecyclerView = holder.findViewById(R.id.recycler_view) as RecyclerView
        // Create a new LayoutManager instance for each RecyclerView
        mRecyclerView!!.layoutManager = CarouselLayoutManager(
            context, RecyclerView.HORIZONTAL, false
        )
        mRecyclerView!!.adapter = mAdapter
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView!!.scrollToPosition(RPrefs.getInt(mKey, mDefaultValue))
        val snapHelper: SnapHelper = PagerSnapHelper()
        if (mRecyclerView!!.onFlingListener == null) {
            snapHelper.attachToRecyclerView(mRecyclerView)
            val snapOnScrollListener = SnapOnScrollListener(
                snapHelper, SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL
            ) { _: Int ->
                mRecyclerView!!.performHapticFeedback(
                    HapticFeedbackConstants.CLOCK_TICK
                )
            }
            mRecyclerView!!.addOnScrollListener(snapOnScrollListener)
        }
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mAdapter = adapter
    }
}
