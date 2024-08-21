package com.drdisagree.iconify.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.drdisagree.iconify.R;

public class PreferenceMenu extends Preference {

    private boolean showArrow = true; // default is true

    public PreferenceMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public PreferenceMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public PreferenceMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PreferenceMenu(@NonNull Context context) {
        super(context);
        init(null);
    }

    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PreferenceMenu);
            showArrow = a.getBoolean(R.styleable.PreferenceMenu_showArrow, true);
            a.recycle();
        }
        setLayoutResource(R.layout.custom_preference_menu);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        ImageView arrowImageView = (ImageView) holder.findViewById(R.id.end_arrow);
        if (arrowImageView != null) {
            arrowImageView.setVisibility(showArrow ? View.VISIBLE : View.GONE);
        }
    }
}
