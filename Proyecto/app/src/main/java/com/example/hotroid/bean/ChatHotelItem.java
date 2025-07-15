package com.example.hotroid.bean;

import java.util.Date;

public class ChatHotelItem {
    private String hotelId;
    private String hotelName;
    private String lastMessage;
    private Date lastMessageTime;
    private int profileImageRes;
    private boolean hasUnreadMessages;
    private int unreadCount;

    public ChatHotelItem() {}

    public ChatHotelItem(String hotelId, String hotelName, String lastMessage,
                         Date lastMessageTime, int profileImageRes) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.profileImageRes = profileImageRes;
        this.hasUnreadMessages = false;
        this.unreadCount = 0;
    }

    // Getters y setters
    public String getHotelId() { return hotelId; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Date getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Date lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getProfileImageRes() { return profileImageRes; }
    public void setProfileImageRes(int profileImageRes) { this.profileImageRes = profileImageRes; }

    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}