package com.example.hotroid.bean;

public class ChatHotelItem {
    private String chatId;
    private String hotelId;
    private String hotelName;
    private String lastMessage;
    private long lastMessageTime; // Cambiar a long para compatibilidad
    private int profileImageRes;
    private boolean hasUnreadMessages;
    private int unreadCount;

    public ChatHotelItem() {
        // Constructor vacío
    }

    public ChatHotelItem(String chatId, String hotelId, String hotelName, String lastMessage,
                         long lastMessageTime, int profileImageRes, boolean hasUnreadMessages, int unreadCount) {
        this.chatId = chatId;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.profileImageRes = profileImageRes;
        this.hasUnreadMessages = hasUnreadMessages;
        this.unreadCount = unreadCount;
    }

    // Getters y Setters
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getProfileImageRes() { return profileImageRes; }
    public void setProfileImageRes(int profileImageRes) { this.profileImageRes = profileImageRes; }

    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}