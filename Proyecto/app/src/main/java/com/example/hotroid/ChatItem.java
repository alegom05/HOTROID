package com.example.hotroid;

public class ChatItem {
    private String hotelName;
    private String lastMessage;
    private String timestamp;
    private boolean isUnread;
    private int profileImageRes;
    private String chatId;
    private boolean isChatbot;

    public ChatItem() {
        // Constructor vacío
    }

    public ChatItem(String hotelName, String lastMessage, String timestamp, boolean isUnread, int profileImageRes) {
        this.hotelName = hotelName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isUnread = isUnread;
        this.profileImageRes = profileImageRes;
    }

    public ChatItem(String chatId, String hotelName, String lastMessage, String timestamp, boolean isUnread, int profileImageRes) {
        this.chatId = chatId;
        this.hotelName = hotelName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isUnread = isUnread;
        this.profileImageRes = profileImageRes;
        this.isChatbot = false; // Default to false
    }

    public ChatItem(String chatId, String hotelName, String lastMessage, String timestamp, boolean isUnread, int profileImageRes, boolean isChatbot) {
        this.chatId = chatId;
        this.hotelName = hotelName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isUnread = isUnread;
        this.profileImageRes = profileImageRes;
        this.isChatbot = isChatbot;
    }

    // Getters y Setters
    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public int getProfileImageRes() {
        return profileImageRes;
    }

    public void setProfileImageRes(int profileImageRes) {
        this.profileImageRes = profileImageRes;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isChatbot() {
        return isChatbot;
    }

    public void setChatbot(boolean chatbot) {
        isChatbot = chatbot;
    }
}