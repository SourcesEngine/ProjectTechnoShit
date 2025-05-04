package com.example.hibyassistant.models;

public class Notification {
    private String id;
    private String title;
    private String message;
    private long timestamp;
    private String postId;
    private boolean read;
    private String userId; // ID of the user who triggered the notification

    // Required empty constructor for Firebase
    public Notification() {}

    public Notification(String title, String message, long timestamp, String postId, String userId) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.postId = postId;
        this.userId = userId;
        this.read = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 