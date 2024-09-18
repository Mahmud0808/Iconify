package com.drdisagree.iconify.ui.preferences;

/*
 * Modified from https://github.com/etidoUP/Material-Time-picker-preference-
 * Credits: etidoUP
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.drdisagree.iconify.R;

public class TimePickerPreference extends Preference {

	private String timeValue = "00:00";

	public TimePickerPreference(Context context) {
		super(context);
	}

	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public TimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		setLayoutResource(R.layout.custom_preference_time_picker);
		if (attrs != null) {
			TypedArray a =
					context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialTimePickerPreference, 0, 0);
			try {
				timeValue = a.getString(R.styleable.MaterialTimePickerPreference_presetValue);
			} catch (Exception e) {
				timeValue = "00:00";
			} finally {
				a.recycle();
			}
		}
	}

	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);
		TextView timeTextView = (TextView) holder.findViewById(R.id.time_stamp);
		timeTextView.setText(timeValue);
	}

	@Override
	protected void onClick() {
		super.onClick();

		// parse hour and minute from timeValue
		AtomicInteger hour = new AtomicInteger(Integer.parseInt(timeValue.split(":")[0]));
		AtomicInteger minute = new AtomicInteger(Integer.parseInt(timeValue.split(":")[1]));

		MaterialTimePicker timePicker =
				new MaterialTimePicker.Builder().setTimeFormat(DateFormat.is24HourFormat(getContext()) ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H).setHour(hour.get()).setMinute(minute.get()).build();

		timePicker.addOnPositiveButtonClickListener(
				v -> {
					hour.set(timePicker.getHour());
					minute.set(timePicker.getMinute());
					String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour.get(), minute.get());

					timeValue = selectedTime;
					persistString(selectedTime);

					notifyChanged();
				});

		timePicker.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "timePicker");
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(Object defaultValue) {
		timeValue = getPersistedString((String) defaultValue);
		persistString(timeValue);
	}

	public String getTimeValue() {
		return this.timeValue;
	}

	public void setTimeValue(String timeValue) {
		this.timeValue = timeValue;
		persistString(timeValue);
		notifyChanged();
	}
}
