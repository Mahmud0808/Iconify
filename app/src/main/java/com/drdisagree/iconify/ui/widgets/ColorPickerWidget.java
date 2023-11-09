package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.fragment.app.FragmentActivity;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.activities.HomePage;

public class ColorPickerWidget extends RelativeLayout {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private View colorView;
    private @ColorInt int color = Color.WHITE;

    public ColorPickerWidget(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_colorpicker, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerWidget);
        setTitle(typedArray.getString(R.styleable.ColorPickerWidget_titleText));
        setSummary(typedArray.getString(R.styleable.ColorPickerWidget_summaryText));
        typedArray.recycle();
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

    public void setColorPickerListener(
            FragmentActivity activity,
            int dialogId,
            int defaultColor,
            boolean showPresets,
            boolean showAlphaSlider,
            boolean showColorShades
    ) {
        if (!(activity instanceof HomePage)) {
            throw new IllegalArgumentException("Activity must be instance of HomePage");
        }

        setPreviewColor(defaultColor);
        container.setOnClickListener(v ->
                ((HomePage) activity).showColorPickerDialog(
                        dialogId,
                        this.color,
                        showPresets,
                        showAlphaSlider,
                        showColorShades)
        );
    }

    public void setPreviewColor(@ColorInt int color) {
        this.color = color;

        if (!isEnabled()) {
            color = Color.LTGRAY;
        }

        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color, color}
        );
        drawable.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        colorView.setBackground(drawable);
    }

    public @ColorInt int getPreviewColor() {
        return color;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        setPreviewColor(enabled ? getPreviewColor() : Color.GRAY);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        colorView = findViewById(R.id.color_widget);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        colorView.setId(View.generateViewId());
    }
}
