package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SwitchWidget extends RelativeLayout {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private ImageView iconImageView;
    private MaterialSwitch materialSwitch;
    private BeforeSwitchChangeListener beforeSwitchChangeListener;

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
        int icon = typedArray.getResourceId(R.styleable.SwitchWidget_icon, 0);
        boolean iconSpaceReserved = typedArray.getBoolean(R.styleable.SwitchWidget_iconSpaceReserved, false);
        typedArray.recycle();

        if (icon != 0) {
            iconSpaceReserved = true;
            iconImageView.setImageResource(icon);
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE);
        }

        container.setOnClickListener(v -> {
            if (materialSwitch.isEnabled()) {
                if (beforeSwitchChangeListener != null) {
                    beforeSwitchChangeListener.beforeSwitchChanged();
                }

                materialSwitch.toggle();
            }
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

    public void setIcon(int icon) {
        iconImageView.setImageResource(icon);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIcon(Drawable drawable) {
        iconImageView.setImageDrawable(drawable);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIconVisibility(int visibility) {
        iconImageView.setVisibility(visibility);
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

    public void setBeforeSwitchChangeListener(BeforeSwitchChangeListener listener) {
        beforeSwitchChangeListener = listener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            TypedValue typedValue = new TypedValue();
            TypedArray a = getContext().obtainStyledAttributes(
                    typedValue.data,
                    new int[]{com.google.android.material.R.attr.colorPrimary}
            );
            int color = a.getColor(0, 0);
            a.recycle();

            iconImageView.setImageTintList(ColorStateList.valueOf(color));
        } else {
            if (SystemUtil.isDarkMode()) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY));
            }
        }

        container.setEnabled(enabled);
        iconImageView.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        materialSwitch.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        iconImageView = findViewById(R.id.icon);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        materialSwitch = findViewById(R.id.switch_widget);

        container.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        materialSwitch.setId(View.generateViewId());

        RelativeLayout.LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.START_OF, materialSwitch.getId());
        layoutParams.addRule(RelativeLayout.END_OF, iconImageView.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }

    public interface BeforeSwitchChangeListener {
        void beforeSwitchChanged();
    }
}
