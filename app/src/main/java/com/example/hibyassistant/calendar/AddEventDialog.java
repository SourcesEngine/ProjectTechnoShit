package com.example.hibyassistant.calendar;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.hibyassistant.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

public class AddEventDialog extends Dialog {
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText locationInput;
    private Button startDateButton;
    private Button startTimeButton;
    private Button endDateButton;
    private Button endTimeButton;
    private CheckBox allDayCheckBox;
    private Button saveButton;
    private Button cancelButton;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private OnEventAddedListener listener;
    private FragmentActivity activity;

    public interface OnEventAddedListener {
        void onEventAdded(String title, String description, String location,
                         LocalDateTime startDateTime, LocalDateTime endDateTime,
                         boolean isAllDay);
    }

    public AddEventDialog(@NonNull Context context, OnEventAddedListener listener) {
        super(context);
        this.listener = listener;
        if (context instanceof FragmentActivity) {
            this.activity = (FragmentActivity) context;
        } else {
            throw new IllegalArgumentException("Context must be a FragmentActivity");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_event);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        startDateButton = findViewById(R.id.startDateButton);
        startTimeButton = findViewById(R.id.startTimeButton);
        endDateButton = findViewById(R.id.endDateButton);
        endTimeButton = findViewById(R.id.endTimeButton);
        allDayCheckBox = findViewById(R.id.allDayCheckBox);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Initialize with current date and time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startDateTime = LocalDateTime.now();
            endDateTime = startDateTime.plusHours(1);
            updateDateTimeButtons();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupListeners() {
        startDateButton.setOnClickListener(v -> showDatePicker(true));
        startTimeButton.setOnClickListener(v -> showTimePicker(true));
        endDateButton.setOnClickListener(v -> showDatePicker(false));
        endTimeButton.setOnClickListener(v -> showTimePicker(false));

        allDayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            startTimeButton.setEnabled(!isChecked);
            endTimeButton.setEnabled(!isChecked);
            if (isChecked) {
                startDateTime = startDateTime.toLocalDate().atStartOfDay();
                endDateTime = endDateTime.toLocalDate().atTime(23, 59);
                updateDateTimeButtons();
            }
        });

        saveButton.setOnClickListener(v -> saveEvent());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDatePicker(boolean isStartDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            
            if (isStartDate) {
                startDateTime = LocalDateTime.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    startDateTime.getHour(),
                    startDateTime.getMinute()
                );
            } else {
                endDateTime = LocalDateTime.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    endDateTime.getHour(),
                    endDateTime.getMinute()
                );
            }
            updateDateTimeButtons();
        });

        datePicker.show(activity.getSupportFragmentManager(), "DATE_PICKER");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showTimePicker(boolean isStartTime) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(isStartTime ? startDateTime.getHour() : endDateTime.getHour())
                .setMinute(isStartTime ? startDateTime.getMinute() : endDateTime.getMinute())
                .setTitleText("Select time")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            if (isStartTime) {
                startDateTime = startDateTime.withHour(timePicker.getHour())
                        .withMinute(timePicker.getMinute());
            } else {
                endDateTime = endDateTime.withHour(timePicker.getHour())
                        .withMinute(timePicker.getMinute());
            }
            updateDateTimeButtons();
        });

        timePicker.show(activity.getSupportFragmentManager(), "TIME_PICKER");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateDateTimeButtons() {
        startDateButton.setText(formatDateTime(startDateTime, true));
        startTimeButton.setText(formatDateTime(startDateTime, false));
        endDateButton.setText(formatDateTime(endDateTime, true));
        endTimeButton.setText(formatDateTime(endDateTime, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatDateTime(LocalDateTime dateTime, boolean isDate) {
        if (isDate) {
            return String.format("%d/%02d/%02d",
                    dateTime.getYear(),
                    dateTime.getMonthValue(),
                    dateTime.getDayOfMonth());
        } else {
            return String.format("%02d:%02d",
                    dateTime.getHour(),
                    dateTime.getMinute());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveEvent() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String location = locationInput.getText().toString().trim();
        boolean isAllDay = allDayCheckBox.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDateTime.isBefore(startDateTime)) {
            Toast.makeText(getContext(), "End time cannot be before start time", Toast.LENGTH_SHORT).show();
            return;
        }

        listener.onEventAdded(title, description, location, startDateTime, endDateTime, isAllDay);
        dismiss();
    }
} 