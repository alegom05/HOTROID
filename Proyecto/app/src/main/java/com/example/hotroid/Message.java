package com.example.hotroid;

public class Message {

    public enum MessageType {
        TEXT,
        IMAGE,
        FILE,
        CHATBOT_OPTIONS,
        CHATBOT_RESPONSE
    }

    private String id;
    private String content;
    private String timestamp;
    private boolean isFromUser;
    private MessageType messageType;
    private String imageUrl;
    private String fileName;

    public Message() {
        // Constructor vacío
    }

    public Message(String id, String content, String timestamp, boolean isFromUser, MessageType messageType) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.isFromUser = isFromUser;
        this.messageType = messageType;
    }

    public Message(String id, String content, String timestamp, boolean isFromUser, MessageType messageType, String imageUrl) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
        this.isFromUser = isFromUser;
        this.messageType = messageType;
        this.imageUrl = imageUrl;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFromUser() {
        return isFromUser;
    }

    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}