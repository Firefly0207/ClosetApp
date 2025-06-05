package com.example.closetapp;

import java.io.Serializable;
import java.util.List;

public class Cloth implements Serializable {
    private transient com.google.firebase.Timestamp timestamp;
    private String id;
    private String imageUrl;
    private String category;
    private List<String> tags;
    private boolean favorite;

    private String washInfo;
    private String fabric;
    private String careInstructions;
    private String lastWornDate;
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
    public String getWashInfo() { return washInfo; }
    public String getFabric() { return fabric; }
    public String getCareInstructions() { return careInstructions; }
    public String getLastWornDate() { return lastWornDate; }
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
    public void setWashInfo(String washInfo) { this.washInfo = washInfo; }
    public void setFabric(String fabric) { this.fabric = fabric; }
    public void setCareInstructions(String careInstructions) { this.careInstructions = careInstructions; }
    public void setLastWornDate(String lastWornDate) { this.lastWornDate = lastWornDate; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cloth cloth = (Cloth) obj;
        return id != null && id.equals(cloth.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
