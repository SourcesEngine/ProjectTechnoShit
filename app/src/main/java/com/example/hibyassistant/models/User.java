package com.example.hibyassistant.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String photoUrl;

    public User() {}

    public User(String id, String name, String email, String photoUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
} 