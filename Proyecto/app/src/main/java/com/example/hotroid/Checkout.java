package com.example.hotroid;

public class Checkout {
    private String roomNumber;
    private String clientName;

    public Checkout(String roomNumber, String clientName) {
        this.roomNumber = roomNumber;
        this.clientName = clientName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getClientName() {
        return clientName;
    }
}
