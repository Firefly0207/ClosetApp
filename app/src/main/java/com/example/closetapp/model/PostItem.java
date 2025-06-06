package com.example.closetapp.model;

public class PostItem {
    private String documentId;
    private String imageUrl;
    private String caption;
    private int likes;

    public PostItem(String documentId, String imageUrl, String caption, int likes) {
        this.documentId = documentId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likes = likes;
    }

    public String getDocumentId() { return documentId; }
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
}
