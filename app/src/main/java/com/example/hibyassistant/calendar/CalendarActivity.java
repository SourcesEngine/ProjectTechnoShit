package com.example.hibyassistant.calendar;

import static java.lang.String.*;

import android.os.Build;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hibyassistant.CalendarEventManager;
import com.example.hibyassistant.R;
import com.google.android.material.button.MaterialButton;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity implements AddEventDialog.OnEventAddedListener {
    private GridView calendarGridView;
    private TextView monthYearText;
    private ImageButton prevMonthButton, nextMonthButton;
    private MaterialButton addEventButton;
    private CalendarAdapter calendarAdapter;
    private Calendar currentDate;
    private List<String> days;
    private List<CalendarEventManager.CalendarEvent> events;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_calendar);
            initializeViews();
            setupCalendar();
            setupListeners();
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing calendar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            calendarGridView = findViewById(R.id.calendarGridView);
            monthYearText = findViewById(R.id.monthYearText);
            prevMonthButton = findViewById(R.id.prevMonthButton);
            nextMonthButton = findViewById(R.id.nextMonthButton);
            addEventButton = findViewById(R.id.addEventButton);
            events = new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("Error finding views: " + e.getMessage());
        }
    }

    private void setupCalendar() {
        try {
            currentDate = Calendar.getInstance();
            days = new ArrayList<>();
            calendarAdapter = new CalendarAdapter(this, days);
            calendarGridView.setAdapter(calendarAdapter);
            updateCalendar();
        } catch (Exception e) {
            throw new RuntimeException("Error setting up calendar: " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupListeners() {
        try {
            prevMonthButton.setOnClickListener(v -> {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            });

            nextMonthButton.setOnClickListener(v -> {
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            });

            addEventButton.setOnClickListener(v -> showAddEventDialog());

            calendarGridView.setOnItemClickListener((parent, view, position, id) -> {
                String day = days.get(position);
                if (!day.isEmpty()) {
                    showEventsForDay(Integer.parseInt(day));
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error setting up listeners: " + e.getMessage());
        }
    }

    private void showAddEventDialog() {
        AddEventDialog dialog = new AddEventDialog(this, this);
        dialog.show();
    }

    @Override
    public void onEventAdded(String title, String description, String location,
                           LocalDateTime startDateTime, LocalDateTime endDateTime,
                           boolean isAllDay) {
        CalendarEventManager.CalendarEvent event = new CalendarEventManager.CalendarEvent(title, description, startDateTime, endDateTime, location, isAllDay);
        events.add(event);
        Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show();
        updateCalendar(); // Refresh the calendar to show the new event
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showEventsForDay(int day) {
        List<CalendarEventManager.CalendarEvent> dayEvents = new ArrayList<>();
        for (CalendarEventManager.CalendarEvent event : events) {
            if (event.getStartTime().getDayOfMonth() == day &&
                event.getStartTime().getMonthValue() == currentDate.get(Calendar.MONTH) + 1 &&
                event.getStartTime().getYear() == currentDate.get(Calendar.YEAR)) {
                dayEvents.add(event);
            }
        }

        if (dayEvents.isEmpty()) {
            Toast.makeText(this, "No events for this day", Toast.LENGTH_SHORT).show();
        } else {
            StringBuilder eventList = new StringBuilder();
            for (CalendarEventManager.CalendarEvent event : dayEvents) {
                eventList.append(event.getTitle())
                        .append(" (")
                        .append(formatTime(event.getStartTime()))
                        .append(")\n");
            }
            Toast.makeText(this, eventList.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String formatTime(LocalDateTime dateTime) {
        return String.format("%02d:%02d", dateTime.getHour(), dateTime.getMinute());
    }

    private void updateCalendar() {
        try {
            days.clear();
            
            // Set month and year text
            String monthYear = format("%s %d",
                getMonthName(currentDate.get(Calendar.MONTH)),
                currentDate.get(Calendar.YEAR));
            monthYearText.setText(monthYear);

            // Get first day of month
            Calendar firstDay = (Calendar) currentDate.clone();
            firstDay.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);

            // Add empty days for alignment
            for (int i = 1; i < firstDayOfWeek; i++) {
                days.add("");
            }

            // Add days of the month
            int daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int i = 1; i <= daysInMonth; i++) {
                days.add(valueOf(i));
            }

            calendarAdapter.setCurrentMonth(currentDate.get(Calendar.MONTH));
            calendarAdapter.setCurrentYear(currentDate.get(Calendar.YEAR));
            calendarAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Error updating calendar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getMonthName(int month) {
        String[] monthNames = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
        return monthNames[month];
    }
}