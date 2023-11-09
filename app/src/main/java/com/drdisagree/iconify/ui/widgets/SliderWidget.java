package com.drdisagree.iconify.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.R;
import com.google.android.material.slider.Slider;

import java.text.DecimalFormat;
import java.util.Objects;

public class SliderWidget extends RelativeLayout {

    private LinearLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private Slider materialSlider;
    private ImageView resetIcon;
    private String valueFormat;
    private int defaultValue;
    private float outputScale = 1f;
    private boolean isDecimalFormat = false;
    private String decimalFormat = "#.#";
    private OnLongClickListener resetClickListener;
    private Slider.OnSliderTouchListener onSliderTouchListener;

    public SliderWidget(Context context) {
        super(context);
        init(context, null);
    }

    public SliderWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SliderWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_slider, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SliderWidget);
        valueFormat = typedArray.getString(R.styleable.SliderWidget_valueFormat);
        defaultValue = typedArray.getInt(R.styleable.SliderWidget_sliderDefaultValue, Integer.MAX_VALUE);
        setTitle(typedArray.getString(R.styleable.SliderWidget_titleText));
        setSliderValueFrom(typedArray.getInt(R.styleable.SliderWidget_sliderValueFrom, 0));
        setSliderValueTo(typedArray.getInt(R.styleable.SliderWidget_sliderValueTo, 100));
        setSliderStepSize(typedArray.getInt(R.styleable.SliderWidget_sliderStepSize, 1));
        setSliderValue(typedArray.getInt(
                R.styleable.SliderWidget_sliderValue,
                typedArray.getInt(R.styleable.SliderWidget_sliderDefaultValue, 50)
        ));
        isDecimalFormat = typedArray.getBoolean(R.styleable.SliderWidget_isDecimalFormat, false);
        decimalFormat = typedArray.getString(R.styleable.SliderWidget_decimalFormat);
        outputScale = typedArray.getFloat(R.styleable.SliderWidget_outputScale, 1f);
        typedArray.recycle();

        if (valueFormat == null) {
            valueFormat = "";
        }

        if (decimalFormat == null) {
            decimalFormat = "#.#";
        }

        setSelectedText();
        handleResetVisibility();
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSelectedText() {
        summaryTextView.setText(
                (valueFormat.isBlank() || valueFormat.isEmpty() ?
                        getContext().getString(
                                R.string.opt_selected1,
                                String.valueOf(
                                        !isDecimalFormat ?
                                                (int) (materialSlider.getValue() / outputScale) :
                                                new DecimalFormat(decimalFormat)
                                                        .format(materialSlider.getValue() / outputScale)
                                )
                        ) :
                        getContext().getString(
                                R.string.opt_selected2,
                                !isDecimalFormat ?
                                        String.valueOf((int) materialSlider.getValue()) :
                                        new DecimalFormat(decimalFormat)
                                                .format(materialSlider.getValue() / outputScale),
                                valueFormat
                        )
                )
        );
    }

    public void setSliderStepSize(int value) {
        materialSlider.setStepSize(value);
    }

    public void setSliderValue(int value) {
        materialSlider.setValue(value);
        setSelectedText();
        handleResetVisibility();
    }

    public int getSliderValue() {
        return (int) materialSlider.getValue();
    }

    public void setSliderValueFrom(int value) {
        materialSlider.setValueFrom(value);
    }

    public void setSliderValueTo(int value) {
        materialSlider.setValueTo(value);
    }

    public void setIsDecimalFormat(boolean isDecimalFormat) {
        this.isDecimalFormat = isDecimalFormat;
        setSelectedText();
    }

    public void setDecimalFormat(String decimalFormat) {
        this.decimalFormat = Objects.requireNonNullElse(decimalFormat, "#.#");
        setSelectedText();
    }

    public void setOutputScale(float scale) {
        this.outputScale = scale;
        setSelectedText();
    }

    public void setOnSliderTouchListener(Slider.OnSliderTouchListener listener) {
        onSliderTouchListener = listener;

        materialSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                notifyOnSliderTouchStarted(slider);
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                setSelectedText();
                handleResetVisibility();
                notifyOnSliderTouchStopped(slider);
            }
        });
    }

    public void setOnSliderChangeListener(Slider.OnChangeListener listener) {
        materialSlider.addOnChangeListener(listener);
    }

    public void setResetClickListener(OnLongClickListener listener) {
        resetClickListener = listener;

        resetIcon.setOnLongClickListener(v -> {
            if (defaultValue == Integer.MAX_VALUE) {
                return false;
            }

            setSliderValue(defaultValue);
            handleResetVisibility();
            notifyOnResetClicked(v);

            return true;
        });
    }

    private void notifyOnSliderTouchStarted(@NonNull Slider slider) {
        if (onSliderTouchListener != null) {
            onSliderTouchListener.onStartTrackingTouch(slider);
        }
    }

    private void notifyOnSliderTouchStopped(@NonNull Slider slider) {
        if (onSliderTouchListener != null) {
            onSliderTouchListener.onStopTrackingTouch(slider);
        }
    }

    private void notifyOnResetClicked(View v) {
        if (resetClickListener != null) {
            resetClickListener.onLongClick(v);
        }
    }

    private void handleResetVisibility() {
        if (defaultValue != Integer.MAX_VALUE && materialSlider.getValue() != defaultValue) {
            resetIcon.setVisibility(VISIBLE);
        } else {
            resetIcon.setVisibility(GONE);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        resetIcon.setEnabled(enabled);
        materialSlider.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        materialSlider = findViewById(R.id.slider_widget);
        resetIcon = findViewById(R.id.reset);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        materialSlider.setId(View.generateViewId());
        resetIcon.setId(View.generateViewId());
    }
}
