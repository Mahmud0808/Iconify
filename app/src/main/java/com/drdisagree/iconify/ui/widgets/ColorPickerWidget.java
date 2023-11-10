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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.ui.activities.HomePage;
import com.drdisagree.iconify.ui.events.ColorDismissedEvent;
import com.drdisagree.iconify.ui.events.ColorSelectedEvent;
import com.drdisagree.iconify.utils.SystemUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class ColorPickerWidget extends RelativeLayout {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private View colorView;
    private @ColorInt int selectedColor = Color.WHITE;
    private int colorPickerDialogId;
    private BeforeColorPickerListener beforeColorPickerListener;
    private OnColorPickerListener colorPickerListener;
    private AfterColorPickerListener afterColorPickerListener;

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
        int colorResId = typedArray.getResourceId(R.styleable.ColorPickerWidget_previewColor, -1);
        selectedColor = typedArray.getColor(R.styleable.ColorPickerWidget_previewColor, Color.WHITE);
        typedArray.recycle();

        if (colorResId != -1) {
            setPreviewColor(ContextCompat.getColor(getContext(), colorResId));
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

    public void setColorPickerListener(
            FragmentActivity activity,
            int defaultColor,
            boolean showPresets,
            boolean showAlphaSlider,
            boolean showColorShades
    ) {
        if (!(activity instanceof HomePage)) {
            throw new IllegalArgumentException("Activity must be instance of HomePage");
        }

        setPreviewColor(defaultColor);
        container.setOnClickListener(v -> {
                    if (beforeColorPickerListener != null) {
                        beforeColorPickerListener.onColorPickerShown();
                    }

                    ((HomePage) activity).showColorPickerDialog(
                            colorPickerDialogId,
                            this.selectedColor,
                            showPresets,
                            showAlphaSlider,
                            showColorShades);
                }
        );
    }

    public void setPreviewColor(@ColorInt int color) {
        this.selectedColor = color;

        if (!isEnabled()) {
            if (SystemUtil.isDarkMode()) {
                color = Color.DKGRAY;
            } else {
                color = Color.LTGRAY;
            }
        }

        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{color, color}
        );
        drawable.setCornerRadius(getResources().getDimension(R.dimen.preview_color_picker_radius) * getResources().getDisplayMetrics().density);
        colorView.setBackground(drawable);
    }

    public @ColorInt int getPreviewColor() {
        return selectedColor;
    }

    public int getColorPickerDialogId() {
        return colorPickerDialogId;
    }

    private void onSelectedColorChanged(@ColorInt int color) {
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

        colorPickerDialogId = colorView.getId();
    }

    public void setBeforeColorPickerListener(BeforeColorPickerListener listener) {
        beforeColorPickerListener = listener;
    }

    public void setOnColorSelectedListener(OnColorPickerListener listener) {
        colorPickerListener = listener;
    }

    public void setAfterColorPickerListener(AfterColorPickerListener listener) {
        afterColorPickerListener = listener;
    }

    public interface BeforeColorPickerListener {
        void onColorPickerShown();
    }

    public interface OnColorPickerListener {
        void onColorSelected(int color);
    }

    public interface AfterColorPickerListener {
        void onColorPickerDismissed();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onColorSelected(ColorSelectedEvent event) {
        if (event.dialogId() == colorPickerDialogId) {
            setPreviewColor(event.selectedColor());

            if (colorPickerListener != null) {
                colorPickerListener.onColorSelected(event.selectedColor());
            }
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onDialogDismissed(ColorDismissedEvent event) {
        if (event.dialogId() == colorPickerDialogId) {
            if (afterColorPickerListener != null) {
                afterColorPickerListener.onColorPickerDismissed();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}
