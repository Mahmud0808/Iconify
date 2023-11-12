package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.utils.SystemUtil;

public class MenuWidget extends RelativeLayout {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private ImageView iconImageView;
    private ImageView endArrowImageView;

    public MenuWidget(Context context) {
        super(context);
        init(context, null);
    }

    public MenuWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MenuWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_menu, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuWidget);
        setTitle(typedArray.getString(R.styleable.MenuWidget_titleText));
        setSummary(typedArray.getString(R.styleable.MenuWidget_summaryText));
        int icon = typedArray.getResourceId(R.styleable.MenuWidget_icon, 0);
        boolean iconSpaceReserved = typedArray.getBoolean(R.styleable.MenuWidget_iconSpaceReserved, false);
        boolean showEndArrow = typedArray.getBoolean(R.styleable.MenuWidget_showEndArrow, false);
        typedArray.recycle();

        if (icon != 0) {
            iconSpaceReserved = true;
            iconImageView.setImageResource(icon);
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE);
        }

        if (showEndArrow) {
            endArrowImageView.setVisibility(VISIBLE);
        }
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

    public void setEndArrowVisibility(int visibility) {
        endArrowImageView.setVisibility(visibility);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        container.setOnClickListener(l);
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        container.setOnLongClickListener(l);
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
            endArrowImageView.setImageTintList(ColorStateList.valueOf(
                    getContext().getColor(R.color.text_color_primary)
            ));
        } else {
            if (SystemUtil.isDarkMode()) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
                endArrowImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY));
                endArrowImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY));
            }
        }

        container.setEnabled(enabled);
        iconImageView.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        iconImageView = findViewById(R.id.icon);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        endArrowImageView = findViewById(R.id.end_arrow);

        container.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        endArrowImageView.setId(View.generateViewId());

        LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.START_OF, endArrowImageView.getId());
        layoutParams.addRule(RelativeLayout.END_OF, iconImageView.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }
}
