package com.example.hibyassistant.models;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private long timestamp;

    // Required empty constructor for Firebase
    public Comment() {
    }

    public Comment(String userId, String userName, String userProfileImage, String content) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 