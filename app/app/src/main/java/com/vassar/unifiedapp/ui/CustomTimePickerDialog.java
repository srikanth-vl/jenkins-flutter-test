package com.vassar.unifiedapp.ui;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomTimePickerDialog extends TimePickerDialog {

    private final static int TIME_PICKER_INTERVAL = 1;
    private TimePicker mTimePicker;
    private final OnTimeSetListener mTimeSetListener;
    private int mHours;
    private int mMinutes;
    private TextView mCurrentTimePicker;

    public CustomTimePickerDialog(Context context, OnTimeSetListener listener,
                                  int hourOfDay, int minute, boolean is24HourView, TextView currentTimePicker) {
        super(context, TimePickerDialog.THEME_HOLO_LIGHT, null, hourOfDay,
                minute / TIME_PICKER_INTERVAL, is24HourView);
        mTimeSetListener = listener;
        mHours = hourOfDay;
        mMinutes = minute;
        mCurrentTimePicker = currentTimePicker;
    }

    @Override
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minuteOfHour / TIME_PICKER_INTERVAL);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                mCurrentTimePicker.setText(String.format("%02d", mTimePicker.getCurrentHour()) + ":" +
                        (String.format("%02d", mTimePicker.getCurrentMinute() * TIME_PICKER_INTERVAL)));
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");
            Field timePickerField = classForid.getField("timePicker");
            mTimePicker = (TimePicker) findViewById(timePickerField.getInt(null));

            Field hourfield = classForid.getField("hour");
            NumberPicker hourSpinner = (NumberPicker) mTimePicker
                    .findViewById(hourfield.getInt(null));

            Field field = classForid.getField("minute");
            NumberPicker minuteSpinner = (NumberPicker) mTimePicker
                    .findViewById(field.getInt(null));

            hourSpinner.setMinValue(0);
            hourSpinner.setMaxValue(mHours);

            minuteSpinner.setMinValue(0);
            minuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);

            List<String> displayedMinutes = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedMinutes.add(String.format("%02d", i));
            }

            List<String> displayedHours = new ArrayList<>();
            for (int i = 0; i < 24; i += 1) {
                displayedHours.add(String.format("%02d", i));
            }

            minuteSpinner.setDisplayedValues(displayedMinutes
                    .toArray(new String[displayedMinutes.size()]));

            hourSpinner.setDisplayedValues(displayedHours
                    .toArray(new String[displayedHours.size()]));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

