package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;

public class CheckoutFirebase {
    @DocumentId
    private String idCheckout;

    private int roomNumber; // CHANGE THIS FROM String TO int
    private String clientName;
    private double baseRate;
    private double additionalCharges;
    private Date checkinDate;
    private Date checkoutDate;

    public CheckoutFirebase() {
        // Constructor sin argumentos
    }

    public CheckoutFirebase(String idCheckout, int roomNumber, String clientName, double baseRate, double additionalCharges, Date checkinDate, Date checkoutDate) { // Update constructor parameter
        this.idCheckout = idCheckout;
        this.roomNumber = roomNumber; // Assign int
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

    public int getRoomNumber() { // CHANGE THIS RETURN TYPE FROM String TO int
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

    public void setRoomNumber(int roomNumber) { // CHANGE THIS PARAMETER TYPE FROM String TO int
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