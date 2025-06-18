package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId;

public class Room {
    @DocumentId
    private String id;
    private String roomNumber;
    private String roomType;
    private int capacityAdults;
    private int capacityChildren;
    private double area; // ¡Cambiado de String a double!

    public Room() {
        // Default constructor required for calls to DataSnapshot.getValue(Room.class)
    }

    public Room(String id, String roomNumber, String roomType, int capacityAdults, int capacityChildren, double area) { // Cambiado a double
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacityAdults = capacityAdults;
        this.capacityChildren = capacityChildren;
        this.area = area;
    }

    // Métodos getter
    public String getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getCapacityAdults() {
        return capacityAdults;
    }

    public int getCapacityChildren() {
        return capacityChildren;
    }

    public double getArea() { // ¡Cambiado a double!
        return area;
    }

    // Métodos setter
    public void setId(String id) {
        this.id = id;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setCapacityAdults(int capacityAdults) {
        this.capacityAdults = capacityAdults;
    }

    public void setCapacityChildren(int capacityChildren) {
        this.capacityChildren = capacityChildren;
    }

    public void setArea(double area) { // ¡Cambiado a double!
        this.area = area;
    }
}