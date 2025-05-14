package com.example.hibyassistant;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CalendarEventManager {
    private List<CalendarEvent> events;
    private Context context;

    public CalendarEventManager(Context context) {
        this.events = new ArrayList<>();
        this.context = context;
    }

    public static class CalendarEvent {
        private String eventId;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String location;
        private boolean isAllDay;

        public CalendarEvent(String title, String description, 
                           LocalDateTime startTime, LocalDateTime endTime, 
                           String location, boolean isAllDay) {
            this.eventId = UUID.randomUUID().toString();
            this.title = title;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.isAllDay = isAllDay;
        }

        // Getters
        public String getEventId() { return eventId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public String getLocation() { return location; }
        public boolean isAllDay() { return isAllDay; }
    }

    /**
     * Add a new event to the calendar
     * @param title Event title
     * @param description Event description
     * @param startTime Event start time
     * @param endTime Event end time
     * @param location Event location
     * @param isAllDay Whether the event is an all-day event
     * @return true if the event was added successfully, false otherwise
     */
    public boolean addNewEvent(String title, String description, 
                             LocalDateTime startTime, LocalDateTime endTime,
                             String location, boolean isAllDay) {
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            showToast("Event title cannot be empty");
            return false;
        }

        if (startTime == null || endTime == null) {
            showToast("Start and end times must be specified");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (endTime.isBefore(startTime)) {
                showToast("End time cannot be before start time");
                return false;
            }
        }

        // Check for time conflicts
        if (!isTimeSlotAvailable(startTime, endTime)) {
            showToast("This time slot conflicts with an existing event");
            return false;
        }

        // Create and add the new event
        CalendarEvent newEvent = new CalendarEvent(title, description, startTime, endTime, location, isAllDay);
        events.add(newEvent);
        showToast("Event added successfully");
        return true;
    }

    /**
     * Get all events for a specific date
     * @param date The date to get events for
     * @return List of events on the specified date
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<CalendarEvent> getEventsForDate(LocalDateTime date) {
        List<CalendarEvent> eventsForDate = new ArrayList<>();
        for (CalendarEvent event : events) {
            if (event.getStartTime().toLocalDate().equals(date.toLocalDate())) {
                eventsForDate.add(event);
            }
        }
        return eventsForDate;
    }

    /**
     * Check if a time slot is available
     * @param startTime The start time to check
     * @param endTime The end time to check
     * @return true if the time slot is available, false if there's a conflict
     */
    private boolean isTimeSlotAvailable(LocalDateTime startTime, LocalDateTime endTime) {
        for (CalendarEvent event : events) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (startTime.isBefore(event.getEndTime()) &&
                    endTime.isAfter(event.getStartTime())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Show a toast message
     * @param message The message to display
     */
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get all events in the calendar
     * @return List of all events
     */
    public List<CalendarEvent> getAllEvents() {
        return new ArrayList<>(events);
    }
} 