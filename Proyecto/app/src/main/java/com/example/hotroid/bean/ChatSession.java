package com.example.hotroid.bean;

import java.util.ArrayList;
import java.util.List;

public class ChatSession {
    private String chatId;
    private String clientId;
    private String hotelId;
    private String hotelName;
    private String status; // "active", "closed"
    private long createdAt;
    private long lastMessageTime;
    private String lastMessage;
    private int unreadCount;
    private List<ChatMessage> messages;

    public ChatSession() {
        // Constructor vacío requerido para Firestore
        this.messages = new ArrayList<>();
    }

    public ChatSession(String chatId, String clientId, String hotelId, String hotelName) {
        this.chatId = chatId;
        this.clientId = clientId;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.lastMessageTime = System.currentTimeMillis();
        this.lastMessage = "Chat iniciado";
        this.unreadCount = 0;
        this.messages = new ArrayList<>();
    }

    // Getters y Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    public void addMessage(ChatMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        this.lastMessage = message.getContent();
        this.lastMessageTime = message.getTimestamp();
    }
}