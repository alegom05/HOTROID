package com.example.hotroid.bean;
import com.google.firebase.firestore.DocumentId;
import java.util.Date; // ¡Importante para fechas!

public class CheckoutFirebase {
    @DocumentId // Esto mapea el ID del documento de Firestore a esta propiedad
    private String idCheckout; // Para almacenar el ID del documento de Firestore

    private String roomNumber;
    private String clientName;
    private double baseRate; // Precio total de la reserva
    private double additionalCharges; // Cobros adicionales de la reserva
    private Date checkinDate; // Fecha de inicio de la reserva
    private Date checkoutDate; // Fecha de fin de la reserva

    // Constructor vacío requerido por Firestore para deserialización
    public CheckoutFirebase() {
        // Constructor sin argumentos
    }

    // Constructor para cuando creas un objeto en Java antes de guardarlo en Firestore
    // idCheckout puede ser null si usas .add()
    public CheckoutFirebase(String idCheckout, String roomNumber, String clientName, double baseRate, double additionalCharges, Date checkinDate, Date checkoutDate) {
        this.idCheckout = idCheckout;
        this.roomNumber = roomNumber;
        this.clientName = clientName;
        this.baseRate = baseRate;
        this.additionalCharges = additionalCharges;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
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

    public double getBaseRate() {
        return baseRate;
    }

    public double getAdditionalCharges() {
        return additionalCharges;
    }

    public Date getCheckinDate() {
        return checkinDate;
    }

    public Date getCheckoutDate() {
        return checkoutDate;
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

    public void setBaseRate(double baseRate) {
        this.baseRate = baseRate;
    }

    public void setAdditionalCharges(double additionalCharges) {
        this.additionalCharges = additionalCharges;
    }

    public void setCheckinDate(Date checkinDate) {
        this.checkinDate = checkinDate;
    }

    public void setCheckoutDate(Date checkoutDate) {
        this.checkoutDate = checkoutDate;
    }
}