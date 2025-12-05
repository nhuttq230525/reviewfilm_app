package com.example.firebase_demo;import com.google.firebase.database.Exclude;

public class Movie {
    private long likesCount = 0;
    private String id;
    private String title;
    private String vietnameseTitle;
    private String description;
    private String posterUrl;
    private String videoUrl;
    private String year;
    private String duration;
    // Trường này chỉ dùng cho dữ liệu local, không lưu lên Firebase
    @Exclude
    private int posterDrawableId;

    // Trạng thái yêu thích, không lưu trực tiếp vào đối tượng Movie trên DB
    @Exclude
    private boolean isFavorite = false;


    // Constructor rỗng bắt buộc cho Firebase
    public Movie() {
    }

    // Constructor chính (dùng khi đọc từ Firebase)
    public Movie(String id, String title, String description, String posterUrl, String videoUrl, String year, String duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.posterUrl = posterUrl;
        this.videoUrl = videoUrl;
        this.year = year;
        this.duration = duration;
    }


    // --- Getters và Setters ---

    @Exclude
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

    public String getVietnameseTitle() {
        return vietnameseTitle;
    }

    public void setVietnameseTitle(String vietnameseTitle) {
        this.vietnameseTitle = vietnameseTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }
    // ----------------------------------------------------

    @Exclude
    public int getPosterDrawableId() {
        return posterDrawableId;
    }

    @Exclude
    public void setPosterDrawableId(int posterDrawableId) {
        this.posterDrawableId = posterDrawableId;
    }

    @Exclude
    public boolean isFavorite() {
        return isFavorite;
    }

    @Exclude
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
