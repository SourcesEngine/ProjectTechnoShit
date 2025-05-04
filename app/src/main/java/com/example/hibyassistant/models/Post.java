package com.example.hibyassistant.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Post {
    private String id;
    private String userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private List<String> mediaUrls;  // Changed from single imageUrl to list of mediaUrls
    private long timestamp;
    private int likes;
    private int comments;
    private Map<String, Boolean> likedBy; // Map of userIds who liked the post
    private List<Comment> commentList;

    // No-argument constructor required for Firebase
    public Post() {
        this.likedBy = new HashMap<>();
        this.commentList = new ArrayList<>();
    }

    // Constructor with all fields
    public Post(String id, String userId, String userName, String userProfileImage, 
                String content, List<String> mediaUrls, long timestamp, int likes, int comments) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userProfileImage = userProfileImage;
        this.content = content;
        this.mediaUrls = mediaUrls;
        this.timestamp = timestamp;
        this.likes = likes;
        this.comments = comments;
        this.likedBy = new HashMap<>();
        this.commentList = new ArrayList<>();
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

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public Map<String, Boolean> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(Map<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }

    public List<Comment> getCommentList() {
        return commentList;
    }

    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }

    // Helper methods
    public boolean isLikedByUser(String userId) {
        return likedBy != null && likedBy.containsKey(userId);
    }

    public void toggleLike(String userId) {
        if (likedBy == null) {
            likedBy = new HashMap<>();
        }
        
        if (likedBy.containsKey(userId)) {
            likedBy.remove(userId);
            likes--;
        } else {
            likedBy.put(userId, true);
            likes++;
        }
    }

    public void addComment(Comment comment) {
        if (commentList == null) {
            commentList = new ArrayList<>();
        }
        commentList.add(comment);
        comments = commentList.size();
    }

    public String getImageUrl() {
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            return mediaUrls.get(0);
        }
        return null;
    }
} 