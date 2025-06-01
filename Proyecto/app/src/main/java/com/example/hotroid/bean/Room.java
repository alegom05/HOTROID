package com.example.hotroid.bean;

public class Room {
    private String roomNumber;
    private String roomType;
    private String capacityAdults;
    private String capacityChildren;
    private String area;

    public Room(String roomNumber, String roomType, String capacityAdults, String capacityChildren, String area) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacityAdults = capacityAdults;
        this.capacityChildren = capacityChildren;
        this.area = area;
    }

    // Métodos getter
    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getCapacityAdults() {
        return capacityAdults;
    }

    public String getCapacityChildren() {
        return capacityChildren;
    }

    public String getArea() {
        return area;
    }
}
