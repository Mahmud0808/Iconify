package com.drdisagree.iconify.ui.preferences;

/*
 * From Siavash79/rangesliderpreference
 * https://github.com/siavash79/rangesliderpreference
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.drdisagree.iconify.ui.activities.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.RangeSlider;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.drdisagree.iconify.R;

public class SliderPreference extends Preference {

    private static final String TAG = SliderPreference.class.getSimpleName();
    private float valueFrom;
    private float valueTo;
    private final float tickInterval;
    private final boolean showResetButton;
    public final List<Float> defaultValue = new ArrayList<>();
    public RangeSlider slider;
    private MaterialButton mResetButton;
    @SuppressWarnings("unused")
    private TextView sliderValue;
    int valueCount;

    boolean updateConstantly, showValueLabel;

    @SuppressWarnings("unused")
    public SliderPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        setSelectable(false);
    }

    public SliderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSelectable(false);
        setLayoutResource(R.layout.custom_preference_slider);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference);
        updateConstantly = a.getBoolean(R.styleable.SliderPreference_updatesContinuously, false);
        valueCount = a.getInteger(R.styleable.SliderPreference_valueCount, 1);
        valueFrom = a.getFloat(R.styleable.SliderPreference_minVal, 0f);
        valueTo = a.getFloat(R.styleable.SliderPreference_maxVal, 100f);
        tickInterval = a.getFloat(R.styleable.SliderPreference_tickInterval, 1f);
        showResetButton = a.getBoolean(R.styleable.SliderPreference_showResetButton, false);
        showValueLabel = a.getBoolean(R.styleable.SliderPreference_showValueLabel, true);
        String defaultValStr = a.getString(androidx.preference.R.styleable.Preference_defaultValue);

        try {
            Scanner scanner = new Scanner(defaultValStr);
            scanner.useDelimiter(",");
            scanner.useLocale(Locale.ENGLISH);

            while (scanner.hasNext()) {
                defaultValue.add(scanner.nextFloat());
            }
        } catch (Exception ignored) {
            Log.e(TAG, String.format("SliderPreference: Error parsing default values for key: %s", getKey()));
        }

        a.recycle();
    }

    public void savePrefs() {
        setValues(getSharedPreferences(), getKey(), slider.getValues());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean setValues(SharedPreferences sharedPreferences, String key, List<Float> values) {
        try {
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.beginObject();
            jsonWriter.name("");
            jsonWriter.beginArray();

            for (float value : values) {
                jsonWriter.value(value);
            }
            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.close();
            String jsonString = writer.toString();

            sharedPreferences.edit().putString(key, jsonString).apply();

            return true;

        } catch (Exception ignored) {
            return false;
        }
    }

    public void syncState() {
        boolean needsCommit = false;

        List<Float> values = getValues(getSharedPreferences(), getKey(), valueFrom);
        BigDecimal step = new BigDecimal(String.valueOf(slider.getStepSize())); //float and double are not accurate when it comes to decimal points

        for (int i = 0; i < values.size(); i++) {
            BigDecimal round = new BigDecimal(Math.round(values.get(i) / slider.getStepSize()));
            double v = Math.min(Math.max(step.multiply(round).doubleValue(), slider.getValueFrom()), slider.getValueTo());
            if (v != values.get(i)) {
                values.set(i, (float) v);
                needsCommit = true;
            }
        }
        if (values.size() < valueCount) {
            needsCommit = true;
            values = defaultValue;
            while (values.size() < valueCount) {
                values.add(valueFrom);
            }
        } else if (values.size() > valueCount) {
            needsCommit = true;
            while (values.size() > valueCount) {
                values.remove(values.size() - 1);
            }
        }

        try {
            slider.setValues(values);
            if (needsCommit) savePrefs();
        } catch (Throwable t) {
            values.clear();
        }
    }

    RangeSlider.OnChangeListener changeListener = (slider, value, fromUser) -> {
        if (!getKey().equals(slider.getTag())) return;

        if (updateConstantly && fromUser) {
            savePrefs();
        }
    };

    RangeSlider.OnSliderTouchListener sliderTouchListener = new RangeSlider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull RangeSlider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull RangeSlider slider) {
            if (!getKey().equals(slider.getTag())) return;

            if (!updateConstantly) {
                savePrefs();
            }
        }
    };

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        TextView title = holder.itemView.findViewById(android.R.id.title);
        title.setTextColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary));

        slider = holder.itemView.findViewById(R.id.slider);
        slider.setTag(getKey());

        slider.addOnSliderTouchListener(sliderTouchListener);
        slider.addOnChangeListener(changeListener);

        mResetButton = holder.itemView.findViewById(R.id.reset_button);
        if (showResetButton) {
            mResetButton.setVisibility(View.VISIBLE);
            mResetButton.setOnClickListener(v -> {
                slider.setValues(defaultValue);
                savePrefs();
            });
        } else {
            mResetButton.setVisibility(View.GONE);
        }

        sliderValue = holder.itemView.findViewById(androidx.preference.R.id.seekbar_value);

        slider.setValueFrom(valueFrom);
        slider.setValueTo(valueTo);
        slider.setStepSize(tickInterval);

        syncState();
    }

    public void setMin(float value) {
        valueFrom = value;
        if (slider != null) slider.setValueFrom(value);
    }

    public void setMax(float value) {
        valueTo = value;
        if (slider != null) slider.setValueTo(value);
    }

    public static List<Float> getValues(SharedPreferences prefs, String key, float defaultValue) {
        List<Float> values;

        try {
            String JSONString = prefs.getString(key, "");
            values = getValues(JSONString);
        } catch (Exception ignored) {
            try {
                float value = prefs.getFloat(key, defaultValue);
                values = Collections.singletonList(value);
            } catch (Exception ignored2) {
                try {
                    int value = prefs.getInt(key, Math.round(defaultValue));
                    values = Collections.singletonList((float) value);
                } catch (Exception ignored3) {
                    values = Collections.singletonList(defaultValue);
                }
            }
        }
        return values;
    }

    public static List<Float> getValues(String JSONString) throws Exception {
        List<Float> values = new ArrayList<>();

        if (JSONString.trim().isEmpty()) return values;

        JsonReader jsonReader = new JsonReader(new StringReader(JSONString));

        jsonReader.beginObject();
        try {
            jsonReader.nextName();
            jsonReader.beginArray();
        } catch (Exception ignored) {
        }

        while (jsonReader.hasNext()) {
            try {
                jsonReader.nextName();
            } catch (Exception ignored) {
            }
            values.add((float) jsonReader.nextDouble());
        }

        return values;
    }

    public static float getSingleFloatValue(SharedPreferences prefs, String key, float defaultValue) {
        float result = defaultValue;

        try {
            result = getValues(prefs, key, defaultValue).get(0);
        } catch (Throwable ignored) {
        }

        return result;
    }

    public static int getSingleIntValue(SharedPreferences prefs, String key, int defaultValue) {
        return Math.round(getSingleFloatValue(prefs, key, defaultValue));
    }
}