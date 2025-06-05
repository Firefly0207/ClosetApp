package com.example.closetapp;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Outfit implements Serializable {
    private String id;
    private String userId;
    private List<String> clothIds;
    private String description;
    private Date date;  // Firestore에서 Timestamp로 자동 변환됨
    private boolean favorite;
    private Date timestamp;
    private int likeCount;
    private Map<String, Object> metadata;

    public Outfit() {
        // Firestore 읽을 때 필요한 빈 생성자
    }

    public Outfit(String id, String userId, List<String> clothIds, String description, 
                 Date date, boolean favorite) {
        this.id = id;
        this.userId = userId;
        this.clothIds = clothIds;
        this.description = description;
        this.date = date;
        this.favorite = favorite;
        this.timestamp = new Date();
        this.likeCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<String> getClothIds() { return clothIds; }
    public void setClothIds(List<String> clothIds) { this.clothIds = clothIds; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
} 