package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId;

public class Room {
    @DocumentId
    private String id;
    private int roomNumber;
    private String roomType;
    private int capacityAdults;
    private int capacityChildren;
    private double area;
    private String status;
    private String hotelId;
    private String imageResourceName;
    private double price;
    private transient int imageResourceId;

    // Constructor vacío necesario para Firestore
    public Room() {}

    // Constructor original para mantener compatibilidad
    public Room(String id, int roomNumber, String roomType, int capacityAdults, int capacityChildren, double area) {
        this(id, roomNumber, roomType, capacityAdults, capacityChildren, area, "HOTEL-DEFAULT", determinarPrecioPorTipo(roomType));
    }

    // Constructor completo
    public Room(String id, int roomNumber, String roomType, int capacityAdults, int capacityChildren,
                double area, String hotelId, double price) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacityAdults = capacityAdults;
        this.capacityChildren = capacityChildren;
        this.area = area;
        this.hotelId = hotelId;
        this.price = price;
        this.status = "Available"; // Estado inicial por defecto
    }

    // Método auxiliar estático para determinar precio por tipo
    private static double determinarPrecioPorTipo(String roomType) {
        if (roomType == null) return 800.0;

        switch (roomType.toLowerCase()) {
            case "suite":
            case "suite lujo":
            case "suite ejecutiva":
                return 2000.0;
            case "doble":
            case "doble superior":
                return 1200.0;
            case "individual":
            case "estándar":
            default:
                return 800.0;
        }
    }

    // --- Getters ---
    public String getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public int getCapacityAdults() { return capacityAdults; }
    public int getCapacityChildren() { return capacityChildren; }
    public double getArea() { return area; }
    public String getStatus() { return status; }
    public String getHotelId() { return hotelId; }
    public String getImageResourceName() { return imageResourceName; }
    public double getPrice() { return price; }
    public int getImageResourceId() { return imageResourceId; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setCapacityAdults(int capacityAdults) { this.capacityAdults = capacityAdults; }
    public void setCapacityChildren(int capacityChildren) { this.capacityChildren = capacityChildren; }
    public void setArea(double area) { this.area = area; }
    public void setStatus(String status) { this.status = status; }
    public void setHotelId(String hotelId) { this.hotelId = hotelId; }
    public void setImageResourceName(String imageResourceName) { this.imageResourceName = imageResourceName; }
    public void setPrice(double price) { this.price = price; }
    public void setImageResourceId(int imageResourceId) { this.imageResourceId = imageResourceId; }

    // Método para obtener la cantidad total de personas que pueden alojarse
    public int getTotalCapacity() {
        return capacityAdults + capacityChildren;
    }

    // Método para obtener una descripción de la capacidad
    public String getCapacityDescription() {
        String desc = capacityAdults + " adulto" + (capacityAdults > 1 ? "s" : "");
        if (capacityChildren > 0) {
            desc += ", " + capacityChildren + " niño" + (capacityChildren > 1 ? "s" : "");
        }
        return desc;
    }
}