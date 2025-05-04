package com.example.hibyassistant.models;

import java.util.Date;

public class ChatHistory {
    private String id;
    private String userId;
    private String message;
    private String response;
    private Date timestamp;

    public ChatHistory() {
        // Required empty constructor for Firestore
    }

    public ChatHistory(String id, String userId, String message, String response, Date timestamp) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.response = response;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 