package com.drdisagree.iconify.ui.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefsHelper;
import com.drdisagree.iconify.ui.utils.CarouselLayoutManager;
import com.drdisagree.iconify.ui.utils.SnapOnScrollListener;

public class RecyclerPreference extends Preference {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private String mKey;
    private int mDefaultValue;

    public RecyclerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSelectable(false);
        setLayoutResource(R.layout.custom_preference_recyclerview);

    }

    public RecyclerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setSelectable(false);
    }

    public void setPreference(String key, int defaultValue) {
        mKey = key;
        mDefaultValue = defaultValue;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mRecyclerView = (RecyclerView) holder.findViewById(R.id.recycler_view);
        // Create a new LayoutManager instance for each RecyclerView
        mRecyclerView.setLayoutManager(new CarouselLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.scrollToPosition(PrefsHelper.getGetPrefs().getInt(mKey, mDefaultValue));
        SnapHelper snapHelper = new PagerSnapHelper();
        if (mRecyclerView.getOnFlingListener() == null) {
            snapHelper.attachToRecyclerView(mRecyclerView);
            SnapOnScrollListener snapOnScrollListener = new SnapOnScrollListener(snapHelper, SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL, position -> mRecyclerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK));
            mRecyclerView.addOnScrollListener(snapOnScrollListener);
        }
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

}
