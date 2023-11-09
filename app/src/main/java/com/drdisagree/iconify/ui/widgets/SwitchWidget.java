package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SwitchWidget extends RelativeLayout {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private MaterialSwitch materialSwitch;

    public SwitchWidget(Context context) {
        super(context);
        init(context, null);
    }

    public SwitchWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwitchWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_switch, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchWidget);
        setTitle(typedArray.getString(R.styleable.SwitchWidget_titleText));
        setSummary(typedArray.getString(R.styleable.SwitchWidget_summaryText));
        setSwitchChecked(typedArray.getBoolean(R.styleable.SwitchWidget_isChecked, false));
        typedArray.recycle();

        container.setOnClickListener(v -> {
            if (!materialSwitch.isEnabled())
                return;

            materialSwitch.toggle();
        });
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSummary(int summaryResId) {
        summaryTextView.setText(summaryResId);
    }

    public void setSummary(String summary) {
        summaryTextView.setText(summary);
    }

    public boolean isSwitchChecked() {
        return materialSwitch.isChecked();
    }

    public void setSwitchChecked(boolean isChecked) {
        materialSwitch.setChecked(isChecked);
    }

    public void setSwitchChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        materialSwitch.setOnCheckedChangeListener(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        materialSwitch.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        materialSwitch = findViewById(R.id.switch_widget);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        materialSwitch.setId(View.generateViewId());
    }
}
