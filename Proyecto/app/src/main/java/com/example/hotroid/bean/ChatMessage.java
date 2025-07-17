package com.example.hotroid.bean;

public class ChatMessage {
    private String messageId;
    private String content;
    private boolean isFromBot;
    private long timestamp;
    private String type; // "text", "menu", "info"

    public ChatMessage() {
        // Constructor vacío requerido para Firestore
    }

    public ChatMessage(String messageId, String content, boolean isFromBot, long timestamp, String type) {
        this.messageId = messageId;
        this.content = content;
        this.isFromBot = isFromBot;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters y Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isFromBot() { return isFromBot; }
    public void setFromBot(boolean fromBot) { isFromBot = fromBot; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}