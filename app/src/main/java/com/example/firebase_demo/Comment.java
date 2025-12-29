package com.example.firebase_demo;

public class Comment {
    private String userId;
    private String userName;
    private String userAvatar;
    private String content;
    private long timestamp;

    public Comment() {
    }

    public Comment(String userId, String userName, String userAvatar, String content, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}