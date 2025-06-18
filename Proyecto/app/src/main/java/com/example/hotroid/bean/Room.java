package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId; // Importar esto para el ID del documento

public class Room {
    @DocumentId // Esto le dice a Firestore que use el ID del documento para este campo
    private String id;
    private String roomNumber;
    private String roomType;
    private int capacityAdults;
    private int capacityChildren;
    private double area;
    private String status; // Agregando el campo status, si lo usas o planeas usarlo

    // Constructor vacío necesario para Firestore
    public Room() {}

    // Constructor completo
    public Room(String id, String roomNumber, String roomType, int capacityAdults, int capacityChildren, double area) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacityAdults = capacityAdults;
        this.capacityChildren = capacityChildren;
        this.area = area;
        this.status = "Available"; // Estado inicial por defecto, puedes ajustarlo
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public int getCapacityAdults() { return capacityAdults; }
    public int getCapacityChildren() { return capacityChildren; }
    public double getArea() { return area; }
    public String getStatus() { return status; } // Getter para status

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setCapacityAdults(int capacityAdults) { this.capacityAdults = capacityAdults; }
    public void setCapacityChildren(int capacityChildren) { this.capacityChildren = capacityChildren; }
    public void setArea(double area) { this.area = area; }
    public void setStatus(String status) { this.status = status; } // Setter para status
}