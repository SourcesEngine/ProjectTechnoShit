package com.example.hibyassistant.calendar;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarEventManager {
    private final List<CalendarEvent> events;

    public CalendarEventManager() {
        this.events = new ArrayList<>();
    }

    public static class CalendarEvent {
        private String eventId;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String location;
        private boolean isAllDay;

        public CalendarEvent(String eventId, String title, String description, 
                           LocalDateTime startTime, LocalDateTime endTime, 
                           String location, boolean isAllDay) {
            this.eventId = eventId;
            this.title = title;
            this.description = description;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.isAllDay = isAllDay;
        }

        // Getters and Setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public boolean isAllDay() { return isAllDay; }
        public void setAllDay(boolean allDay) { isAllDay = allDay; }
    }

    /**
     * Add a new event to the calendar
     * @param event The event to add
     * @return true if the event was added successfully, false otherwise
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean addEvent(CalendarEvent event) {
        if (event == null || event.getTitle() == null || event.getTitle().trim().isEmpty()) {
            return false;
        }
        
        // Check for time conflicts
        if (!isTimeSlotAvailable(event.getStartTime(), event.getEndTime())) {
            return false;
        }
        
        events.add(event);
        return true;
    }

    /**
     * Remove an event from the calendar
     * @param eventId The ID of the event to remove
     * @return true if the event was removed successfully, false otherwise
     */
    public boolean removeEvent(String eventId) {
        return events.removeIf(event -> event.getEventId().equals(eventId));
    }

    /**
     * Update an existing event
     * @param updatedEvent The updated event information
     * @return true if the event was updated successfully, false otherwise
     */
    public boolean updateEvent(CalendarEvent updatedEvent) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getEventId().equals(updatedEvent.getEventId())) {
                events.set(i, updatedEvent);
                return true;
            }
        }
        return false;
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean isTimeSlotAvailable(LocalDateTime startTime, LocalDateTime endTime) {
        for (CalendarEvent event : events) {
            if (startTime.isBefore(event.getEndTime()) && 
                endTime.isAfter(event.getStartTime())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get all events in the calendar
     * @return List of all events
     */
    public List<CalendarEvent> getAllEvents() {
        return new ArrayList<>(events);
    }

    /**
     * Search for events by title
     * @param searchTerm The term to search for in event titles
     * @return List of events matching the search term
     */
    public List<CalendarEvent> searchEventsByTitle(String searchTerm) {
        List<CalendarEvent> matchingEvents = new ArrayList<>();
        for (CalendarEvent event : events) {
            if (event.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                matchingEvents.add(event);
            }
        }
        return matchingEvents;
    }
} 