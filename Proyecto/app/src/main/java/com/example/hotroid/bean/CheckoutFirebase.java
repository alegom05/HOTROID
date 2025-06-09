package com.example.hotroid.bean; // O el paquete donde tengas tus otros beans

import com.google.firebase.firestore.DocumentId;

public class CheckoutFirebase {
    @DocumentId // Esto mapea el ID del documento de Firestore a esta propiedad
    private String idCheckout; // Para almacenar el ID del documento de Firestore

    private String roomNumber;
    private String clientName;
    // Puedes añadir más propiedades aquí si tu documento de checkout en Firestore las tiene,
    // por ejemplo:
    // private String hotelName;
    // private long checkinDate; // O String, dependiendo de cómo lo almacenes
    // private long checkoutDate;

    // Constructor vacío requerido por Firestore para deserialización
    public CheckoutFirebase() {
        // Constructor sin argumentos
    }

    // Constructor para cuando creas un objeto en Java antes de guardarlo en Firestore
    // idCheckout puede ser null si usas .add()
    public CheckoutFirebase(String idCheckout, String roomNumber, String clientName) {
        this.idCheckout = idCheckout;
        this.roomNumber = roomNumber;
        this.clientName = clientName;
    }

    // --- Getters ---
    public String getIdCheckout() {
        return idCheckout;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getClientName() {
        return clientName;
    }

    // --- Setters (Necesarios para Firestore para deserialización) ---
    public void setIdCheckout(String idCheckout) {
        this.idCheckout = idCheckout;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // Puedes añadir setters/getters para las nuevas propiedades si las agregas
    // public String getHotelName() { return hotelName; }
    // public void setHotelName(String hotelName) { this.hotelName = hotelName; }
}