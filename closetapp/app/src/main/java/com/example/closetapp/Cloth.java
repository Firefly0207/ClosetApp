package com.example.closetapp;


import java.util.List;

public class Cloth {
    private com.google.firebase.Timestamp timestamp;
    private String id;
    private String imageUrl;
    private String category;
    private List<String> tags;
    private boolean favorite;

    public Cloth() {
        // Firestore 읽을 때 필요한 빈 생성자
    }

    public Cloth(String id, String imageUrl, String category, List<String> tags, boolean favorite) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.category = category;
        this.tags = tags;
        this.favorite = favorite;
    }

    // Getter, Setter
    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }
    public com.google.firebase.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(com.google.firebase.Timestamp timestamp) {
        this.timestamp = timestamp;
    }


    public List<String> getTagsList() {
        return tags;
    }

    public String getTags() {
        return String.join(", ", tags);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setId(String id) {
        this.id = id;
    }

}
