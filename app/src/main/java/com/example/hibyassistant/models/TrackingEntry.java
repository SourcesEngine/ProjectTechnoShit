package com.example.hibyassistant.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TrackingEntry {
    public static final String TYPE_FEEDING = "feeding";
    public static final String TYPE_SLEEP = "sleep";
    public static final String TYPE_DIAPER = "diaper";
    public static final String TYPE_HEALTH = "health";

    private String id;
    private String userId;
    private String type;
    private Date timestamp;
    private Map<String, Object> data;

    public TrackingEntry() {
        // Required empty constructor for Firestore
    }

    public TrackingEntry(String userId, String type, Map<String, Object> data) {
        this.userId = userId;
        this.type = type;
        this.timestamp = new Date();
        this.data = data;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("type", type);
        map.put("timestamp", timestamp);
        map.put("data", data);
        return map;
    }
} 