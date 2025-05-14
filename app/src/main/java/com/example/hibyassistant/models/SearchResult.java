package com.example.hibyassistant.models;

public class SearchResult {
    private String id;
    private String category;
    private String title;
    private String description;
    private String type;

    public SearchResult(String id, String category, String title, String description, String type) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
} 